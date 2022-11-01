package com.mrx.springdnsserver.controller;

import com.mrx.springdnsserver.mapper.DnsMapper;
import com.mrx.springdnsserver.model.result.Result;
import com.mrx.springdnsserver.service.DnsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.Positive;
import java.util.List;
import java.util.Optional;

/**
 * @author Mr.X
 * @since 2022-10-31 07:04
 */
@Validated
@RestController
public class DnsController {

    private static final Logger logger = LoggerFactory.getLogger(DnsController.class);

    private DnsMapper dnsMapper;

    private DnsService dnsService;

    @Autowired
    public void setDnsService(DnsService dnsService) {
        this.dnsService = dnsService;
    }

    @Autowired
    public void setDnsMapper(DnsMapper dnsMapper) {
        this.dnsMapper = dnsMapper;
    }

    @GetMapping("/update")
    public Result<?> updateDns(
            @RequestParam String host, @RequestParam(required = false) String action,
            @RequestParam List<String> ip
    ) {
        // logger.debug("updateDns: {} -> {}", host, ip);
        // Host hostInDB = dnsMapper.getHostFromDB(host);
        // logger.debug("hostInDB: {}", hostInDB);
        // // 如果数据库中已存在 host 记录, 那就只管更新 dns
        // if (hostInDB != null && !ip.isEmpty()) {
        //     if ("update".equals(action) && ip.size() == 1) {
        //         return dnsMapper.updateDns(hostInDB.getId(), ip.get(0)) ? Result.success() : Result.fail();
        //     }
        //     return dnsMapper.addDns(Dns.of(hostInDB, ip)) ? Result.success() : Result.fail();
        // }
        // // 若数据库中不存在 host 记录, 那就先插入 host 记录, 再 插入 dns 记录
        // return dnsService.addHostAndDns(Host.of(host), ip) ? Result.success() : Result.fail();
        return Result.fail();
    }

    @GetMapping("/query")
    public Result<?> queryDns(@RequestParam String host) {
        return Result.success(
                Optional.ofNullable(dnsService.getGDnsRecord(host).setHost(host))
                        .orElse(dnsMapper.getDnsRecordByHost(host))
        );
    }

    @GetMapping("/info")
    public Result<?> getResolveInfo(@RequestParam @Positive(message = "interval 必须为 正整数") Integer interval) {
        return Result.success(dnsService.countResolveByInterval(interval, 10));
    }

}
