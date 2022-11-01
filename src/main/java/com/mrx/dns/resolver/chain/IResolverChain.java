package com.mrx.dns.resolver.chain;

import com.mrx.dns.resolver.IResolver;
import lombok.SneakyThrows;

import java.util.List;

import static com.mrx.dns.util.PerformanceUtil.runMeasure;

/**
 * @author Mr.X
 * @since 2022-11-01 11:40
 */
public interface IResolverChain {

    void addResolver(IResolver... resolvers);

    @SneakyThrows
    default List<String> get(String host, String ip) {
        // 去除 dns 查询的域名后缀 .
        String nKey = host.endsWith(".") ? host.substring(0, host.length() - 1) : host;
        return runMeasure(() -> getIpsByHost(nKey, ip));
    }

    List<String> getIpsByHost(String host, String ip);

}
