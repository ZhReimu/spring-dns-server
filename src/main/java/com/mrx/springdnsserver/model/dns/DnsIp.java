package com.mrx.springdnsserver.model.dns;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Objects;

/**
 * @author Mr.X
 * @since 2022-11-01 12:43
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
public class DnsIp {

    private Integer id;

    private String ip;

    private Long createTime;

    public DnsIp(String ip) {
        this.ip = ip;
    }

    public static DnsIp of(String ip) {
        return new DnsIp(ip);
    }

    @Override
    public int hashCode() {
        return ip != null ? ip.hashCode() : 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DnsIp dnsIp = (DnsIp) o;
        return Objects.equals(ip, dnsIp.ip);
    }

}
