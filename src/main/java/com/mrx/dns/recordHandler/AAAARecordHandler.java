package com.mrx.dns.recordHandler;

import com.mrx.dns.RecordUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xbill.DNS.Message;
import org.xbill.DNS.Type;

/**
 * @author Mr.X
 * @since 2022-10-30 11:43
 */
public class AAAARecordHandler implements IRecordHandler {

    private static final Logger logger = LoggerFactory.getLogger(AAAARecordHandler.class);

    @Override
    public boolean handleQuestion(Message message) {
        // 暂不支持 ipv6, 直接返回 空响应
        RecordUtil.clearRecord(message);
        logger.trace("handleQuestion:\n{}", message);
        // 处理完毕退出 chain
        return true;
    }

    @Override
    public boolean supportType(int type) {
        return type == Type.AAAA;
    }

}
