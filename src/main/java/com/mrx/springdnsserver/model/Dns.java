package com.mrx.springdnsserver.model;

import lombok.Data;

import java.util.List;

/**
 * @author Mr.X
 * @since 2022-10-30 16:39
 */
@Data
public class Dns {

    private Integer id;

    private Integer hostId;

    private List<String> ips;

}
