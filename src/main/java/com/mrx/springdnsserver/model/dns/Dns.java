package com.mrx.springdnsserver.model.dns;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

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

    private List<Integer> ipIds;

    public Dns(Host host, List<Integer> ip) {
        this(host.getId(), ip);
    }

    public Dns(Integer hostId, List<Integer> ipIds) {
        this.hostId = hostId;
        this.ipIds = ipIds;
    }

    /**
     * 使用 hostId 和 ip 组成一个 dns 对象
     *
     * @param host 带有 id 的 host 对象
     * @param ip   该 host 对应的 ip
     * @return dns 对象
     */
    public static Dns of(Host host, List<DnsIp> ip) {
        return new Dns(host, ip.stream().map(DnsIp::getId).collect(Collectors.toList()));
    }

    public void forEach(BiConsumer<Integer, Integer> consumer) {
        for (Integer ipId : ipIds) consumer.accept(hostId, ipId);
    }

}
