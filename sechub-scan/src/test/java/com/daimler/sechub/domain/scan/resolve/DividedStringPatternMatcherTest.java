// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.resolve;

import static org.junit.Assert.*;

import org.junit.Test;

public class DividedStringPatternMatcherTest {

    @Test
    public void test_ipv4_matcher() {
        /* prepare */
        DividedStringPatternMatcher matcherToTest = new DividedStringPatternMatcher("54.*.*.*", '.');

        /* test */

        assertFalse(matcherToTest.isMatching("1.2.3"));
        assertFalse(matcherToTest.isMatching("1.2.3.4"));
        assertFalse(matcherToTest.isMatching("55.2.3.4"));
        assertFalse(matcherToTest.isMatching("54"));
        assertFalse(matcherToTest.isMatching(""));
        assertFalse(matcherToTest.isMatching(null));
        assertFalse(matcherToTest.isMatching("54.2.3."));

        assertTrue(matcherToTest.isMatching("54.2.3.4"));
        assertTrue(matcherToTest.isMatching("54.2.3.255"));
        assertTrue(matcherToTest.isMatching("54.255.255.255"));

    }

    @Test
    public void test_ipv6_matcher() {
        /* prepare */
        DividedStringPatternMatcher matcherToTest = new DividedStringPatternMatcher("2001:DB8:0:0:8:800:200C:*", ':');

        /* test */
        assertFalse(matcherToTest.isMatching("2002:db8:0:0:8:800:200C:417A"));
        assertFalse(matcherToTest.isMatching("0:::417A"));
        assertFalse(matcherToTest.isMatching("2001:DB8:0:0:8:800:200C:"));
        assertFalse(matcherToTest.isMatching("2001:DB8::8:800:200C:417A")); // short form is not supported

        assertTrue(matcherToTest.isMatching("2001:DB8:0:0:8:800:200C:417A"));
        assertTrue(matcherToTest.isMatching("2001:DB8:0:0:8:800:200C:417B"));
    }

}
