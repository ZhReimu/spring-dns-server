package com.mrx.springdnsserver.mapper;

import com.mrx.dns.util.IHostRepository;
import com.mrx.dns.util.NetworkUtil;
import com.mrx.springdnsserver.config.DnsServerConfig;
import com.mrx.springdnsserver.model.dns.Dns;
import com.mrx.springdnsserver.model.dns.DnsRecord;
import com.mrx.springdnsserver.model.dns.Host;
import org.apache.ibatis.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Mr.X
 * @since 2022-10-30 16:27
 */
@Mapper
public interface DnsMapper extends IHostRepository {

    Logger logger = LoggerFactory.getLogger(DnsMapper.class);

    @Select("SELECT ip FROM tb_dns WHERE host_id = (SELECT id FROM tb_host WHERE host = #{host})")
    List<String> getIPsByHost(@Param("host") String host);

    /**
     * 通过 hostId 更新 dns, 目前的行为会将该 hostId 下的所有 ip 全部更新为一样的 ip
     *
     * @param hostId hostId
     * @param ip     更新后的 ip
     * @return 更新结果
     */
    @Update("UPDATE tb_dns SET ip = ip WHERE host_id = #{hostId}")
    Boolean updateDns(@Param("hostId") Integer hostId, @Param("ip") String ip);

    DnsRecord getDnsRecordByHost(String host);

    @Override
    default List<String> getIpsByHost(String nKey) {
        // 记录 解析日志
        // Optional.ofNullable(checkHostExists(nKey)).ifPresent(this::insertLog);
        // 实现 泛域名解析
        DnsRecord gDnsRecord = getGDnsRecord(nKey);
        if (gDnsRecord != null) {
            logger.info("检测到泛域名: {} -> {}", nKey, gDnsRecord.getHost());
            return ipChecker(gDnsRecord.getIps());
        }
        // 普通域名解析
        List<String> hosts = getIPsByHost(nKey);
        if (hosts == null || hosts.isEmpty()) {
            Host host = new Host(nKey);
            try {
                logger.info("开始递归解析: {}", nKey);
                // 如果没有手动指定 hosts, 那就尝试调用系统 dns 的结果, 只需要 ipv4 的结果
                List<String> res = Arrays.stream(InetAddress.getAllByName(nKey))
                        .filter(it -> it instanceof Inet4Address)
                        .map(InetAddress::getHostAddress)
                        .map(this::ipChecker)
                        .collect(Collectors.toList());
                // 递归解析后, 将解析结果存入数据库
                if (addHostAndDns(host, res)) logger.info("插入 host 与 dns 记录成功, 本次解析结果已缓存");
                return res;
            } catch (Exception e) {
                logger.warn("调用系统 dns 出错: {} -> {}", e.getLocalizedMessage(), e.getClass().getName());
                addErrorHost(host);
                hosts = Collections.emptyList();
            }
        }
        return ipChecker(hosts);
    }

    DnsRecord getGDnsRecord(@Param("host") String host);

    default String ipChecker(String ip) {
        String cfIP = DnsServerConfig.configHolder.getCfip();
        if (NetworkUtil.isInCFips(ip)) {
            logger.info("检测到 cloud-flare ip, 自动替换为当前设置的 最优 ip: {}", cfIP);
            return cfIP;
        }
        return ip;
    }

    /**
     * 检测 ips 中的 ip 是否为 cloudflare ip, 如果是, 那就将其替换为 当前设置的最优 cloudflare ip {@link DnsServerConfig#getCfip()}<br/>
     * 若不是, 那就返回参数中的 ips
     *
     * @param ips 要检测的 ip
     * @return 检测完毕的 ip
     */
    default List<String> ipChecker(List<String> ips) {
        Logger logger = LoggerFactory.getLogger(DnsMapper.class);
        List<String> cfIP = List.of(DnsServerConfig.configHolder.getCfip());
        if (ips.stream().anyMatch(NetworkUtil::isInCFips)) {
            logger.info("检测到 cloud-flare ip, 自动替换为当前设置的 最优 ip: {}", cfIP);
            return cfIP;
        }
        return ips;
    }

    @SuppressWarnings("unused") // mapper.xml 里用到了
    @Select("SELECT ip FROM tb_generic_dns WHERE host_id = #{hostId}")
    List<String> getGHostIpsByGHost(@Param("hostId") Integer hostId);

    /**
     * 向数据库添加 host 和 dns
     *
     * @param host 要添加的 host, 无 id
     * @param ips  该 host 所对应的 ip
     * @return 添加结果
     */
    default Boolean addHostAndDns(final Host host, List<String> ips) {
        // 添加 host 之前, 先检查 host 是否存在
        Host hostInDB = checkHostExists(host.getHost());
        // 若不存在, 先走添加 host 流程, 再走添加 dns 流程
        if (hostInDB == null) {
            // 执行了 addHost 后 host 就会有 id
            return addHost(host) && addDns(Dns.of(host, ips));
        }
        // 若存在, 走 添加 dns 流程, hostInDB 里包含 id
        return addDns(Dns.of(hostInDB, ips));
    }

    @Insert("INSERT INTO tb_host_error(host) VALUES (#{host})")
    void addErrorHost(Host host);

    @Select("SELECT * FROM tb_host WHERE host = #{host} LIMIT 1")
    Host checkHostExists(@Param("host") String host);

    @Insert("INSERT INTO tb_host(host) VALUES (#{host})")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    Boolean addHost(Host host);

    Boolean addDns(Dns dns);

}
