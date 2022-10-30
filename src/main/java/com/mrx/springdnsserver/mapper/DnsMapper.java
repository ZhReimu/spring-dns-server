package com.mrx.springdnsserver.mapper;

import com.mrx.dns.util.IHostRepository;
import com.mrx.springdnsserver.model.Dns;
import com.mrx.springdnsserver.model.Host;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * @author Mr.X
 * @since 2022-10-30 16:27
 */
@Mapper
public interface DnsMapper extends IHostRepository {

    @Select("SELECT ip FROM tb_dns WHERE host_id = (SELECT id FROM tb_host WHERE host = #{host})")
    List<String> getIPsByHost(@Param("host") String host);

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

    @Override
    default List<String> get(String key) {
        String nKey = key.substring(0, key.length() - 1);
        return getIPsByHost(nKey);
    }

}
