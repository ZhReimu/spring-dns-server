package com.mrx.dns.server;

import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

/**
 * @author Mr.X
 * @since 2022-10-30 09:23
 */
@SuppressWarnings("InfiniteLoopStatement")
public abstract class AbsDnsServer extends Thread {

    private static final Logger logger = LoggerFactory.getLogger(AbsDnsServer.class);

    private static final String THREAD_NAME_SUFFIX = "-DnsServer";

    private static final int BUFF_SIZE = 1024;

    private Runnable r;

    private final int port;

    protected AbsDnsServer(int port) {
        this.port = port;
    }

    /**
     * 以 NIO 方式 启动 Dns 服务器
     *
     * @see #start(ServerMode)
     */
    @Override
    public synchronized void start() {
        start(ServerMode.NIO);
    }

    @Override
    @SneakyThrows
    public void run() {
        logger.debug("DNS 服务器已开启, 运行在端口: {}", port);
        r.run();
    }

    /**
     * 以 mode 模式启动 Dns 服务器
     *
     * @param mode 服务器启动模式 {@link ServerMode}
     */
    public void start(ServerMode mode) {
        if (ServerMode.BIO.equals(mode)) {
            r = this::bioRun;
            setName("bio" + THREAD_NAME_SUFFIX);
        } else {
            r = this::nioRun;
            setName("nio" + THREAD_NAME_SUFFIX);
        }
        super.start();
    }

    /**
     * 以 bio 方式启动服务器
     */
    @SneakyThrows
    private void bioRun() {
        try (DatagramSocket socket = new DatagramSocket(port)) {
            while (true) {
                byte[] buff = new byte[BUFF_SIZE];
                DatagramPacket packet = new DatagramPacket(buff, 0, BUFF_SIZE);
                socket.receive(packet);
                logger.trace("收到请求: {}", packet.getSocketAddress());
                packet.setData(packetHandler(packet.getData(), packet.getAddress().getHostAddress()));
                socket.send(packet);
            }
        }
    }

    /**
     * 当 socket 收到一个 packet 时调用本方法
     *
     * @param packet 收到的 packet
     * @return 要发回去的 packet
     */
    protected abstract byte[] packetHandler(byte[] packet, String host);

    /**
     * 以 nio 方式启动服务器
     */
    @SneakyThrows
    private void nioRun() {
        try (DatagramChannel channel = DatagramChannel.open()) {
            channel.configureBlocking(false);
            channel.bind(new InetSocketAddress(port));
            ByteBuffer buffer = ByteBuffer.allocate(BUFF_SIZE);
            while (true) {
                // 清空Buffer
                buffer.clear();
                // 接受客户端发送数据
                SocketAddress client = channel.receive(buffer);
                if (client instanceof InetSocketAddress) {
                    channel.send(ByteBuffer.wrap(packetHandler(buffer.array(), ((InetSocketAddress) client).getHostName())), client);
                }
            }
        }
    }

    /**
     * DNS 服务器启动模式
     */
    public enum ServerMode {
        NIO, BIO
    }

}
