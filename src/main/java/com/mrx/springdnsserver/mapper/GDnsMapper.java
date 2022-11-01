package com.mrx.springdnsserver.mapper;

import com.mrx.springdnsserver.model.dns.DnsRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @author Mr.X
 * @since 2022-11-01 15:55
 */
@Mapper
public interface GDnsMapper {

    DnsRecord getGDnsRecord(@Param("host") String host);

}
