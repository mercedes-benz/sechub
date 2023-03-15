// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan;

import static org.junit.Assert.*;

import org.junit.Test;

public class NetworkTargetTypeTest {

    @Test
    public void healthcheck() {
        assertEquals("Hmm.. amount of available target types differs! Seems there is a new target typeToTest which is not tested, please add + modify...", 4,
                NetworkTargetType.values().length);
    }

    @Test
    public void INTRANET_test() {
        NetworkTargetType typeToTest = NetworkTargetType.INTRANET;

        assertTrue(typeToTest.isIntranet());
        assertTrue(typeToTest.isValid());

        assertFalse(typeToTest.isInternet());
    }

    @Test
    public void INTERNET_test() {
        NetworkTargetType typeToTest = NetworkTargetType.INTERNET;

        assertTrue(typeToTest.isInternet());
        assertTrue(typeToTest.isValid());

        assertFalse(typeToTest.isIntranet());
    }

    @Test
    public void UNKNOWN_test() {
        NetworkTargetType typeToTest = NetworkTargetType.UNKNOWN;

        assertNothingReturnsTrue(typeToTest);
    }

    @Test
    public void ILLEGAL_test() {
        NetworkTargetType typeToTest = NetworkTargetType.ILLEGAL;

        assertNothingReturnsTrue(typeToTest);
    }

    private void assertNothingReturnsTrue(NetworkTargetType typeToTest) {
        assertFalse(typeToTest.isInternet());
        assertFalse(typeToTest.isIntranet());
        assertFalse(typeToTest.isValid());
    }
}
