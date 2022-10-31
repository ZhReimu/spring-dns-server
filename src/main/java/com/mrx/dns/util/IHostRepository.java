package com.mrx.dns.util;

import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author Mr.X
 * @since 2022-10-30 17:07
 */
public interface IHostRepository {

    /**
     * 使用 域名 查找 ip, 与 {@link #getIpsByHost(String)} 不同的是, 本方法会计算执行时间<br/>
     * 以及 本方法会去除 dns 请求域名后的 .
     *
     * @param host 要查找 ip 的 域名, 末尾带 .
     * @return 该域名的 ip
     */
    @SneakyThrows
    default List<String> get(String host) {
        // 去除 dns 查询的域名后缀 .
        String nKey = host.substring(0, host.length() - 1);
        return runMeasure(() -> getIpsByHost(nKey));
    }

    /**
     * 使用 域名 查找 ip
     *
     * @param host dns 请求 中的 域名, 末尾不带 .
     * @return 该域名的 ip
     */
    List<String> getIpsByHost(String host);

    default <T> T runMeasure(XSupplier<T> supplier) throws Exception {
        Logger logger = LoggerFactory.getLogger(IHostRepository.class);
        long start = System.currentTimeMillis();
        T res = supplier.get();
        long end = System.currentTimeMillis();
        logger.debug("本次操作耗时: {} ms", end - start);
        return res;
    }

    default void runMeasure(Runnable supplier) {
        Logger logger = LoggerFactory.getLogger(IHostRepository.class);
        long start = System.currentTimeMillis();
        supplier.run();
        long end = System.currentTimeMillis();
        logger.debug("本次操作耗时: {} ms", end - start);
    }

    interface XSupplier<T> {

        T get() throws Exception;

    }

}
