package com.mrx.springdnsserver.runner;

import com.mrx.dns.AbsDnsServer;
import com.mrx.dns.recordHandler.DefaultDnsServer;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * @author Mr.X
 * @since 2022-10-30 16:23
 */
@Component
public class DnsServerRunner implements ApplicationRunner {

    @Override
    public void run(ApplicationArguments args) throws Exception {
        DefaultDnsServer dnsServer = DefaultDnsServer.getInstance("x-dns-server");
        dnsServer.start(AbsDnsServer.ServerMode.BIO);
    }

}
