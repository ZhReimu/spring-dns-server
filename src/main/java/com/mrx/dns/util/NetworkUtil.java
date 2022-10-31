package com.mrx.dns.util;

import com.mrx.springdnsserver.config.DnsServerConfig;
import com.mrx.springdnsserver.mapper.DnsMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author Mr.X
 * @since 2022-10-30 21:22
 */
public class NetworkUtil {

    private static final Logger logger = LoggerFactory.getLogger(NetworkUtil.class);

    private static final String cfIP = DnsServerConfig.configHolder.getCfip();

    private static final List<String> CF_IPS = List.of(
            "173.245.48.0/20",
            "103.21.244.0/22",
            "103.22.200.0/22",
            "103.31.4.0/22",
            "141.101.64.0/18",
            "108.162.192.0/18",
            "190.93.240.0/20",
            "188.114.96.0/20",
            "197.234.240.0/22",
            "198.41.128.0/17",
            "162.158.0.0/15",
            "104.16.0.0/13",
            "104.24.0.0/14",
            "172.64.0.0/13",
            "131.0.72.0/22"
    );

    public static boolean isInRange(String ip, String cidr) {
        try {
            String[] ips = ip.split("\\.");
            int ipAddr = (Integer.parseInt(ips[0]) << 24)
                    | (Integer.parseInt(ips[1]) << 16)
                    | (Integer.parseInt(ips[2]) << 8) | Integer.parseInt(ips[3]);
            int type = Integer.parseInt(cidr.replaceAll(".*/", ""));
            int mask = 0xFFFFFFFF << (32 - type);
            String cidrIp = cidr.replaceAll("/.*", "");
            String[] cidrIps = cidrIp.split("\\.");
            int cidrIpAddr = (Integer.parseInt(cidrIps[0]) << 24)
                    | (Integer.parseInt(cidrIps[1]) << 16)
                    | (Integer.parseInt(cidrIps[2]) << 8)
                    | Integer.parseInt(cidrIps[3]);
            return (ipAddr & mask) == (cidrIpAddr & mask);
        } catch (Exception e) {
            logger.warn("出现异常:", e);
        }
        return false;
    }

    public static boolean isInCFips(String ip) {
        for (String cidr : CF_IPS) {
            if (isInRange(ip, cidr)) return true;
        }
        return false;
    }

    public static String ipChecker(String ip) {
        if (isInCFips(ip)) {
            logger.info("检测到 cloud-flare ip, 自动替换为当前设置的 最优 ip: {}", cfIP);
            return cfIP;
        }
        return ip;
    }

    /**
     * 检测 ips 中的 ip 是否为 cloudflare ip, 如果是, 那就将其替换为 当前设置的最优 cloudflare ip {@link DnsServerConfig#getCfip()}<br/>
     * 若不是, 那就返回参数中的 ips
     *
     * @param ips 要检测的 ip
     * @return 检测完毕的 ip
     */
    public static List<String> ipChecker(List<String> ips) {
        Logger logger = LoggerFactory.getLogger(DnsMapper.class);
        List<String> cfIPList = List.of(cfIP);
        if (ips.stream().anyMatch(NetworkUtil::isInCFips)) {
            logger.info("检测到 cloud-flare ip, 自动替换为当前设置的 最优 ip: {}", cfIPList);
            return cfIPList;
        }
        return ips;
    }

}
