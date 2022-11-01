package com.mrx.springdnsserver.mapper;

import com.mrx.springdnsserver.model.dns.Host;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author Mr.X
 * @since 2022-11-01 15:41
 */
@Mapper
public interface ErrorHostMapper {

    void insertOrUpdateErrorHost(Host host);

}
