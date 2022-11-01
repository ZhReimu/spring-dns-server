package com.mrx.dns.resolver.impl;

import com.alibaba.fastjson2.JSON;
import com.mrx.dns.resolver.IResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.CollectionUtils;

import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 只有 host 解析的 {@link IResolver}
 *
 * @author Mr.X
 * @see MemoryHostsResolver
 * @since 2022-10-31 16:22
 */
public class JsonHostsResolver implements IResolver {

    private static final Logger logger = LoggerFactory.getLogger(JsonHostsResolver.class);

    private static final JsonHostsResolver instance = new JsonHostsResolver();

    private final Map<String, List<String>> map = new HashMap<>();

    private JsonHostsResolver() {
        initHosts();
    }

    @SuppressWarnings({"unchecked"})
    private void initHosts() {
        try (InputStream ins = new ClassPathResource("hosts.json").getInputStream()) {
            map.putAll(JSON.parseObject(ins).to(HashMap.class));
            logger.debug("初始化 {} 个 hosts", map.size());
        } catch (Exception e) {
            logger.warn("初始化 hosts 失败: {}", e.getLocalizedMessage());
        }
    }

    public static JsonHostsResolver getInstance() {
        return instance;
    }

    /**
     * 使用 host 查找对应的 ip
     *
     * @param host host ( 域名 )
     * @param ip   调用者的 ip
     * @return 找到的 与 该 host 对应的 ip, 如果真的找不到, 那将返回 {@link Collections#emptyList()}
     */
    @SuppressWarnings("DuplicatedCode")
    public List<String> getIpsByHost(String host, String ip) {
        logger.trace("使用 hosts.json 解析: {}", host);
        // 实现 泛域名解析
        for (Map.Entry<String, List<String>> entry : map.entrySet()) {
            String entryKey = entry.getKey();
            if (entryKey.contains("*")) {
                if (entryKey.startsWith("*.")) entryKey = entryKey.replace("*.", "");
                else entryKey = entryKey.replace("*", "");
                if (host.endsWith(entryKey)) return entry.getValue();
            }
        }
        List<String> hosts = map.get(host);
        return CollectionUtils.isEmpty(hosts) ? Collections.emptyList() : hosts;
    }

}
