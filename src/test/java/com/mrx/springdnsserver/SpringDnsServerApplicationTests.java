package com.mrx.springdnsserver;

import com.mrx.springdnsserver.mapper.DnsHostMapper;
import com.mrx.springdnsserver.mapper.DnsIpMapper;
import com.mrx.springdnsserver.mapper.DnsMapper;
import com.mrx.springdnsserver.model.dns.DnsIp;
import com.mrx.springdnsserver.model.dns.Host;
import com.mrx.springdnsserver.service.DnsService;
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

    @Autowired
    private DnsService dnsService;

    @Autowired
    private DnsIpMapper dnsIpMapper;

    @Autowired
    private DnsHostMapper dnsHostMapper;

    @Test
    public void v2Test() {
        String ip = "1.1.1.1";
        String host = "test.com";
        dnsIpMapper.insertDnsIp(DnsIp.of("1.1.1.1"));
        System.out.println(dnsIpMapper.getDnsIpByIp(ip));
        dnsHostMapper.insertHost(Host.of(host));
        System.out.println(dnsHostMapper.getHostByHost(host));
    }

    @Test
    void countResolveTest() {
        logger.debug("一小时内的解析数量: {}", dnsService.countResolveByInterval(60, 10));
    }

    @Test
    void addHostAndDnsTest() {
        Host host = Host.of("test.com");
        List<String> ips = List.of("127.0.0.1", "1.1.1.1");
        dnsService.addHostAndDns(host, ips);
    }

}
