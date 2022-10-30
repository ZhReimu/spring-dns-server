package com.mrx.springdnsserver.model;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author Mr.X
 * @since 2022-10-30 16:39
 */
@Data
@Accessors(chain = true)
public class Host {

    private Integer id;

    private String host;

}
