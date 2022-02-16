// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.monitoring;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class MemoryRuntimeTest {

    @Test
    void getter_for_memory_have_same_values_than_system_runtime_pendants() {
        /* prepare */
        MemoryRuntime runtimeToTest = new MemoryRuntime();
        Runtime systemRuntime = Runtime.getRuntime();

        // define/consume local variables before calling system runtime
        long maxMemory = 0;
        long totalMemory = 1;
        long freeMemory = 2;

        long maxMemory2 = 3;
        long totalMemory2 = 4;
        long freeMemory2 = 5;

        /* execute */
        maxMemory = systemRuntime.maxMemory();
        totalMemory = systemRuntime.totalMemory();
        freeMemory = systemRuntime.freeMemory();

        maxMemory2 = runtimeToTest.getMaxMemory();
        totalMemory2 = runtimeToTest.getTotalMemory();
        freeMemory2 = runtimeToTest.getFreeMemory();

        /* test */
        assertEquals(maxMemory, maxMemory2);
        assertEquals(totalMemory, totalMemory2);
        assertEquals(freeMemory, freeMemory2);
    }

}
