package com.mrx.springdnsserver;

import com.mrx.springdnsserver.mapper.DnsMapper;
import com.mrx.springdnsserver.model.dns.Dns;
import com.mrx.springdnsserver.model.dns.Host;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
@SpringBootTest
class SpringDnsServerApplicationTests {

    private static final Logger logger = LoggerFactory.getLogger(SpringDnsServerApplicationTests.class);

    @Autowired
    private DnsMapper dnsMapper;

    @Test
    void dnsMapperTest() {
        logger.debug("getIpsByHost: {}", dnsMapper.getIPsByHost("test.com"));
        Host host = new Host();
        host.setHost("t.com");
        logger.debug("addHost: {}", dnsMapper.addHost(host));
        logger.debug("addHost: {}", host);
        Dns dns = new Dns();
        dns.setHostId(host.getId());
        dns.setIps(List.of("1.1.1.1", "2.2.2.2"));
        dnsMapper.addDns(dns);
        dns.setIps(List.of("6.6.6.6", "7.7.7.7"));
        dns.forEach((hostId, ip) -> dnsMapper.updateDns(hostId, ip));
    }

}
