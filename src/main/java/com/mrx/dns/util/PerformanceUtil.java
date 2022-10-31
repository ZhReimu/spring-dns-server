package com.mrx.dns.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Mr.X
 * @since 2022-10-31 16:43
 */
public class PerformanceUtil {


    public static <T> T runMeasure(XSupplier<T> supplier) throws Exception {
        Logger logger = LoggerFactory.getLogger(IHostRepository.class);
        long start = System.currentTimeMillis();
        T res = supplier.get();
        long end = System.currentTimeMillis();
        logger.debug("本次操作耗时: {} ms", end - start);
        return res;
    }

    public static void runMeasure(Runnable supplier) {
        Logger logger = LoggerFactory.getLogger(IHostRepository.class);
        long start = System.currentTimeMillis();
        supplier.run();
        long end = System.currentTimeMillis();
        logger.debug("本次操作耗时: {} ms", end - start);
    }

    public interface XSupplier<T> {

        T get() throws Exception;

    }


}
