package com.mrx.dns.resolver;

import lombok.SneakyThrows;

import java.util.List;

import static com.mrx.dns.util.PerformanceUtil.runMeasure;

/**
 * @author Mr.X
 * @since 2022-10-30 17:07
 */
public interface IResolver {

    /**
     * 使用 域名 查找 ip, 与 {@link #getIpsByHost(String, String)} 不同的是, 本方法会计算执行时间<br/>
     * 以及 本方法会去除 dns 请求域名后的 .
     *
     * @param host 要查找 ip 的 域名, 末尾带 .
     * @param ip   发起请求的客户端的 ip
     * @return 该域名的 ip
     */
    @SneakyThrows
    default List<String> get(String host, String ip) {
        // 去除 dns 查询的域名后缀 .
        String nKey = host.endsWith(".") ? host.substring(0, host.length() - 1) : host;
        return runMeasure(() -> getIpsByHost(nKey, ip));
    }

    /**
     * 使用 域名 查找 ip
     *
     * @param host dns 请求 中的 域名, 末尾不带 .
     * @param ip   发起请求的客户端的 ip
     * @return 该域名的 ip
     */
    List<String> getIpsByHost(String host, String ip);

}
