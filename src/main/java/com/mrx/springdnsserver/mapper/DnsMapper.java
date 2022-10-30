package com.mrx.springdnsserver.mapper;

import com.mrx.dns.util.IHostRepository;
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
            if (nKey.endsWith(gHost)) return getIpsByHostId(host.getId());
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
                    if (addHost(host)) {
                        logger.debug("插入 host 记录成功");
                        Dns dns = new Dns().setHostId(host.getId()).setIps(res);
                        if (addDns(dns)) {
                            logger.debug("插入 dns 记录成功");
                        } else {
                            logger.warn("插入 dns 记录失败: {}", dns);
                        }
                    } else {
                        logger.debug("插入 host 记录失败: {}", host);
                    }
                    logger.debug("本次解析结果已缓存");
                    return res;
                });
            } catch (Exception e) {
                logger.warn("调用系统 dns 出错:", e);
                addErrorHost(host);
                hosts = Collections.emptyList();
            }
        }
        return hosts;
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
