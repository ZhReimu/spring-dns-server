package com.mrx.springdnsserver.config;

import com.mrx.springdnsserver.mapper.ResolveLogMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import static com.mrx.springdnsserver.mapper.ResolveLogMapper.resolveLog;

/**
 * @author Mr.X
 * @since 2022-10-31 14:16
 */
@Component
public class ResolveLogKeeper {

    private static final Logger logger = LoggerFactory.getLogger(ResolveLogKeeper.class);

    private ResolveLogMapper mapper;

    @Autowired
    public void setMapper(ResolveLogMapper mapper) {
        this.mapper = mapper;
    }

    @Scheduled(cron = "0 0/2 * * * ?")
    public void saveLog() {
        logger.debug("计划任务, 保存 resolveLog: {}", resolveLog.size());
        if (!resolveLog.isEmpty()) mapper.saveLog();
    }

}
