package com.mrx.springdnsserver.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.function.BiConsumer;

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

    public Dns(Host host, List<String> ip) {
        this(host.getId(), ip);
    }

    public static Dns of(Host host, List<String> ip) {
        return new Dns(host, ip);
    }

    public void forEach(BiConsumer<Integer, String> consumer) {
        for (String ip : ips) consumer.accept(hostId, ip);
    }

}
