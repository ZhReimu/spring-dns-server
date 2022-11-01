package com.mrx.springdnsserver.service;

import com.mrx.dns.resolver.IResolver;
import com.mrx.springdnsserver.mapper.DnsIpMapper;
import com.mrx.springdnsserver.mapper.DnsMapper;
import com.mrx.springdnsserver.mapper.ResolveLogMapper;
import com.mrx.springdnsserver.model.dns.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.*;
import java.util.stream.Collectors;

import static com.mrx.dns.util.PerformanceUtil.runMeasure;

/**
 * @author Mr.X
 * @since 2022-11-01 11:23
 */
@Service
public class DnsService implements IResolver {

    public static final List<ResolveLog> resolveLog = new ArrayList<>();

    public static final Logger logger = LoggerFactory.getLogger(DnsMapper.class);

    private ResolveLogMapper mapper;

    private DnsMapper dnsMapper;

    private DnsIpMapper dnsIpMapper;

    @Autowired
    public void setDnsIpMapper(DnsIpMapper dnsIpMapper) {
        this.dnsIpMapper = dnsIpMapper;
    }

    @Autowired
    public void setDnsMapper(DnsMapper dnsMapper) {
        this.dnsMapper = dnsMapper;
    }

    @Autowired
    public void setMapper(ResolveLogMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * 保存解析日志, 需要在子线程执行, 以免影响性能
     */
    public void saveLog() {
        runMeasure(() -> {
            Map<String, Integer> logCache = new HashMap<>();
            for (ResolveLog log : resolveLog) {
                // 首先尝试从 logCache 中获取 LogIp 的 id
                Integer ipId = Optional.ofNullable(logCache.get(log.getIp())).orElseGet(() -> {
                    // 若 logCache 中不存在当前 LogIp 的 id, 那就尝试从数据库中获取 LogIp 的 id
                    LogIp logIp = Optional.ofNullable(mapper.getResolveIpByIp(log.getIp())).orElseGet(() -> {
                        // 若 还是不存在, 那就插入一条 LogIp 并返回新插入的 LogIp 的 id
                        LogIp t = LogIp.of(log.getIp());
                        mapper.insertResolveIp(t);
                        return t;
                    });
                    synchronized (logCache) {
                        // 使用数据库中的 LogIp 信息填充 logCache
                        logCache.put(logIp.getIp(), logIp.getId());
                    }
                    // 返回 数据库中的 LogIp 的 id
                    return logIp.getId();
                });
                // 更新此条数据的 ipId
                log.setIpId(ipId);
            }
            // 插入 解析日志
            mapper.insertLogBatch(resolveLog);
            synchronized (resolveLog) {
                resolveLog.clear();
            }
        });
    }

    public List<Integer> countResolveByInterval(int interval, int step) {
        List<Integer> res = new ArrayList<>();
        for (int i = 0; i < interval; i += step) {
            res.add(mapper.countResolveByPeriod(i + step, i));
        }
        return res;
    }

    @Override
    public List<String> getIpsByHost(String nKey, String ip) {
        // 记录日志
        synchronized (resolveLog) {
            resolveLog.add(ResolveLog.of(nKey, ip));
        }
        // 实现 泛域名解析
        DnsRecord gDnsRecord = dnsMapper.getGDnsRecord(nKey);
        if (gDnsRecord != null) {
            logger.info("检测到泛域名: {} -> {}", nKey, gDnsRecord.getHost());
            return gDnsRecord.getIps();
        }
        // 普通域名解析
        List<String> hosts = dnsMapper.getIPsByHost(nKey);
        if (CollectionUtils.isEmpty(hosts)) {
            Host host = new Host(nKey);
            try {
                logger.info("开始递归解析: {}", nKey);
                // 如果没有手动指定 hosts, 那就尝试调用系统 dns 的结果, 只需要 ipv4 的结果
                hosts = Arrays.stream(InetAddress.getAllByName(nKey))
                        .filter(it -> it instanceof Inet4Address)
                        .map(InetAddress::getHostAddress)
                        .collect(Collectors.toList());
                // 递归解析后, 将解析结果存入数据库
                addHostAndDns(host, hosts);
                logger.info("插入 host 与 dns 记录成功, 本次解析结果已缓存");
                return hosts;
            } catch (Exception e) {
                logger.warn("调用系统 dns 出错: {} -> {}", e.getLocalizedMessage(), e.getClass().getName());
                dnsMapper.addErrorHost(host);
                hosts = Collections.emptyList();
            }
        }
        return hosts;
    }

    /**
     * 向数据库添加 host 和 dns
     *
     * @param host 要添加的 host, 无 id
     * @param ips  该 host 所对应的 ip
     */
    public void addHostAndDns(@NonNull final Host host, List<String> ips) {
        // 添加 host 之前, 先检查 host 是否存在
        Host hostInDB = dnsMapper.getHostFromDB(host.getHost());
        // 若不存在, 先走添加 host 流程, 再走添加 dns 流程
        if (hostInDB == null) {
            // 执行了 addHost 后 host 就会有 id
            dnsMapper.addHost(host);
            // 插入 dns
            dnsMapper.addDns(Dns.of(host, addDnsIps(ips)));
        } else {
            // 若存在, 走 添加 dns 流程, hostInDB 里包含 id
            dnsMapper.addDns(Dns.of(hostInDB, addDnsIps(ips)));
        }
    }

    private List<DnsIp> addDnsIps(List<String> ips) {
        // 当前 dns 记录所需的 ip, 没有 id
        List<DnsIp> dnsIps = ips.stream().map(DnsIp::of).collect(Collectors.toList());
        // tb_dns_ip 表里有的 ip
        List<DnsIp> ipsInDB = dnsIpMapper.listDnsIpByIpd(ips);
        // 获取 tb_dns_ip 表里没有的 ip
        List<DnsIp> ipsNotExists = new ArrayList<>(dnsIps);
        ipsNotExists.removeAll(ipsInDB);
        // 批量插入, 插入完毕这些对象就 不会有 id, 这里必须一个个插入
        ipsNotExists.forEach(it -> dnsIpMapper.insertDnsIp(it));
        // 两部分 ip 组合起来
        ipsInDB.addAll(ipsNotExists);
        return ipsInDB;
    }

}
