package com.mrx.springdnsserver.model.dns;

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

    /**
     * 使用 hostId 和 ip 组成一个 dns 对象
     *
     * @param host 带有 id 的 host 对象
     * @param ip   该 host 对应的 ip
     * @return dns 对象
     */
    public static Dns of(Host host, List<String> ip) {
        return new Dns(host, ip);
    }

    public void forEach(BiConsumer<Integer, String> consumer) {
        for (String ip : ips) consumer.accept(hostId, ip);
    }

}
