// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.core.resilience;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ResilientActionExecutorTest {

    private ResilientActionExecutor<TestResult> executorToTest;
    private TestAction action;

    @BeforeEach
    void before() {
        executorToTest = new ResilientActionExecutor<>();
        action = new TestAction();
    }

    @Test
    void fall_through_200_ms__inside_time_frame_same_exception_will_be_returned_after_this_real_call_done() throws Exception {
        /* prepare */
        long millisToFallThrough = 200;

        action.throwables.add(new IllegalArgumentException("first-to-reuse")); // one exception only, so second try will return result
        action.result.text = "OK";

        FallthroughResilienceProposal resilienceProposal = mock(FallthroughResilienceProposal.class);
        when(resilienceProposal.getMillisecondsForFallThrough()).thenReturn(millisToFallThrough);

        ResilienceConsultant resilienceConsultant = mock(ResilienceConsultant.class);
        when(resilienceConsultant.consultFor(any(ResilienceContext.class))).thenReturn(resilienceProposal);

        executorToTest.add(resilienceConsultant);

        TestResult result = null;
        long timeStart = System.currentTimeMillis();
        boolean failing = true;
        int counter = 0;

        /* execute */
        while (failing) {
            try {
                Thread.sleep(20);
                counter++;
                result = executorToTest.executeResilient(action);

                failing = false;
            } catch (IllegalArgumentException e) {
                assertThat(e.getMessage()).isEqualTo("first-to-reuse");
            }
        }

        /* test */
        long timeEnd = System.currentTimeMillis();
        long timeElapsed = timeEnd - timeStart;

        assertThat(counter).withFailMessage("Must have at least 5 times called action, but was only %d", counter).isGreaterThan(5);

        assertThat(timeElapsed).withFailMessage("Time elapsed: %d millis", timeElapsed).isGreaterThanOrEqualTo(millisToFallThrough);

        assertThat(result).isNotNull();
        assertThat(result.text).isEqualTo("OK");
    }

    @Test
    void no_error_no_consultant_defined_just_runs_through_and_returns_result() throws Exception {
        /* execute */
        TestResult result = executorToTest.executeResilient(action);

        /* test */
        assertThat(result).isNotNull();
    }

    @Test
    void no_error_but_consultant_defined_just_runs_through_and_returns_result_consultant_never_called() throws Exception {
        /* prepare */
        ResilienceConsultant resilienceConsultant = mock(ResilienceConsultant.class);
        executorToTest.add(resilienceConsultant);

        /* execute */
        TestResult result = executorToTest.executeResilient(action);

        /* test */
        assertThat(result).isNotNull();
        verify(resilienceConsultant, never()).consultFor(any(ResilienceContext.class));

    }

    @Test
    void retry_3_times_allowed__we_throw_errors_which_forces_a_retry_the_callback_is_called_3_times() throws Exception {
        /* prepare */
        action.throwables.add(new IllegalArgumentException());
        action.throwables.add(new IllegalArgumentException());
        action.throwables.add(new IllegalArgumentException());

        ResilienceCallback callback = mock(ResilienceCallback.class);
        RetryResilienceProposal resilienceProposal = mock(RetryResilienceProposal.class);
        when(resilienceProposal.getMaximumAmountOfRetries()).thenReturn(3);
        when(resilienceProposal.getMillisecondsToWaitBeforeRetry()).thenReturn(0L);

        ResilienceConsultant resilienceConsultant = mock(ResilienceConsultant.class);
        when(resilienceConsultant.consultFor(any(ResilienceContext.class))).thenReturn(resilienceProposal);

        executorToTest.add(resilienceConsultant);

        /* execute */
        TestResult result = executorToTest.executeResilient(action, callback);

        /* test */
        assertThat(result).isNotNull();
        assertThat(result.timesActionHasBeenExecuted).isEqualTo(4);
        verify(callback, times(3)).beforeRetry(any());

    }

    @Test
    void retry_3_times_allowed__we_got_an_error_which_forces_a_retry_the_callback_is_called1_times() throws Exception {
        /* prepare */
        action.throwables.add(new IllegalArgumentException());

        ResilienceCallback callback = mock(ResilienceCallback.class);
        RetryResilienceProposal resilienceProposal = mock(RetryResilienceProposal.class);
        when(resilienceProposal.getMaximumAmountOfRetries()).thenReturn(3);
        when(resilienceProposal.getMillisecondsToWaitBeforeRetry()).thenReturn(0L);

        ResilienceConsultant resilienceConsultant = mock(ResilienceConsultant.class);
        when(resilienceConsultant.consultFor(any(ResilienceContext.class))).thenReturn(resilienceProposal);

        executorToTest.add(resilienceConsultant);

        /* execute */
        TestResult result = executorToTest.executeResilient(action, callback);

        /* test */
        assertThat(result).isNotNull();
        assertThat(result.timesActionHasBeenExecuted).isEqualTo(2);
        verify(callback, times(1)).beforeRetry(any());

    }

    @Test
    void retry_2_times_allowed__we_got_an_error_which_forces_a_retry_we_got_the_result_from_second_attempt() throws Exception {
        /* prepare */
        action.throwables.add(new IllegalArgumentException()); // one exception only, so second try will return result
        action.result.text = "OK";

        RetryResilienceProposal resilienceProposal = mock(RetryResilienceProposal.class);
        when(resilienceProposal.getMaximumAmountOfRetries()).thenReturn(2);
        when(resilienceProposal.getMillisecondsToWaitBeforeRetry()).thenReturn(0L);

        ResilienceConsultant resilienceConsultant = mock(ResilienceConsultant.class);
        when(resilienceConsultant.consultFor(any(ResilienceContext.class))).thenReturn(resilienceProposal);

        executorToTest.add(resilienceConsultant);

        /* execute */
        TestResult result = executorToTest.executeResilient(action);

        /* test */
        assertThat(result).isNotNull();
        assertThat(result.timesActionHasBeenExecuted).isEqualTo(2);
        assertThat(result.text).isEqualTo("OK");

        /* check also that the consultant was inspected one time */
        verify(resilienceConsultant, times(1)).consultFor(any(ResilienceContext.class));

    }

    @Test
    void retry_2_times_allowed__we_got_two_errors_which_forces_2_retries_we_got_the_result_from_third_attempt() throws Exception {
        /* prepare */
        action.throwables.add(new IllegalArgumentException()); // first
        action.throwables.add(new IllegalArgumentException()); // second
        action.result.text = "OK";

        RetryResilienceProposal resilienceProposal = mock(RetryResilienceProposal.class);
        when(resilienceProposal.getMaximumAmountOfRetries()).thenReturn(2);
        when(resilienceProposal.getMillisecondsToWaitBeforeRetry()).thenReturn(0L);

        ResilienceConsultant resilienceConsultant = mock(ResilienceConsultant.class);
        when(resilienceConsultant.consultFor(any(ResilienceContext.class))).thenReturn(resilienceProposal);

        executorToTest.add(resilienceConsultant);

        /* execute */
        TestResult result = executorToTest.executeResilient(action);

        /* test */
        assertThat(result).isNotNull();
        assertThat(result.timesActionHasBeenExecuted).isEqualTo(3);
        assertThat(result.text).isEqualTo("OK");

        verify(resilienceConsultant, times(2)).consultFor(any(ResilienceContext.class));

    }

    @Test
    void retry_2_times_allowed__we_got_3_errors_which_forces_2_retries_and_we_got_the_origin_failure_from_third_attempt() throws Exception {
        /* prepare */
        action.throwables.add(new IllegalArgumentException("first")); // first will be ignored
        action.throwables.add(new IllegalArgumentException("second")); // second will be ignored
        action.throwables.add(new IllegalArgumentException("third")); // third will be thrown
        action.result.text = "OK";

        RetryResilienceProposal resilienceProposal = mock(RetryResilienceProposal.class);
        when(resilienceProposal.getMaximumAmountOfRetries()).thenReturn(2);
        when(resilienceProposal.getMillisecondsToWaitBeforeRetry()).thenReturn(0L);

        ResilienceConsultant resilienceConsultant = mock(ResilienceConsultant.class);
        when(resilienceConsultant.consultFor(any(ResilienceContext.class))).thenReturn(resilienceProposal);

        executorToTest.add(resilienceConsultant);

        /* execute */
        assertThatThrownBy(() -> executorToTest.executeResilient(action)).isInstanceOf(IllegalArgumentException.class).hasMessage("third");

        /* test */
        assertThat(action.result.timesActionHasBeenExecuted).isEqualTo(3);
        /*
         * check also that the consultant was inspected 3 times - we always ask the
         * consultants in case of errors, make because of an exception information the
         * maximum could change...
         */
        verify(resilienceConsultant, times(3)).consultFor(any(ResilienceContext.class));

    }

    @Test
    void containsConsultatant_differs_correctly() {
        /* prepare */
        executorToTest.add(new TestConsultant());

        /* test */
        assertThat(executorToTest.containsConsultant(TestConsultant.class)).isTrue();
        assertThat(executorToTest.containsConsultant(TestConsultant2.class)).isFalse();
    }

    private class TestAction implements ResilientAction<TestResult> {

        Queue<Exception> throwables = new ArrayBlockingQueue<>(10);
        TestResult result = new TestResult();

        @Override
        public TestResult execute() throws Exception {
            result.timesActionHasBeenExecuted++;
            if (throwables.isEmpty()) {
                return result;
            }
            Exception fetched = throwables.poll();
            throw fetched;
        }

    }

    private class TestResult {
        private int timesActionHasBeenExecuted;
        private String text;

    }

    private class TestConsultant implements ResilienceConsultant {

        @Override
        public ResilienceProposal consultFor(ResilienceContext context) {
            return null;
        }

    }

    private class TestConsultant2 implements ResilienceConsultant {

        @Override
        public ResilienceProposal consultFor(ResilienceContext context) {
            return null;
        }

    }

}
