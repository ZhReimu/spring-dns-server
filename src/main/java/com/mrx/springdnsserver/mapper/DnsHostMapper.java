package com.mrx.springdnsserver.mapper;

import com.mrx.springdnsserver.model.dns.Host;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @author Mr.X
 * @since 2022-11-01 12:37
 */
@Mapper
public interface DnsHostMapper {

    Host getHostByHost(@Param("host") String host);

    void insertHost(Host host);

}
