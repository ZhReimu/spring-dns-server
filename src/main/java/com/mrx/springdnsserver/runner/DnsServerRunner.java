package com.mrx.springdnsserver.runner;

import com.mrx.dns.AbsDnsServer;
import com.mrx.dns.recordHandler.DefaultDnsServer;
import com.mrx.dns.util.IHostRepository;
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

    @Autowired
    public void setRepository(IHostRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(ApplicationArguments args) {
        DefaultDnsServer dnsServer = DefaultDnsServer.getInstance("x-dns-server", repository);
        dnsServer.start(AbsDnsServer.ServerMode.BIO);
    }

}
