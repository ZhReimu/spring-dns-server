package com.mrx.dns.util;

import com.alibaba.fastjson2.JSON;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class HostsHashMap extends HashMap<String, List<String>> {

    private static final Logger logger = LoggerFactory.getLogger(HostsHashMap.class);

    private static final HostsHashMap instance = new HostsHashMap();

    private HostsHashMap() {
        initHosts();
    }

    @SuppressWarnings({"unchecked", "ConstantConditions"})
    private void initHosts() {
        try (InputStream ins = getClass().getResourceAsStream("/hosts.json")) {
            putAll(JSON.parseObject(ins).to(HashMap.class));
            logger.debug("初始化 hosts: {}", this);
        } catch (Exception e) {
            logger.warn("初始化 hosts 失败", e);
        }
    }

    public static HostsHashMap getInstance() {
        return instance;
    }

    @SuppressWarnings("unused")
    public void put(String key, String... value) {
        put(key, List.of(value));
    }

    /**
     * 使用 host 查找对应的 ip
     *
     * @param key host
     * @return 找到的 与 该 host 对应的 ip, 如果真的找不到, 那将返回 {@link Collections#emptyList()}
     */
    public List<String> get(String key) {
        String nKey = key.substring(0, key.length() - 1);
        // key: www.baidu.com.
        // map: *.baidu.com -> 127.0.0.1
        // m.baidu.com, www.baidu.com, baidu.com
        // 实现 泛域名解析
        for (Entry<String, List<String>> entry : entrySet()) {
            String entryKey = entry.getKey();
            if (entryKey.contains("*")) {
                if (entryKey.startsWith("*.")) entryKey = entryKey.replace("*.", "");
                else entryKey = entryKey.replace("*", "");
                if (nKey.endsWith(entryKey)) return entry.getValue();
            }
        }
        List<String> hosts = super.get(nKey);
        if (hosts == null || hosts.isEmpty()) {
            try {
                logger.warn("开始递归解析: {}", nKey);
                // 如果没有手动指定 hosts, 那就尝试调用系统 dns 的结果
                return runMeasure(() -> {
                    List<String> res = Arrays.stream(InetAddress.getAllByName(nKey))
                            .map(InetAddress::getHostAddress)
                            .collect(Collectors.toList());
                    super.put(nKey, res);
                    logger.debug("本次解析结果已缓存");
                    return res;
                });
            } catch (Exception e) {
                logger.warn("调用系统 dns 出错:", e);
                hosts = Collections.emptyList();
            }
        }
        return hosts;
    }

    @SneakyThrows
    private <T> T runMeasure(XSupplier<T> supplier) {
        long start = System.currentTimeMillis();
        T res = supplier.get();
        long end = System.currentTimeMillis();
        logger.debug("本次操作耗时: {} ms", end - start);
        return res;
    }

    private interface XSupplier<T> {

        T get() throws Exception;

    }

}
