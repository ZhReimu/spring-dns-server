package com.mrx.dns.recordHandler;

import com.mrx.dns.resolver.DefaultResolver;
import com.mrx.dns.util.RecordUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xbill.DNS.Message;
import org.xbill.DNS.Name;
import org.xbill.DNS.Section;
import org.xbill.DNS.Type;

import java.util.List;

/**
 * @author Mr.X
 * @since 2022-10-30 09:36
 */
public class ARecordHandler implements IRecordHandler {

    private static final Logger logger = LoggerFactory.getLogger(ARecordHandler.class);

    private final DefaultResolver resolver;

    public ARecordHandler(DefaultResolver resolver) {
        this.resolver = resolver;
    }

    @Override
    public boolean handleQuestion(Message message, String host) {
        Name name = message.getQuestion().getName();
        List<String> answers = resolver.get(name.toString(), host);
        logger.debug("解析域名: {} -> {}", name, answers);
        if (answers.isEmpty()) {
            // 如果 响应内容 为空, 那就返回 空响应
            RecordUtil.clearRecord(message);
        } else {
            answers.forEach(it -> message.addRecord(RecordUtil.newARecord(name, it), Section.ANSWER));
        }
        logger.trace("handleQuestion:\n{}", message);
        // 处理完毕, 退出 chain
        return true;
    }

    @Override
    public boolean supportType(int type) {
        return type == Type.A;
    }

}
