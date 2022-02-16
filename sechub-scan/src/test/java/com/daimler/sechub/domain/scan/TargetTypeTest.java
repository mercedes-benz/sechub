// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan;

import static org.junit.Assert.*;

import org.junit.Test;

public class TargetTypeTest {

    @Test
    public void healthcheck() {

        assertEquals("Hmm.. seems there is a new target typeToTest which is not tested, please add + modify...", 5, TargetType.values().length);
    }

    @Test
    public void INTRANET_test() {
        TargetType typeToTest = TargetType.INTRANET;

        assertTrue(typeToTest.isIntranet());
        assertTrue(typeToTest.isValid());

        assertFalse(typeToTest.isInternet());
        assertFalse(typeToTest.isCodeUpload());
    }

    @Test
    public void CODEUPLOAD_test() {
        TargetType typeToTest = TargetType.CODE_UPLOAD;

        assertTrue(typeToTest.isValid());
        assertTrue(typeToTest.isCodeUpload());

        assertFalse(typeToTest.isInternet());
        assertFalse(typeToTest.isIntranet());
    }

    @Test
    public void INTERNET_test() {
        TargetType typeToTest = TargetType.INTERNET;

        assertTrue(typeToTest.isInternet());
        assertTrue(typeToTest.isValid());

        assertFalse(typeToTest.isCodeUpload());
        assertFalse(typeToTest.isIntranet());

    }

    @Test
    public void UNKNOWN_test() {
        TargetType typeToTest = TargetType.UNKNOWN;

        assertNothingReturnsTrue(typeToTest);
    }

    @Test
    public void ILLEGAL_test() {
        TargetType typeToTest = TargetType.ILLEGAL;

        assertNothingReturnsTrue(typeToTest);
    }

    private void assertNothingReturnsTrue(TargetType typeToTest) {
        assertFalse(typeToTest.isInternet());
        assertFalse(typeToTest.isIntranet());
        assertFalse(typeToTest.isValid());
        assertFalse(typeToTest.isCodeUpload());
    }
}
