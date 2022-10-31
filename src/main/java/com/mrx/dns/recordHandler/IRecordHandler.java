package com.mrx.dns.recordHandler;

import com.mrx.dns.server.AbsDnsServer;
import org.xbill.DNS.Message;

import java.net.DatagramPacket;

/**
 * @author Mr.X
 * @since 2022-10-30 09:35
 */
public interface IRecordHandler {

    /**
     * 处理 question 请求
     *
     * @param message 包含 question 的 message
     * @return 返回 true 表示已经处理了本条 message, 不再进行后边的 chain 的调用, false 表示还未处理本条 message
     */
    boolean handleQuestion(Message message);

    /**
     * 当前 handler 是否支持 本次 Question 的 type<br/>
     * 若 不支持, 在 {@link AbsDnsServer#packetHandler(DatagramPacket)} 中不会被调用
     *
     * @param type {@link org.xbill.DNS.Type}
     * @return 支持为 true, 不支持为 false
     */
    @SuppressWarnings("JavadocReference")
    boolean supportType(int type);

}
