package com.mrx.springdnsserver;

import com.mrx.springdnsserver.config.DnsServerConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
@EnableConfigurationProperties({DnsServerConfig.class})
public class SpringDnsServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringDnsServerApplication.class, args);
    }

}
