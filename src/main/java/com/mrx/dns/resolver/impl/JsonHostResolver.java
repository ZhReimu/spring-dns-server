package com.mrx.dns.resolver.impl;

import com.mrx.dns.repository.HostsMap;
import com.mrx.dns.repository.IHostRepository;

import java.util.List;

/**
 * @author Mr.X
 * @since 2022-10-31 16:52
 */
public class JsonHostResolver implements IHostRepository {

    private static final IHostRepository hostsMap = HostsMap.getInstance();

    private static final JsonHostResolver instance = new JsonHostResolver();

    private JsonHostResolver() {
    }

    public static JsonHostResolver getInstance() {
        return instance;
    }

    @Override
    public List<String> getIpsByHost(String host, String ip) {
        // 优先使用 host.json 中的内容解析
        return hostsMap.getIpsByHost(host, ip);
    }

}
