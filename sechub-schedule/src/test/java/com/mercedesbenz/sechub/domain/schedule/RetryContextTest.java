// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.schedule;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class RetryContextTest {

    @Before
    public void before() {

    }

    @Test
    public void max_is_zero_but_never_executionFailed_so_retry_NOT_possible() {
        /* prepare */
        RetryContext retryToTest = new RetryContext(0);

        /* execute / test */
        assertFalse(retryToTest.isRetryPossible());
    }

    @Test
    public void max_is_1_but_never_executionFailed_so_retry_possible() {
        /* prepare */
        RetryContext retryToTest = new RetryContext(1);

        /* execute / test */
        assertTrue(retryToTest.isRetryPossible());
    }

    @Test
    public void max_is_n1_but_never_executionFailed_so_retry_NOT_possible() {
        /* prepare */
        RetryContext retryToTest = new RetryContext(-1);

        /* execute / test */
        assertFalse(retryToTest.isRetryPossible());
    }

    @Test
    public void max_is_0_additional_one_executionFailed_so_retry_NOT_possible() {
        /* prepare */
        RetryContext retryToTest = new RetryContext(0);

        retryToTest.executionFailed();

        /* execute / test */
        assertFalse(retryToTest.isRetryPossible());
    }

    @Test
    public void max_is_1_one_executionFailed_so_retry_NOT_possible() {
        /* prepare */
        RetryContext retryToTest = new RetryContext(1);

        retryToTest.executionFailed();

        /* execute / test */
        assertFalse(retryToTest.isRetryPossible());
    }

    @Test
    public void max_is_1_two_executionFailed_so_retry_NOT_possible() {
        /* prepare */
        RetryContext retryToTest = new RetryContext(1);

        retryToTest.executionFailed();
        retryToTest.executionFailed();

        /* execute / test */
        assertFalse(retryToTest.isRetryPossible());
    }

    @Test
    public void max_is_2_one_executionFailed_so_retry_possible() {
        /* prepare */
        RetryContext retryToTest = new RetryContext(2);

        retryToTest.executionFailed();

        /* execute / test */
        assertTrue(retryToTest.isRetryPossible());
    }

    @Test
    public void max_is_2_two_executionFailed_so_retry_not_possible() {
        /* prepare */
        RetryContext retryToTest = new RetryContext(2);

        retryToTest.executionFailed();
        retryToTest.executionFailed();

        /* execute / test */
        assertFalse(retryToTest.isRetryPossible());
    }

    @Test
    public void retry_done_with_time_to_wait_100_waits_at_least_200_ms() {
        /* prepare */
        RetryContext retryToTest = new RetryContext(2);
        retryToTest.setRetryTimeToWait(100);
        long time1 = System.currentTimeMillis();
        retryToTest.executionFailed();
        retryToTest.executionFailed();
        long elapsed = System.currentTimeMillis() - time1;

        /* execute / test */
        assertTrue("Time elapsed:" + elapsed, elapsed >= 200);
    }

    @Test
    public void _2_retries_done_with_time_to_wait_10ms_waits_not_longer_than_50_ms() {
        /* prepare */
        RetryContext retryToTest = new RetryContext(2);
        retryToTest.setRetryTimeToWait(10);
        long time1 = System.currentTimeMillis();
        retryToTest.executionFailed();
        retryToTest.executionFailed();
        long elapsed = System.currentTimeMillis() - time1;

        /* execute / test */
        assertTrue("Time elapsed:" + elapsed, elapsed <= 50);
    }

    @Test
    public void max_is_2_none_executionFailed_but_done_called_so_no_retry_possible() {
        /* prepare */
        RetryContext retryToTest = new RetryContext(2);

        /* check preconditions - getter call has no influences at all */
        assertTrue(retryToTest.isRetryPossible());
        assertTrue(retryToTest.isRetryPossible());
        assertTrue(retryToTest.isRetryPossible());

        /* execute */
        retryToTest.executionDone();

        /* test */
        assertFalse(retryToTest.isRetryPossible());
    }

}
