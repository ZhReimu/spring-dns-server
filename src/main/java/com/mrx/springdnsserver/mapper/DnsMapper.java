package com.mrx.springdnsserver.mapper;

import com.mrx.dns.repository.IHostRepository;
import com.mrx.dns.resolver.IResolver;
import com.mrx.dns.util.NetworkUtil;
import com.mrx.springdnsserver.model.dns.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.*;
import java.util.stream.Collectors;

import static com.mrx.dns.util.NetworkUtil.ipChecker;
import static com.mrx.dns.util.PerformanceUtil.runMeasure;

/**
 * @author Mr.X
 * @since 2022-10-30 16:27
 */
@Mapper
public interface DnsMapper extends IHostRepository, IResolver {

    Logger logger = LoggerFactory.getLogger(DnsMapper.class);

    List<ResolveLog> resolveLog = new ArrayList<>();

    List<String> getIPsByHost(@Param("host") String host);

    /**
     * 通过 hostId 更新 dns, 目前的行为会将该 hostId 下的所有 ip 全部更新为一样的 ip
     *
     * @param hostId hostId
     * @param ip     更新后的 ip
     * @return 更新结果
     */
    Boolean updateDns(@Param("hostId") Integer hostId, @Param("ip") String ip);

    DnsRecord getDnsRecordByHost(String host);

    default List<Integer> countResolveByInterval(int interval, int step) {
        List<Integer> res = new ArrayList<>();
        for (int i = 0; i < interval; i += step) {
            res.add(countResolveByPeriod(i + step, i));
        }
        return res;
    }

    /**
     * 统计 start 分钟前 到 end 分钟之间的解析数量
     *
     * @param start start 分钟前
     * @param end   end 分钟止
     * @return 这个时间段的解析数量
     */
    Integer countResolveByPeriod(@Param("start") Integer start, @Param("end") Integer end);

    /**
     * 保存解析日志, 需要在子线程执行, 以免影响性能
     */
    default void saveLog() {
        runMeasure(() -> {
            Map<String, Integer> logCache = new HashMap<>();
            for (ResolveLog log : resolveLog) {
                // 首先尝试从 logCache 中获取 LogIp 的 id
                Integer ipId = Optional.ofNullable(logCache.get(log.getIp())).orElseGet(() -> {
                    // 若 logCache 中不存在当前 LogIp 的 id, 那就尝试从数据库中获取 LogIp 的 id
                    LogIp logIp = Optional.ofNullable(getResolveIpByIp(log.getIp())).orElseGet(() -> {
                        // 若 还是不存在, 那就插入一条 LogIp 并返回新插入的 LogIp 的 id
                        LogIp t = LogIp.of(log.getIp());
                        insertResolveIp(t);
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
            insertLogBatch(resolveLog);
            synchronized (resolveLog) {
                resolveLog.clear();
            }
        });
    }

    @Override
    default List<String> getIpsByHost(String nKey, String ip) {
        // 记录日志
        synchronized (resolveLog) {
            resolveLog.add(ResolveLog.of(nKey, ip));
        }
        // 实现 泛域名解析
        DnsRecord gDnsRecord = getGDnsRecord(nKey);
        if (gDnsRecord != null) {
            logger.info("检测到泛域名: {} -> {}", nKey, gDnsRecord.getHost());
            return ipChecker(gDnsRecord.getIps());
        }
        // 普通域名解析
        List<String> hosts = getIPsByHost(nKey);
        if (CollectionUtils.isEmpty(hosts)) {
            Host host = new Host(nKey);
            try {
                logger.info("开始递归解析: {}", nKey);
                // 如果没有手动指定 hosts, 那就尝试调用系统 dns 的结果, 只需要 ipv4 的结果
                hosts = Arrays.stream(InetAddress.getAllByName(nKey))
                        .filter(it -> it instanceof Inet4Address)
                        .map(InetAddress::getHostAddress)
                        .map(NetworkUtil::ipChecker)
                        .collect(Collectors.toList());
                // 递归解析后, 将解析结果存入数据库
                if (addHostAndDns(host, hosts)) logger.info("插入 host 与 dns 记录成功, 本次解析结果已缓存");
                return hosts;
            } catch (Exception e) {
                logger.warn("调用系统 dns 出错: {} -> {}", e.getLocalizedMessage(), e.getClass().getName());
                addErrorHost(host);
                hosts = Collections.emptyList();
            }
        }
        return ipChecker(hosts);
    }

    /**
     * 向数据库添加 host 和 dns
     *
     * @param host 要添加的 host, 无 id
     * @param ips  该 host 所对应的 ip
     * @return 添加结果
     */
    default Boolean addHostAndDns(final Host host, List<String> ips) {
        // 添加 host 之前, 先检查 host 是否存在
        Host hostInDB = getHostFromDB(host.getHost());
        // 若不存在, 先走添加 host 流程, 再走添加 dns 流程
        if (hostInDB == null) {
            // 执行了 addHost 后 host 就会有 id
            return addHost(host) && addDns(Dns.of(host, ips));
        }
        // 若存在, 走 添加 dns 流程, hostInDB 里包含 id
        return addDns(Dns.of(hostInDB, ips));
    }

    DnsRecord getGDnsRecord(@Param("host") String host);

    void insertLogBatch(@Param("resolveLog") List<ResolveLog> resolveLog);

    void addErrorHost(Host host);

    LogIp getResolveIpByIp(@Param("ip") String ip);

    void insertResolveIp(LogIp logIp);

    Host getHostFromDB(@Param("host") String host);

    Boolean addHost(Host host);

    Boolean addDns(Dns dns);

}
