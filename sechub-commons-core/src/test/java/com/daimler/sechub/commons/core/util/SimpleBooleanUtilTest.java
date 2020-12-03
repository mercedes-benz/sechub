// SPDX-License-Identifier: MIT
package com.daimler.sechub.commons.core.util;

import static org.junit.Assert.*;

import org.junit.Test;

public class SimpleBooleanUtilTest {


    /* FALSE parts */
    @Test
    public void isFalseOrNull_null_yes() {
        assertTrue(SimpleBooleanUtil.isFalseOrNull(null));
    }

    @Test
    public void isFalseOrNull_FALSE_yes() {
        assertTrue(SimpleBooleanUtil.isFalseOrNull(Boolean.FALSE));
    }

    @Test
    public void isFalseOrNull_TRUE_no() {
        assertFalse(SimpleBooleanUtil.isFalseOrNull(Boolean.TRUE));
    }

    /* TRUE parts */
    @Test
    public void isTrue_null_no() {
        assertFalse(SimpleBooleanUtil.isTrue(null));
    }

    @Test
    public void isTrue_FALSE_no() {
        assertFalse(SimpleBooleanUtil.isTrue(Boolean.FALSE));
    }

    @Test
    public void isTrue_TRUE_yes() {
        assertTrue(SimpleBooleanUtil.isTrue(Boolean.TRUE));
    }
}
