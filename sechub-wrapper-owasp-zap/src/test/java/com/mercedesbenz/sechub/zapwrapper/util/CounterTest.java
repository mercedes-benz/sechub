// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.zapwrapper.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CounterTest {

    private Counter counterToTest;

    @BeforeEach
    void beforeEach() {
        counterToTest = new Counter();
    }

    @Test
    void testIncrement() {
        /* prepare */
        assertEquals(0, counterToTest.getCount());

        /* execute */
        counterToTest.increment();

        /* test */
        assertEquals(1, counterToTest.getCount());
    }
}