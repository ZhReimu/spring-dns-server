package com.mrx.springdnsserver.model.dns;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Mr.X
 * @since 2022-10-31 21:18
 */
@Data
@NoArgsConstructor
public class ResolveLog {

    private Integer id;

    private String host;

    private String ip;

    private Integer ipId;

    private Long createTime;

    public ResolveLog(String host, String ip) {
        this.host = host;
        this.ip = ip;
    }

    public static ResolveLog of(String host, String ip) {
        return new ResolveLog(host, ip);
    }

}
