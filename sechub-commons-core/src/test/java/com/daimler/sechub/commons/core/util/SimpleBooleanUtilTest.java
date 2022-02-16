// SPDX-License-Identifier: MIT
package com.daimler.sechub.commons.core.util;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class SimpleBooleanUtilTest {


    /* FALSE parts */
    @Test
    void isFalseOrNull_null_yes() {
        assertTrue(SimpleBooleanUtil.isFalseOrNull(null));
    }

    @Test
    void isFalseOrNull_FALSE_yes() {
        assertTrue(SimpleBooleanUtil.isFalseOrNull(Boolean.FALSE));
    }

    @Test
    void isFalseOrNull_TRUE_no() {
        assertFalse(SimpleBooleanUtil.isFalseOrNull(Boolean.TRUE));
    }

    /* TRUE parts */
    @Test
    void isTrue_null_no() {
        assertFalse(SimpleBooleanUtil.isTrue(null));
    }

    @Test
    void isTrue_FALSE_no() {
        assertFalse(SimpleBooleanUtil.isTrue(Boolean.FALSE));
    }

    @Test
    void isTrue_TRUE_yes() {
        assertTrue(SimpleBooleanUtil.isTrue(Boolean.TRUE));
    }
}
