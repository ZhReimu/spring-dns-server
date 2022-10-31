package com.mrx.dns.server;

import com.mrx.dns.recordHandler.AAAARecordHandler;
import com.mrx.dns.recordHandler.ARecordHandler;
import com.mrx.dns.recordHandler.IRecordHandler;
import com.mrx.dns.resolver.Resolver;
import com.mrx.dns.util.RecordUtil;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xbill.DNS.*;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Mr.X
 * @since 2022-10-30 09:22
 */
public class DefaultDnsServer extends AbsDnsServer {

    private static final Logger logger = LoggerFactory.getLogger(DefaultDnsServer.class);

    private static volatile DefaultDnsServer dnsServer;

    private static final List<IRecordHandler> recordHandlerChain = new ArrayList<>();

    private static final IRecordHandler nsRecordHandler = new NSRecordHandler();

    private static final IRecordHandler ptrRecordHandler = new PTRRecordHandler();

    private static final IRecordHandler noneRecordHandler = new NoneRecordHandler();

    private static Name SERVER_NAME;

    /**
     * 构造一个 DNSServer 对象
     *
     * @param serverName 服务器名字
     * @param handlers   其能处理的 Question 的 handler
     */
    private DefaultDnsServer(String serverName, int port, IRecordHandler... handlers) {
        super(port);
        SERVER_NAME = Name.fromConstantString(serverName.endsWith(".") ? serverName : serverName + ".");
        // ns handler 放最先, 不管怎么样都会修改 ns 为 权威响应
        recordHandlerChain.add(nsRecordHandler);
        if (handlers != null) {
            recordHandlerChain.addAll(List.of(handlers));
        }
        // ptr handler 放在 自定义 handler 后边
        recordHandlerChain.add(ptrRecordHandler);
        // 最后放入兜底用的 NoneRecordHandler
        recordHandlerChain.add(noneRecordHandler);
        logger.debug("初始化 DnsServer, 本次一共加载了 {} 个 RecordHandler, 其中 3 个 RecordHandler 是最基础的 RecordHandler",
                recordHandlerChain.size()
        );
    }

    /**
     * 获取当前 dnsServer 的实例
     *
     * @param serverName 服务器名字
     * @return 当前 dnsServer 的实例, 这个实例是 单例 的
     */
    public static DefaultDnsServer getInstance(String serverName, int port, Resolver resolver) {
        if (dnsServer == null) {
            synchronized (DefaultDnsServer.class) {
                if (dnsServer == null) {
                    dnsServer = new DefaultDnsServer(serverName, port, new ARecordHandler(resolver), new AAAARecordHandler());
                }
            }
        }
        return dnsServer;
    }

    @SuppressWarnings("unused")
    public void addHandler(IRecordHandler... handlers) {
        recordHandlerChain.addAll(recordHandlerChain.size() - 2, List.of(handlers));
    }

    @SneakyThrows
    protected byte[] packetHandler(byte[] packet) {
        Message message = new Message(packet);
        for (IRecordHandler handler : recordHandlerChain) {
            // 如果 handler 支持 本次 Question, 那就调用它处理, 如果当前 handler 处理返回 true, 那就终止事件传递
            if (handler.supportType(message.getQuestion().getType()) && handler.handleQuestion(message)) break;
        }
        return message.toWire();
    }

    /**
     * 一个 什么都不做 的 RecordHandler, 用做 兜底策略
     */
    private static final class NoneRecordHandler implements IRecordHandler {

        private static final Logger logger = LoggerFactory.getLogger(NoneRecordHandler.class);

        @Override
        public boolean handleQuestion(Message message) {
            Record r = message.getQuestion();
            logger.warn("跳过不支持的 DNS 查询: {} -> {}", r.getName(), Type.string(r.getType()));
            RecordUtil.clearRecord(message);
            return true;
        }

        @Override
        public boolean supportType(int type) {
            return true;
        }

    }

    /**
     * 通过调用 {@link Message} 中的 {@link Header#setFlag(int)} 为 {@link Flags#AA}<br/>
     * 将本次响应消息设置为 权威应答 消息
     */
    private static final class NSRecordHandler implements IRecordHandler {

        private final Logger logger = LoggerFactory.getLogger(NSRecordHandler.class);

        @Override
        public boolean handleQuestion(Message message) {
            Name name = message.getQuestion().getName();
            message.addRecord(RecordUtil.newNsRecord(name, SERVER_NAME), Section.AUTHORITY);
            Header header = message.getHeader();
            header.setFlag(Flags.QR);
            header.setFlag(Flags.AA);
            header.setFlag(Flags.RD);
            logger.trace("NSQuestion:\n{}", message);
            return false;
        }

        @Override
        public boolean supportType(int type) {
            return true;
        }

    }

    /**
     * 反向域名解析, 这里只用来设置 DNS 服务器名
     */
    private static final class PTRRecordHandler implements IRecordHandler {

        private final Logger logger = LoggerFactory.getLogger(PTRRecordHandler.class);

        private final List<String> ips;

        @SneakyThrows
        public PTRRecordHandler() {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            List<Inet4Address> ips = new ArrayList<>();
            while (networkInterfaces.hasMoreElements()) {
                Enumeration<InetAddress> inetAddresses = networkInterfaces.nextElement().getInetAddresses();
                while (inetAddresses.hasMoreElements()) {
                    InetAddress address = inetAddresses.nextElement();
                    if (address instanceof Inet4Address) ips.add((Inet4Address) address);
                }
            }
            this.ips = ips.stream()
                    .map(it -> {
                        String[] parts = it.getHostAddress().split("\\.");
                        return String.join(".", parts[3], parts[2], parts[1], parts[0], "in-addr.arpa.");
                    })
                    .collect(Collectors.toList());
        }

        @Override
        @SneakyThrows
        public boolean handleQuestion(Message message) {
            Name name = message.getQuestion().getName();
            String host = name.toString();
            // 只处理 ips 中存在的反向域名解析
            if (ips.contains(host)) {
                message.addRecord(RecordUtil.newRecord(ARecord.class, SERVER_NAME, host.replace(".in-addr.arpa.", "")), Section.ANSWER);
                logger.trace("PTRQuestion:\n{}", message);
                return true;
            }
            return false;
        }

        @Override
        public boolean supportType(int type) {
            return type == Type.PTR;
        }

    }

}
