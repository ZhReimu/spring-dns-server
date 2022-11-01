package com.mrx.dns.resolver.chain;

import com.mrx.dns.resolver.IResolver;

/**
 * @author Mr.X
 * @since 2022-11-01 11:40
 */
public interface IResolverChain extends IResolver {

    void addResolver(IResolver... resolvers);

}
