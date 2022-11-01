package com.mrx.springdnsserver.mapper;

import com.mrx.springdnsserver.model.dns.LogIp;
import com.mrx.springdnsserver.model.dns.ResolveLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author Mr.X
 * @since 2022-11-01 11:10
 */
@Mapper
public interface ResolveLogMapper {

    LogIp getResolveIpByIp(@Param("ip") String ip);

    void insertResolveIp(LogIp logIp);

    void insertLogBatch(@Param("resolveLog") List<ResolveLog> resolveLog);

    /**
     * 统计 start 分钟前 到 end 分钟之间的解析数量
     *
     * @param start start 分钟前
     * @param end   end 分钟止
     * @return 这个时间段的解析数量
     */
    Integer countResolveByPeriod(@Param("start") Integer start, @Param("end") Integer end);

}
