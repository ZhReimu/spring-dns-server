package com.mrx.dns.resolver.chain.impl;

import com.mrx.dns.resolver.IResolver;
import com.mrx.dns.resolver.chain.IResolverChain;
import com.mrx.dns.util.NetworkUtil;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Mr.X
 * @since 2022-10-31 16:42
 */
public class DefaultResolverChain implements IResolverChain {

    private static final DefaultResolverChain instance = new DefaultResolverChain();

    private final List<IResolver> resolvers = new ArrayList<>();

    public static DefaultResolverChain getInstance(IResolver... resolvers) {
        instance.addResolver(resolvers);
        return instance;
    }

    @Override
    public void addResolver(IResolver... resolvers) {
        this.resolvers.addAll(List.of(resolvers));
    }

    @Override
    public List<String> getIpsByHost(String host, String ip) {
        for (IResolver resolver : resolvers) {
            List<String> hostList = resolver.getIpsByHost(host, ip);
            if (!CollectionUtils.isEmpty(hostList)) return hostList.stream()
                    .distinct()
                    .map(NetworkUtil::ipChecker)
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

}
