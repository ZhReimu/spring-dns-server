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
     * 使用 域名 查找 ip
     *
     * @param key dns 请求 中的 域名, 末尾有 .
     * @return 该域名的 ip
     */
    List<String> get(String key);

    @SneakyThrows
    default <T> T runMeasure(XSupplier<T> supplier) {
        Logger logger = LoggerFactory.getLogger(IHostRepository.class);
        long start = System.currentTimeMillis();
        T res = supplier.get();
        long end = System.currentTimeMillis();
        logger.debug("本次操作耗时: {} ms", end - start);
        return res;
    }

    interface XSupplier<T> {

        T get() throws Exception;

    }

}
