// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.resolve;

import static org.junit.Assert.*;

import java.net.Inet6Address;
import java.net.InetAddress;

import org.junit.Before;
import org.junit.Test;

import com.daimler.sechub.domain.scan.Target;
import com.daimler.sechub.domain.scan.TargetType;

public class IntraneIPpatternResolveStrategyTest {

    private static InetAddress INET_6_ADR1;
    private static InetAddress INET_6_ADR2;
    private static InetAddress INET_4_ADR1;
    private static InetAddress INET_4_ADR2;
    static {
        try {
            INET_6_ADR1 = Inet6Address.getByName("2001:CA52:0:0:8:800:200C:417A");
            INET_6_ADR2 = Inet6Address.getByName("2001:DB9:0:0:8:800:200C:417A");

            INET_4_ADR1 = Inet6Address.getByName("192.168.178.2");
            INET_4_ADR2 = Inet6Address.getByName("54.3.2.27");
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }
    private IntranetIPpatternResolveStrategy strategyToTest;

    @Before
    public void before() {
        strategyToTest = new IntranetIPpatternResolveStrategy();
    }

    @Test
    public void initialize_returned_result_as_expected() {
        assertTrue(strategyToTest.initialize("intranet-ip-pattern:xxx"));
        assertTrue(strategyToTest.initialize("intranet-ip-pattern: xxx"));
        assertTrue(strategyToTest.initialize("intranet-ip-pattern: xxx,yyy"));

        assertFalse(strategyToTest.initialize(""));
        assertFalse(strategyToTest.initialize(null));
        assertFalse(strategyToTest.initialize("xxx,yyy"));
        assertFalse(strategyToTest.initialize("ends-with xxx,yyy"));
        assertFalse(strategyToTest.initialize(" intranet-ip-pattern:xxx"));

        assertFalse(strategyToTest.initialize("intranet-ip-pattern:"));

    }

    @Test
    public void intranet_inet6_adr1_matching_pattern_used() throws Exception {

        /* execute */
        strategyToTest.initialize("intranet-ip-pattern:2001:CA52:*:*:*:*:*:*");

        /* test */
        assertEquals(new Target(INET_6_ADR1, TargetType.INTRANET), strategyToTest.resolveTargetFor(INET_6_ADR1));

        assertEquals(new Target(INET_6_ADR2, TargetType.INTERNET), strategyToTest.resolveTargetFor(INET_6_ADR2));
        assertEquals(new Target(INET_4_ADR1, TargetType.INTERNET), strategyToTest.resolveTargetFor(INET_4_ADR1));
        assertEquals(new Target(INET_4_ADR2, TargetType.INTERNET), strategyToTest.resolveTargetFor(INET_4_ADR2));
    }

    @Test
    public void intranet_inet6_adr1_and_adr2_matching_pattern_used() throws Exception {

        /* execute */
        strategyToTest.initialize("intranet-ip-pattern:2001:CA52:*:*:*:*:*:*,2001:DB9:0:0:8:800:200C:*");

        /* test */
        assertEquals(new Target(INET_6_ADR1, TargetType.INTRANET), strategyToTest.resolveTargetFor(INET_6_ADR1));
        assertEquals(new Target(INET_6_ADR2, TargetType.INTRANET), strategyToTest.resolveTargetFor(INET_6_ADR2));

        assertEquals(new Target(INET_4_ADR1, TargetType.INTERNET), strategyToTest.resolveTargetFor(INET_4_ADR1));
        assertEquals(new Target(INET_4_ADR2, TargetType.INTERNET), strategyToTest.resolveTargetFor(INET_4_ADR2));
    }

    @Test
    public void intranet_inet4_adr1_and_inet6_adr2_matching_pattern_used() throws Exception {

        /* execute */
        strategyToTest.initialize("intranet-ip-pattern:192.168.178.*,2001:DB9:0:0:8:800:200C:*");

        /* test */
        assertEquals(new Target(INET_4_ADR1, TargetType.INTRANET), strategyToTest.resolveTargetFor(INET_4_ADR1));
        assertEquals(new Target(INET_6_ADR2, TargetType.INTRANET), strategyToTest.resolveTargetFor(INET_6_ADR2));

        assertEquals(new Target(INET_6_ADR1, TargetType.INTERNET), strategyToTest.resolveTargetFor(INET_6_ADR1));
        assertEquals(new Target(INET_4_ADR2, TargetType.INTERNET), strategyToTest.resolveTargetFor(INET_4_ADR2));
    }

}
