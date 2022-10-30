package com.mrx.springdnsserver;

import com.mrx.dns.util.NetworkUtil;
import org.junit.jupiter.api.Test;

/**
 * @author Mr.X
 * @since 2022-10-30 21:17
 */
public class TestWithOutSpring {

    @Test
    public void networkTest() {
        System.out.println(NetworkUtil.isInCFips("172.67.186.61"));
        System.out.println(NetworkUtil.isInCFips("172.67.186.62"));
    }

}
