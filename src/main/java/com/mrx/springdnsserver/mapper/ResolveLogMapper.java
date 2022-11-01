package com.mrx.springdnsserver.mapper;

import com.mrx.springdnsserver.model.dns.LogIp;
import com.mrx.springdnsserver.model.dns.ResolveLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.*;

import static com.mrx.dns.util.PerformanceUtil.runMeasure;

/**
 * @author Mr.X
 * @since 2022-11-01 11:10
 */
@Mapper
public interface ResolveLogMapper {

    List<ResolveLog> resolveLog = new ArrayList<>();

    /**
     * 保存解析日志, 需要在子线程执行, 以免影响性能
     */
    default void saveLog() {
        runMeasure(() -> {
            Map<String, Integer> logCache = new HashMap<>();
            for (ResolveLog log : resolveLog) {
                // 首先尝试从 logCache 中获取 LogIp 的 id
                Integer ipId = Optional.ofNullable(logCache.get(log.getIp())).orElseGet(() -> {
                    // 若 logCache 中不存在当前 LogIp 的 id, 那就尝试从数据库中获取 LogIp 的 id
                    LogIp logIp = Optional.ofNullable(getResolveIpByIp(log.getIp())).orElseGet(() -> {
                        // 若 还是不存在, 那就插入一条 LogIp 并返回新插入的 LogIp 的 id
                        LogIp t = LogIp.of(log.getIp());
                        insertResolveIp(t);
                        return t;
                    });
                    synchronized (logCache) {
                        // 使用数据库中的 LogIp 信息填充 logCache
                        logCache.put(logIp.getIp(), logIp.getId());
                    }
                    // 返回 数据库中的 LogIp 的 id
                    return logIp.getId();
                });
                // 更新此条数据的 ipId
                log.setIpId(ipId);
            }
            // 插入 解析日志
            insertLogBatch(resolveLog);
            synchronized (resolveLog) {
                resolveLog.clear();
            }
        });
    }

    LogIp getResolveIpByIp(@Param("ip") String ip);

    void insertResolveIp(LogIp logIp);

    void insertLogBatch(@Param("resolveLog") List<ResolveLog> resolveLog);

    default List<Integer> countResolveByInterval(int interval, int step) {
        List<Integer> res = new ArrayList<>();
        for (int i = 0; i < interval; i += step) {
            res.add(countResolveByPeriod(i + step, i));
        }
        return res;
    }

    /**
     * 统计 start 分钟前 到 end 分钟之间的解析数量
     *
     * @param start start 分钟前
     * @param end   end 分钟止
     * @return 这个时间段的解析数量
     */
    Integer countResolveByPeriod(@Param("start") Integer start, @Param("end") Integer end);

}
