package com.mrx.springdnsserver.model.dns;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

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

}
