// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.monitoring;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;

public class MemoryUsagePercentMonitorTest {

    private MemoryUsageMonitor monitorToTest;
    private MemoryRuntime memoryRuntime;

    @Before
    public void before() throws Exception {
        memoryRuntime = mock(MemoryRuntime.class);
        monitorToTest = new MemoryUsageMonitor(memoryRuntime, 200);
    }

    @Test
    public void max_memory_allocated_half_time_nothing_free__results_in_50_percent() {
        /* prepare maximum allocated, but 500 are free */
        when(memoryRuntime.getMaxMemory()).thenReturn(1000L);
        when(memoryRuntime.getTotalMemory()).thenReturn(500L); // allocated
        when(memoryRuntime.getFreeMemory()).thenReturn(0L);

        /* execute */
        double result = monitorToTest.getMemoryUsageInPercent();

        /* test */
        assertEquals("Expected 50%", 50, result, 0.01);

    }

    @Test
    public void max_memory_allocated__but_half_is_still_free__results_in_50_percent() {
        /* prepare maximum allocated, but 500 are free */
        when(memoryRuntime.getMaxMemory()).thenReturn(1000L);
        when(memoryRuntime.getTotalMemory()).thenReturn(1000L); // allocated
        when(memoryRuntime.getFreeMemory()).thenReturn(500L);

        /* execute */
        double result = monitorToTest.getMemoryUsageInPercent();

        /* test */
        assertEquals("Expected 50%", 50.0, result, 0.01);

    }

    @Test
    public void max_memory_allocated_half_time_but_all_allocated_is_also_free__results_in_0_percent() {
        /* prepare maximum allocated, but 500 are free */
        when(memoryRuntime.getMaxMemory()).thenReturn(1000L);
        when(memoryRuntime.getTotalMemory()).thenReturn(500L); // allocated
        when(memoryRuntime.getFreeMemory()).thenReturn(500L);

        /* execute */
        double result = monitorToTest.getMemoryUsageInPercent();

        /* test */
        assertEquals("Expected 0%", 0, result, 0.01);

    }

}
