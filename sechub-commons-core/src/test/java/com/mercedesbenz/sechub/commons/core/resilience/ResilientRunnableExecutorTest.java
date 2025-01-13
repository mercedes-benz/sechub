// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.core.resilience;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ResilientRunnableExecutorTest {

    private ResilientRunOrFailExecutor executorToTest;

    @BeforeEach
    void beforeEach() {
        executorToTest = new ResilientRunOrFailExecutor();
    }

    @Test
    void one_retry_defined_but_no_execution_happens() throws Exception {
        /* prepare */
        executorToTest.add(new AlwaysRetry1TimeWith10MillisecondsTestConsultant());

        final AtomicInteger integer = new AtomicInteger();
        assertEquals(0, integer.intValue());

        /* execute */
        executorToTest.executeResilient(() -> integer.incrementAndGet());

        /* test */
        assertEquals(1, integer.intValue()); // execution was done
    }

    @Test
    void one_retry_defined__exception_on_first_run_but_not_on_seconds_try() throws Exception {
        /* prepare */
        executorToTest.add(new AlwaysRetry1TimeWith10MillisecondsTestConsultant());

        TestFailDefinedTimes testFailDefinedTimes = new TestFailDefinedTimes(1);

        /* execute */
        executorToTest.executeResilient(testFailDefinedTimes);

        /* test */
        assertEquals(2, testFailDefinedTimes.runs);
    }

    @Test
    void one_retry_defined__exception_on_first_and_second_run_will_fail_with_exception() throws Exception {
        /* prepare */
        executorToTest.add(new AlwaysRetry1TimeWith10MillisecondsTestConsultant());

        TestFailDefinedTimes testFailDefinedTimes = new TestFailDefinedTimes(2);

        /* execute */
        assertThrows(IOException.class, () -> executorToTest.executeResilient(testFailDefinedTimes));

        /* test */
        assertEquals(2, testFailDefinedTimes.runs);
    }

    private class AlwaysRetry1TimeWith10MillisecondsTestConsultant implements ResilienceConsultant {

        @Override
        public ResilienceProposal consultFor(ResilienceContext context) {
            return new SimpleRetryResilienceProposal("info1", 1, 10);
        }

    }
}
