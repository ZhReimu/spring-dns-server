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
        System.out.println(NetworkUtil.isInRange("172.67.186.61", "173.245.48.0/20"));
        System.out.println(NetworkUtil.isInRange("172.67.186.62", "172.64.0.0/13"));
    }

}
