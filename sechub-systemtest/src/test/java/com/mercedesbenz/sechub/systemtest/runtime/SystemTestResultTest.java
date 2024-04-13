// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.systemtest.runtime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;

class SystemTestResultTest {

    @Test
    void empty_result_has_no_tests_and_no_failing_tests() {
        /* prepare */
        SystemTestResult result = new SystemTestResult();

        /* test */
        assertEquals(0, result.getAmountOfAllTests());
        assertEquals(0, result.getAmountOfFailedTests());
    }

    @Test
    void result_with_one_run_which_failed_has_correct_amounts() {
        /* prepare */
        SystemTestResult result = new SystemTestResult();
        SystemTestRunResult run = mock(SystemTestRunResult.class);
        when(run.hasFailed()).thenReturn(true);

        result.getRuns().add(run);

        /* test */
        assertEquals(1, result.getAmountOfAllTests());
        assertEquals(1, result.getAmountOfFailedTests());
    }

    @Test
    void result_with_one_run_which_not_failed_has_correct_amounts() {
        /* prepare */
        SystemTestResult result = new SystemTestResult();
        SystemTestRunResult run = mock(SystemTestRunResult.class);
        when(run.hasFailed()).thenReturn(false);

        result.getRuns().add(run);

        /* test */
        assertEquals(1, result.getAmountOfAllTests());
        assertEquals(0, result.getAmountOfFailedTests());
    }

    @Test
    void result_with_three_runs_one_failed_has_correct_amounts() {
        /* prepare */
        SystemTestResult result = new SystemTestResult();
        SystemTestRunResult run1 = mock(SystemTestRunResult.class);
        when(run1.hasFailed()).thenReturn(false);
        result.getRuns().add(run1);

        SystemTestRunResult run2 = mock(SystemTestRunResult.class);
        when(run2.hasFailed()).thenReturn(true);
        result.getRuns().add(run2);

        SystemTestRunResult run3 = mock(SystemTestRunResult.class);
        when(run3.hasFailed()).thenReturn(false);
        result.getRuns().add(run3);

        /* test */
        assertEquals(3, result.getAmountOfAllTests());
        assertEquals(1, result.getAmountOfFailedTests());
    }

}
