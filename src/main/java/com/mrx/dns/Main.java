package com.mrx.dns;

import com.mrx.dns.recordHandler.DefaultDnsServer;
import com.mrx.dns.resolver.Resolver;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Mr.X
 * @since 2022-10-30 8:34
 */
public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    @SneakyThrows
    public static void main(String[] args) {
        DefaultDnsServer dnsServer = DefaultDnsServer.getInstance("x-dns-server", 53, Resolver.getInstance());
        dnsServer.start(AbsDnsServer.ServerMode.BIO);
        logger.debug("程序已启动");
    }

}