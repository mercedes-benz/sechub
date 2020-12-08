// SPDX-License-Identifier: MIT
package com.daimler.sechub.integrationtest.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.daimler.sechub.integrationtest.api.TestAPI;

/**
 * Sometimes we got race conditions in tests and an assertXYZ will fail. Instead
 * of coding always TestAPI.waitMilliSeconds(millis); with magic time areas
 * inside testcases (which will sometimes not work, sometimes only slow down
 * tess...), we will use this class with retry mechanism.
 * 
 * @author Albert Tregnaghi
 *
 */
public class RetryAssertionErrorRunner {

    private static final Logger LOG = LoggerFactory.getLogger(RetryAssertionErrorRunner.class);

    /**
     * Does retry all 500 milliseconds
     * 
     * @param testRunnable
     * @param retries
     * @param refreshRunnable is a special runnable which is called only after a
     *                        former check has failed and to refresh /reload a
     *                        state, if not necessary this can be null. But be
     *                        aware: when state is not refreshed a retry makes
     *                        normally no sense!
     */
    public static void runWithRetries(int retries, Runnable testRunnable, Runnable refreshRunnable) {
        runWithRetries(retries, 500, testRunnable, refreshRunnable);
    }

    /**
     * Does retry call with given time to wait and refresh runner
     * 
     * @param retries
     * @param timeToWaitBeforeFinalFail
     * @param testRunnable
     * @param refreshRunnable           is a special runnable which is called only
     *                                  after a former check has failed and to
     *                                  refresh /reload a state, if not necessary
     *                                  this can be null. But be aware: when state
     *                                  is not refreshed a retry makes normally no
     *                                  sense!
     */
    public static void runWithRetries(int retries, int timeToWaitBeforeFinalFail, Runnable testRunnable, Runnable refreshRunnable) {
        new RetryAssertionErrorRunner().start(testRunnable, retries, timeToWaitBeforeFinalFail, refreshRunnable);
    }

    private void start(Runnable testRunnable, int retries, int timeToWaitBeforeRetry, Runnable refreshRunnable) {
        int calls = retries + 1;
        int call = 0;
        AssertionError lastAssertionFailure = null;
        for (int i = 0; i < calls; i++) {
            call++;
            try {
                lastAssertionFailure = null;
                testRunnable.run();
            } catch (AssertionError failure) {
                LOG.warn("Call {} failed with message:{}", call, failure.getMessage());
                lastAssertionFailure = failure;
                if (i == calls - 1) {
                    /* last one - so no wait */
                    continue;
                }
                LOG.warn("Call {} did fail in test - will retry in {} milliseconds {}/{} times", call, timeToWaitBeforeRetry, call, retries);
                TestAPI.waitMilliSeconds(timeToWaitBeforeRetry);
                if (refreshRunnable != null) {
                    LOG.info("Calling refresh runnable to obtain new state");
                    refreshRunnable.run();
                } else {
                    LOG.warn(
                            "Refresh runnable is not defined - so  test runnable MUST contain refresh mechanism to obtain new state! Otherwise old state is always reused and retry will make no sense at all!");
                }
            }
        }
        if (lastAssertionFailure != null) {
            LOG.error("Did try out {} times to run with waiting between {}  - but still problems, so throw last failure", retries, timeToWaitBeforeRetry,
                    lastAssertionFailure);
            throw lastAssertionFailure;
        }

    }

}
