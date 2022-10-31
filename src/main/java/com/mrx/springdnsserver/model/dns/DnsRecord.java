package com.mrx.springdnsserver.model.dns;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author Mr.X
 * @since 2022-10-31 08:00
 */
@Data
@Accessors(chain = true)
public class DnsRecord {

    /**
     * host 表中的 id
     */
    private Integer id;

    /**
     * host 表中的 host
     */
    private String host;

    /**
     * dns 表中的 ip
     */
    private List<String> ips;

}
