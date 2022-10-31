package com.mrx.springdnsserver.model.dns;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Mr.X
 * @since 2022-10-31 22:00
 */
@Data
@NoArgsConstructor
public class LogIp {

    private Integer id;

    private String ip;

    public LogIp(String ip) {
        this.ip = ip;
    }

    public static LogIp of(String ip) {
        return new LogIp(ip);
    }

}
