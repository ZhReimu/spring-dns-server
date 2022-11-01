package com.mrx.springdnsserver.mapper;

import com.mrx.springdnsserver.model.dns.Dns;
import com.mrx.springdnsserver.model.dns.DnsRecord;
import com.mrx.springdnsserver.model.dns.Host;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author Mr.X
 * @since 2022-10-30 16:27
 */
@Mapper
public interface DnsMapper {

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

    DnsRecord getGDnsRecord(@Param("host") String host);

    Host getHostFromDB(@Param("host") String host);

    Boolean addHost(Host host);

    Boolean addDns(Dns dns);

}
