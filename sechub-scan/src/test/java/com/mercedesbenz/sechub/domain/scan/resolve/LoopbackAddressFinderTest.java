// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.resolve;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class LoopbackAddressFinderTest {

    private LoopbackAddressFinder detectorToTest;

    @Before
    public void before() throws Exception {
        detectorToTest = new LoopbackAddressFinder();
    }

    @Test
    public void _null() {
        assertNotLoopback("");
    }

    @Test
    public void empty() {
        assertNotLoopback("");
        assertNotLoopback("   ");
    }

    @Test
    public void localhost() {
        assertIsLoopback("localhost");
    }

    @Test
    public void example_org_is_legal() {
        assertNotLoopback("example.org");
        assertNotLoopback("www.example.org");
    }

    @Test
    public void example_com_is_legal() {
        assertNotLoopback("example.com");
        assertNotLoopback("www.example.com");
    }

    @Test
    public void IPv4_adress_not_being_loopback_is_legal() {
        assertNotLoopback("192.168.178.1");
    }

    @Test
    public void IPv6_adress_not_being_loopback_is_legal() {
        /*
         * From: https://www.ietf.org/rfc/rfc2732.txt "To use a literal IPv6 address in
         * a URL, the literal address should be enclosed in "[" and "]" characters."
         */
        assertNotLoopback("[2001:DB8:0:0:8:800:200C:417A]");
    }

    @Test
    public void IPv4_loopback() {
        assertIsLoopback("127.0.0.1");
        assertIsLoopback("127.255.255.254");
    }

    @Test
    public void IPv6_loopback() {
        /*
         * From: https://www.ietf.org/rfc/rfc2732.txt "To use a literal IPv6 address in
         * a URL, the literal address should be enclosed in "[" and "]" characters."
         */
        assertIsLoopback("[::1]");
        assertIsLoopback("[0:0:0:0:0:0:0:1]");
    }

    private void assertNotLoopback(String address) {
        common_assert_loopback(address, false);
    }

    private void assertIsLoopback(String address) {
        common_assert_loopback(address, true);
    }

    private void common_assert_loopback(String address, boolean illegal) {
        boolean result = detectorToTest.isLoopback(address);
        assertEquals(address, illegal, result);
    }

}
