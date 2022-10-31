package com.mrx.springdnsserver.runner;

import com.mrx.dns.recordHandler.DefaultDnsServer;
import com.mrx.dns.repository.IHostRepository;
import com.mrx.springdnsserver.config.DnsServerConfig;
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

    private IHostRepository repository;

    private DnsServerConfig serverConfig;

    @Autowired
    public void setRepository(IHostRepository repository) {
        this.repository = repository;
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
                repository
        );
        dnsServer.start(serverConfig.getMode());
    }

}
