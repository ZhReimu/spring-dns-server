package com.mrx.springdnsserver.runner;

import com.mrx.dns.resolver.chain.impl.DefaultResolverChain;
import com.mrx.dns.resolver.impl.JsonHostResolver;
import com.mrx.dns.server.DefaultDnsServer;
import com.mrx.springdnsserver.config.DnsServerConfig;
import com.mrx.springdnsserver.service.DnsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * @author Mr.X
 * @since 2022-10-30 16:23
 */
@Component
public class DnsServerRunner implements ApplicationRunner {

    private DnsService dnsService;

    private DnsServerConfig serverConfig;

    @Autowired
    public void setRepository(DnsService dnsService) {
        this.dnsService = dnsService;
    }

    @Autowired
    public void setServerConfig(DnsServerConfig serverConfig) {
        this.serverConfig = serverConfig;
    }

    @Override
    public void run(ApplicationArguments args) {
        DnsServerConfig.configHolder = serverConfig;
        DefaultDnsServer dnsServer = DefaultDnsServer.getInstance(
                serverConfig.getName(), serverConfig.getPort(),
                DefaultResolverChain.getInstance(JsonHostResolver.getInstance(), dnsService)
        );
        dnsServer.start(serverConfig.getMode());
    }

}
