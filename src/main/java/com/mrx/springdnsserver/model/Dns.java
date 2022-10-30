package com.mrx.springdnsserver.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author Mr.X
 * @since 2022-10-30 16:39
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
public class Dns {

    private Integer id;

    private Integer hostId;

    private List<String> ips;

    public Dns(Integer hostId, List<String> ips) {
        this.hostId = hostId;
        this.ips = ips;
    }

}
