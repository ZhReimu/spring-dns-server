package com.mrx.springdnsserver.mapper;

import com.mrx.dns.util.IHostRepository;
import com.mrx.dns.util.NetworkUtil;
import com.mrx.springdnsserver.config.DnsServerConfig;
import com.mrx.springdnsserver.model.Dns;
import com.mrx.springdnsserver.model.Host;
import org.apache.ibatis.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    @Select("SELECT ip FROM tb_dns WHERE host_id = (SELECT id FROM tb_host WHERE host = #{host})")
    List<String> getIPsByHost(@Param("host") String host);

    @Override
    default List<String> getIpsByHost(String nKey) {
        Logger logger = LoggerFactory.getLogger(DnsMapper.class);
        // 实现 泛域名解析
        for (Host host : listGenericDomains()) {
            String gHost = host.getHost();
            gHost = gHost.startsWith("*.") ? gHost.replace("*.", "") :
                    gHost.replace("*", "");
            if (nKey.endsWith(gHost)) {
                return ipChecker(getIpsByHostId(host.getId()));
            }
        }
        // 普通域名解析
        List<String> hosts = getIPsByHost(nKey);
        if (hosts == null || hosts.isEmpty()) {
            Host host = new Host(nKey);
            try {
                logger.warn("开始递归解析: {}", nKey);
                // 如果没有手动指定 hosts, 那就尝试调用系统 dns 的结果
                return runMeasure(() -> {
                    List<String> res = Arrays.stream(InetAddress.getAllByName(nKey))
                            .map(InetAddress::getHostAddress)
                            .collect(Collectors.toList());
                    // 递归解析后, 将解析结果存入数据库
                    if (addHost(host)) {
                        logger.debug("插入 host 记录成功");
                        Dns dns = new Dns(host.getId(), ipChecker(res));
                        if (addDns(dns)) {
                            logger.debug("插入 dns 记录成功");
                        } else {
                            logger.warn("插入 dns 记录失败: {}", dns);
                        }
                    } else {
                        logger.debug("插入 host 记录失败: {}", host);
                    }
                    logger.debug("本次解析结果已缓存");
                    return ipChecker(res);
                });
            } catch (Exception e) {
                logger.warn("调用系统 dns 出错:", e);
                addErrorHost(host);
                hosts = Collections.emptyList();
            }
        }
        return ipChecker(hosts);
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
            logger.debug("检测到 cloud-flare ip, 自动替换为当前设置的 最优 ip: {}", cfIP);
            return cfIP;
        }
        return ips;
    }

    @Select("SELECT id,host FROM tb_host WHERE host LIKE '%*%'")
    List<Host> listGenericDomains();

    @Insert("INSERT INTO tb_host(host) VALUES (#{host})")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    Boolean addHost(Host host);

    @Insert("<script> " +
            "INSERT INTO tb_dns(host_id, ip) VALUES " +
            "<foreach collection=\"ips\" item=\"ip\" separator=\",\" > " +
            "        (#{hostId},#{ip})" +
            " </foreach>" +
            "</script>")
    Boolean addDns(Dns dns);

    @Insert("INSERT INTO tb_host_error(host) VALUES (#{host})")
    void addErrorHost(Host host);

    @Select("SELECT ip FROM tb_dns WHERE host_id = #{hostId}")
    List<String> getIpsByHostId(@Param("hostId") Integer hostId);

}
