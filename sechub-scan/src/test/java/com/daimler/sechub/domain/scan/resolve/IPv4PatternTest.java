// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.resolve;

import static org.junit.Assert.*;

import java.net.Inet4Address;

import org.junit.Test;

public class IPv4PatternTest {

    @Test
    public void test_valid_pattern_no_asterisk() throws Exception {
        IPv4Pattern pattern = new IPv4Pattern("192.168.178.2");
        assertTrue(pattern.isValid());
        assertTrue(pattern.isMatching(Inet4Address.getByAddress(bytes(192, 168, 178, 2))));
        assertFalse(pattern.isMatching(Inet4Address.getByAddress(bytes(192, 168, 178, 3))));
    }

    @Test
    public void test_valid_pattern_last_part_asterisk() throws Exception {
        IPv4Pattern pattern = new IPv4Pattern("192.168.178.*");
        assertTrue(pattern.isValid());
        assertTrue(pattern.isMatching(Inet4Address.getByAddress(bytes(192, 168, 178, 1))));
        assertTrue(pattern.isMatching(Inet4Address.getByAddress(bytes(192, 168, 178, 254))));

        assertFalse(pattern.isMatching(Inet4Address.getByAddress(bytes(192, 168, 179, 1))));
        assertFalse(pattern.isMatching(Inet4Address.getByAddress(bytes(192, 168, 179, 254))));
        assertFalse(pattern.isMatching(Inet4Address.getByAddress(bytes(255, 255, 255, 255))));
    }

    @Test
    public void test_valid_pattern_last_three_parts_asterisk() throws Exception {
        IPv4Pattern pattern = new IPv4Pattern("54.*.*.*");
        assertTrue(pattern.isValid());
        assertTrue(pattern.isMatching(Inet4Address.getByAddress(bytes(54, 168, 178, 1))));
        assertTrue(pattern.isMatching(Inet4Address.getByAddress(bytes(54, 254, 171, 254))));

        assertFalse(pattern.isMatching(Inet4Address.getByAddress(bytes(53, 168, 179, 1))));
        assertFalse(pattern.isMatching(Inet4Address.getByAddress(bytes(192, 168, 179, 254))));
        assertFalse(pattern.isMatching(Inet4Address.getByAddress(bytes(255, 255, 255, 255))));
    }

    @Test
    public void test_invalid_pattern_wrong_number() throws Exception {
        IPv4Pattern pattern = new IPv4Pattern("256.168.178.*");
        assertFalse(pattern.isValid());
    }

    @Test
    public void test_invalid_pattern_wrong_amount() throws Exception {
        IPv4Pattern pattern = new IPv4Pattern("253.168.178");
        assertFalse(pattern.isValid());
    }

    private static byte[] bytes(int... bytes) {
        byte[] result = new byte[bytes.length];
        int p = 0;
        for (int i : bytes) {
            result[p++] = (byte) i;
        }
        return result;
    }

}
