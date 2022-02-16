// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.resolve;

import static org.junit.Assert.*;

import java.net.Inet6Address;

import org.junit.Test;

public class IPv6PatternTest {

    @Test
    public void valid_ipv6_pattern_last_part_asterisk() throws Exception {
        IPv6Pattern pattern = new IPv6Pattern("2001:db8:0:0:0:0:2:*");

        assertTrue(pattern.isValid());

        assertTrue(pattern.isMatching(Inet6Address.getByName("2001:db8:0:0:0:0:2:1")));
        assertTrue(pattern.isMatching(Inet6Address.getByName("2001:db8:0:0:0:0:2:ffff")));

        assertFalse(pattern.isMatching(Inet6Address.getByName("2001:db8:0:0:0:0:3:1")));
        assertFalse(pattern.isMatching(Inet6Address.getByName("2002:db8:0:0:0:0:2:1")));
        assertFalse(pattern.isMatching(Inet6Address.getByName("2002:db8:0:0:0:0:2:ffff")));
    }

    @Test
    public void valid_ipv6_pattern_last_two_part_asterisk() throws Exception {
        IPv6Pattern pattern = new IPv6Pattern("2001:db8:0:0:0:0:*:*");

        assertTrue(pattern.isValid());

        assertTrue(pattern.isMatching(Inet6Address.getByName("2001:db8:0:0:0:0:2:1")));
        assertTrue(pattern.isMatching(Inet6Address.getByName("2001:db8:0:0:0:0:2:ffff")));
        assertTrue(pattern.isMatching(Inet6Address.getByName("2001:db8:0:0:0:0:3:1")));
        assertTrue(pattern.isMatching(Inet6Address.getByName("2001:db8:0:0:0:0:ffff:1")));

        assertFalse(pattern.isMatching(Inet6Address.getByName("2002:db8:0:0:0:0:2:1")));
        assertFalse(pattern.isMatching(Inet6Address.getByName("2002:db8:0:0:0:0:2:ffff")));
    }

    @Test
    public void valid_ipv6_pattern_because_not_8_dividers_is_invalid() throws Exception {
        IPv6Pattern pattern = new IPv6Pattern("2001:db8:0:0:0:0:*");

        assertFalse(pattern.isValid());

    }

    @Test
    public void valid_ipv6_pattern_because_number_too_big_is_invalid() throws Exception {
        IPv6Pattern pattern = new IPv6Pattern("10000:db8:0:0:0:0:*:*");

        assertFalse(pattern.isValid());

    }

}
