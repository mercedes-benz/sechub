// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.systemtest.runtime;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SystemTestRuntimeContextTest {
    private SystemTestRuntimeContext contextToTest;

    @BeforeEach
    void beforeEach() {
        contextToTest = new SystemTestRuntimeContext();
    }

    @Test
    void initial_context_runs_all_tests() {
        assertTrue(contextToTest.isRunningAllTests());
        assertTrue(contextToTest.isRunningTest("anything"));
    }

    @Test
    void initial_context_with_added_null_as_test_runs_all_tests() {
        /* prepare */
        contextToTest.addTestsToRun(null);

        /* execute + test */
        assertTrue(contextToTest.isRunningAllTests());
        assertTrue(contextToTest.isRunningTest("anything"));
    }

    @Test
    void initial_context_with_empty_tests_to_run_runs_all_tests() {
        /* prepare */
        contextToTest.addTestsToRun(new ArrayList<>());

        /* execute + test */
        assertTrue(contextToTest.isRunningAllTests());
        assertTrue(contextToTest.isRunningTest("anything"));
    }

    @Test
    void initial_context_with_one_test_entry_to_run_runs_only_defined_one() {
        /* prepare */
        List<String> testNames = new ArrayList<>();
        testNames.add("defined-testname");
        contextToTest.addTestsToRun(testNames);

        /* execute + test */
        assertFalse(contextToTest.isRunningAllTests());
        assertTrue(contextToTest.isRunningTest("defined-testname"));
        assertFalse(contextToTest.isRunningTest("anything"));
    }

    @Test
    void initial_context_with_two_test_entries_to_run_runs_only_defined_ones() {
        /* prepare */
        List<String> testNames = new ArrayList<>();
        testNames.add("defined-testname1");
        testNames.add("defined-testname2");
        contextToTest.addTestsToRun(testNames);

        /* execute + test */
        assertFalse(contextToTest.isRunningAllTests());
        assertTrue(contextToTest.isRunningTest("defined-testname1"));
        assertTrue(contextToTest.isRunningTest("defined-testname2"));
        assertFalse(contextToTest.isRunningTest("anything"));
    }

}
