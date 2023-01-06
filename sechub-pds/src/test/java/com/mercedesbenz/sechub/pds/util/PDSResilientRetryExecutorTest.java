// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.util;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.pds.util.PDSResilientRetryExecutor.ExceptionThrower;

class PDSResilientRetryExecutorTest {

    private ExceptionThrower<TestTargetException> thrower;

    class TestTargetException extends Exception {
        private static final long serialVersionUID = 1L;

        public TestTargetException(String message, Exception cause) {
            super(message, cause);
        }

    }

    @BeforeEach
    void beforeEach() {
        thrower = new ExceptionThrower<TestTargetException>() {

            @Override
            public void throwException(String message, Exception cause) throws TestTargetException {
                throw new TestTargetException(message, cause);
            }

        };

    }

    @Test
    void no_problem_no_exception() throws TestTargetException {
        /* prepare */
        PDSResilientRetryExecutor<TestTargetException> executorToTest = new PDSResilientRetryExecutor<>(1, thrower, RuntimeException.class);

        /* execute +test */
        executorToTest.execute(() -> System.currentTimeMillis(), "id1");

    }

    @Test
    void fails_with_illegal_argument_when_no_target_exception_class_in_constructor() {
        /* test */
        assertThrows(IllegalArgumentException.class, () -> {
            /* execute */
            new PDSResilientRetryExecutor<TestTargetException>(1, thrower);
        });

    }

    @Test
    void when_runtime_exception_is_defined_as_handled_a_illegal_state_exception_will_be_handled() throws Exception {
        /* prepare */
        PDSResilientRetryExecutor<TestTargetException> executorToTest = new PDSResilientRetryExecutor<>(1, thrower, RuntimeException.class);

        /* test */
        assertThrows(TestTargetException.class, () -> {
            /* execute */
            executorToTest.execute(() -> {
                throw new IllegalStateException("test"); // fail always

            }, "id1");
        });

    }

    @Test
    void when_runtime_exception_is_defined_as_handled_a_runtime_exception_will_be_handled() throws Exception {
        /* prepare */
        PDSResilientRetryExecutor<TestTargetException> executorToTest = new PDSResilientRetryExecutor<>(1, thrower, RuntimeException.class);

        /* test */
        assertThrows(TestTargetException.class, () -> {
            /* execute */
            executorToTest.execute(() -> {
                throw new RuntimeException("test"); // fail always
            }, "id1");
        });

    }

    @Test
    void when_runtime_exception_is_defined_as_handled_a_IOException_will_be_handled_as_well() throws Exception {
        /* prepare */
        PDSResilientRetryExecutor<TestTargetException> executorToTest = new PDSResilientRetryExecutor<>(1, thrower, RuntimeException.class);

        /* test */
        assertThrows(TestTargetException.class, () -> {
            /* execute */
            executorToTest.execute(() -> {
                throw new IOException("test-io..."); // fail always

            }, "id1");
        });

    }

    @Test
    void when_runtime_exception_is_defined_as_handled_a_illegal_state_exception__happening_two_times_but_one_retry_execution_is_done() throws Exception {
        /* prepare */
        PDSResilientRetryExecutor<TestTargetException> executorToTest = new PDSResilientRetryExecutor<>(1, thrower, RuntimeException.class);
        AtomicInteger executionCount = new AtomicInteger();

        /* test */
        assertThrows(TestTargetException.class, () -> {
            /* execute */
            executorToTest.execute(() -> {
                if (executionCount.incrementAndGet() < 3) { // fail on first + second
                    throw new IllegalStateException("failing...");
                }
                return "output";

            }, "id1");
        });

    }

    @Test
    void when_runtime_exception_is_defined_as_handled_a_illegal_state_exception__happening_one_time_but_one_retry_execution_is_done() throws Exception {
        /* prepare */
        PDSResilientRetryExecutor<TestTargetException> executorToTest = new PDSResilientRetryExecutor<>(1, thrower, RuntimeException.class);
        AtomicInteger executionCount = new AtomicInteger();

        /* execute */
        executorToTest.execute(() -> {
            if (executionCount.incrementAndGet() < 2) { // fail on first only
                throw new IllegalStateException("failing...");
            }
            return null;

        }, "id1");
        /* test */
        assertEquals(2, executionCount.get());

    }

