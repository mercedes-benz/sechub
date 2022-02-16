// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter.netsparker;

import static org.junit.Assert.*;

import org.junit.Test;

public class NetsparkerStateTest {

    @Test
    public void is_wellknown_return_true_for_Cancelled() {
        assertTrue(NetsparkerState.isWellknown("Cancelled"));
    }

    @Test
    public void is_wellknown_return_true_for_Failed() {
        assertTrue(NetsparkerState.isWellknown("Failed"));
    }

    @Test
    public void is_wellknown_return_true_for_Complete() {
        assertTrue(NetsparkerState.isWellknown("Complete"));
    }

    @Test
    public void is_wellknown_return_false_for_Unknown() {
        assertFalse(NetsparkerState.isWellknown("Unknown"));
    }
}
