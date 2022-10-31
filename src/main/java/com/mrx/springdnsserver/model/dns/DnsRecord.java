package com.mrx.springdnsserver.model.dns;

import lombok.Data;

import java.util.List;

/**
 * @author Mr.X
 * @since 2022-10-31 08:00
 */
@Data
public class DnsRecord {

    private Integer id;

    private String host;

    private List<String> ips;

}
