package com.mrx.springdnsserver.mapper;

import com.mrx.springdnsserver.model.dns.DnsIp;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author Mr.X
 * @since 2022-11-01 12:36
 */
@Mapper
public interface DnsIpMapper {

    DnsIp getDnsIpByIp(@Param("ip") String ip);

    List<DnsIp> listDnsIpByIpd(@Param("ips") List<String> ips);

    void insertDnsIp(@Param("ip") DnsIp ip);

    void insertDnsIpBatch(@Param("ips") List<DnsIp> ips);

}
