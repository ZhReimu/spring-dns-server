package com.mrx.dns.resolver.chain.impl;

import com.mrx.dns.repository.IHostRepository;
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

    private final List<IHostRepository> resolvers = new ArrayList<>();

    public static DefaultResolverChain getInstance(IHostRepository... resolvers) {
        instance.addResolver(resolvers);
        return instance;
    }

    @Override
    public void addResolver(IHostRepository... resolvers) {
        this.resolvers.addAll(List.of(resolvers));
    }

    @Override
    public List<String> getIpsByHost(String host, String ip) {
        for (IHostRepository resolver : resolvers) {
            List<String> hostList = resolver.getIpsByHost(host, ip);
            if (!CollectionUtils.isEmpty(hostList)) return hostList.stream()
                    .distinct()
                    .map(NetworkUtil::ipChecker)
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

}
