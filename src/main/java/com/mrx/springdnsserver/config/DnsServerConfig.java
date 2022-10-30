package com.mrx.springdnsserver.config;

import com.mrx.dns.AbsDnsServer;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Mr.X
 * @since 2022-10-30 18:29
 */
@Data
@ConfigurationProperties(prefix = "dns")
public class DnsServerConfig {

    /**
     * Dns 服务器使用的 端口号
     */
    private Integer port = 53;

    /**
     * Dns 服务器名称
     */
    private String name = "x-dns-server";

    /**
     * Dns 服务器启动模式
     */
    private AbsDnsServer.ServerMode mode = AbsDnsServer.ServerMode.BIO;

    public static DnsServerConfig configHolder;

    /**
     * 当前最好的 CloudFlareIP
     */
    private String cfip = "172.67.186.61";

}
