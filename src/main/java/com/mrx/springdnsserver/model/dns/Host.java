package com.mrx.springdnsserver.model.dns;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @author Mr.X
 * @since 2022-10-30 16:39
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
public class Host {

    private Integer id;

    private String host;

    private Long createTime;

    public Host(String host) {
        this.host = host;
    }

    public static Host of(String host) {
        return new Host(host);
    }

}