    @Test
    void when_runtime_exception_is_defined_as_handled_a_illegal_state_exception__happening_10_times_but_11_retry_execution_is_done() throws Exception {
        /* prepare */
        PDSResilientRetryExecutor<TestTargetException> executorToTest = new PDSResilientRetryExecutor<>(11, thrower, RuntimeException.class);
        AtomicInteger executionCount = new AtomicInteger();

        /* execute */
        executorToTest.execute(() -> {
            if (executionCount.incrementAndGet() < 10) {// fail on first 9 only
                throw new IllegalStateException("failing...");
            }
            return null;

        }, "id1");
        /* test */
        assertEquals(10, executionCount.get());

    }

    @Test
    void when_3_times_is_max_and_always_failing_final_failed_exception_is_thrown() throws Exception {
        /* prepare */
        PDSResilientRetryExecutor<TestTargetException> executorToTest = new PDSResilientRetryExecutor<>(3, thrower, IllegalArgumentException.class);
        AtomicInteger executionCount = new AtomicInteger();

        /* execute */
        assertThrows(TestTargetException.class, () -> {
            executorToTest.execute(() -> {
                executionCount.incrementAndGet();
                throw new IllegalArgumentException("Just for test", null);

            }, "id1");
        });

        /* test */
        assertEquals(4, executionCount.get()); // 1 normal , 3 retries ...

    }

    @Test
    void when_runtime_exception_is_defined_as_handled_a_illegal_state_exception__happening_1_times_but_5_retry_execution_is_done_waittime_0() throws Exception {
        /* prepare */
        int allowedMinimumMillisecondsForExecution = 0;
        int allowedMaximumMillisecondsForExecution = 30;

        PDSResilientRetryExecutor<TestTargetException> executorToTest = new PDSResilientRetryExecutor<>(5, thrower, RuntimeException.class);

        /* execute */
        assertSecondAttemptDoneInExpectedTimeRange(executorToTest, allowedMinimumMillisecondsForExecution, allowedMaximumMillisecondsForExecution);

    }

    @Test
    void when_runtime_exception_is_defined_as_handled_a_illegal_state_exception__happening_1_times_but_5_retry_execution_is_done_waittime_40ms()
            throws Exception {
        /* prepare */
        int waitTime = 40;
        int allowedMinimumMillisecondsForExecution = 40;
        int allowedMaximumMillisecondsForExecution = 70;

        PDSResilientRetryExecutor<TestTargetException> executorToTest = new PDSResilientRetryExecutor<>(5, thrower, RuntimeException.class);
        executorToTest.setMilliSecondsToWaiBeforeRetry(waitTime);

        /* execute */
        assertSecondAttemptDoneInExpectedTimeRange(executorToTest, allowedMinimumMillisecondsForExecution, allowedMaximumMillisecondsForExecution);

    }

    private void assertSecondAttemptDoneInExpectedTimeRange(PDSResilientRetryExecutor<TestTargetException> executorToTest, int min, int max)
            throws TestTargetException {
        /* prepare */
        AtomicInteger executionCount = new AtomicInteger();

        long start = System.currentTimeMillis();
        /* execute */
        String result = executorToTest.execute(() -> {
            if (executionCount.incrementAndGet() < 2) { // fail on first only
                throw new IllegalStateException("failing...");
            }
            return "my-result";

        }, "id1");
        long end = System.currentTimeMillis();

        /* test */
        assertEquals(2, executionCount.get());
        assertEquals("my-result", result);

        long elapsed = end - start;
        if (elapsed < min) {
            fail("Elapsed time: " + elapsed + " is lower than min: " + min);
        }
        if (elapsed > max) {
            fail("Elapsed time: " + elapsed + " is bigger than max: " + min);
        }
    }

}
