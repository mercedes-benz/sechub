// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.monitoring;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.lang.management.OperatingSystemMXBean;

import org.junit.Before;
import org.junit.Test;

public class TestCPUMonitor {

    private OperatingSystemMXBean bean;
    private CPUMonitor monitorToTest;

    @Before
    public void before() throws Exception {
        bean = mock(OperatingSystemMXBean.class);
        monitorToTest = new CPUMonitor(bean, 200);
    }

    @Test
    public void one_processor_system_load_average_0_dot_5_results_cpu_load_0_5() {
        when(bean.getAvailableProcessors()).thenReturn(1);
        when(bean.getSystemLoadAverage()).thenReturn(0.5);

        /* execute + test */
        assertEquals(0.5, monitorToTest.getCPULoadAverage(), 0.01);
    }

    @Test
    public void two_processors_system_load_average_2_percent_results_cpu_load_1() {
        when(bean.getAvailableProcessors()).thenReturn(2);
        when(bean.getSystemLoadAverage()).thenReturn(2.0);

        /* execute + test */
        assertEquals(1.0, monitorToTest.getCPULoadAverage(), 0.01);
    }

    @Test
    public void two_processors_system_load_average_1_percent_results_cpu_load_0_5() {
        when(bean.getAvailableProcessors()).thenReturn(2);
        when(bean.getSystemLoadAverage()).thenReturn(1.0);

        /* execute + test */
        assertEquals(0.5, monitorToTest.getCPULoadAverage(), 0.01);
    }

    @Test
    public void four_processors_system_load_average_1_percent_results_cpu_load_0_25() {
        when(bean.getAvailableProcessors()).thenReturn(4);
        when(bean.getSystemLoadAverage()).thenReturn(1.0);

        /* execute + test */
        assertEquals(0.25, monitorToTest.getCPULoadAverage(), 0.01);
    }

}
