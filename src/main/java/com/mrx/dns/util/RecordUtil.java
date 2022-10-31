package com.mrx.dns.util;

import lombok.SneakyThrows;
import org.xbill.DNS.*;

import java.net.InetAddress;

/**
 * @author Mr.X
 * @since 2022-10-30 09:47
 */
public class RecordUtil {

    public static final int TTL = 600;

    @Deprecated
    @SneakyThrows
    public static <T extends Record> T newRecord(Class<T> type, Name name, String host) {
        return type.getConstructor(Name.class, int.class, long.class, InetAddress.class)
                .newInstance(name, DClass.IN, TTL, InetAddress.getByName(host));
    }

    /**
     * 新建一个 nsRecord
     *
     * @param name   nsRecord 所指 name
     * @param target nsRecord 所在 name
     * @return 新的 nsRecord
     */
    public static NSRecord newNsRecord(Name name, Name target) {
        return new NSRecord(name, DClass.IN, TTL, target);
    }

    @SneakyThrows
    public static ARecord newARecord(Name name, String host) {
        return new ARecord(name, DClass.IN, TTL, InetAddress.getByName(host));
    }

    public static PTRRecord newPTRRecord(Name name, Name target) {
        return new PTRRecord(target, DClass.IN, TTL, name);
    }

    /**
     * 清空 AUTHORITY 节的记录并将 message 设置为 NXDOMAIN
     *
     * @param message 要操作的 message
     */
    public static void clearRecord(Message message) {
        message.removeAllRecords(Section.AUTHORITY);
        Header header = message.getHeader();
        header.unsetFlag(Flags.AA);
        header.setRcode(Rcode.NXDOMAIN);
    }

}
