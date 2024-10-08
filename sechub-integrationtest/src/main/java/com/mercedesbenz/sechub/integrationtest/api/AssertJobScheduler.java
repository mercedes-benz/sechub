// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.api;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

public class AssertJobScheduler<R> extends AbstractAssert {

    private static final int DEFAULT_TIMEOUT_MS = 120000; // 2 minutes max
    private TestUser user;
    private TestProject project;
    private R returnTarget;

    /**
     * Creates assert object - if user is able to fetch job list...
     *
     * @param assertUser
     */
    AssertJobScheduler(R returnTarget, TestUser user, TestProject project) {
        this.user = user;
        this.project = project;
        this.returnTarget = returnTarget;
    }

    public static void assertJobSchedulerEnabled() {
        assertJobSchedulerEnabled(true);
    }

    public static void assertJobSchedulerDisabled() {
        assertJobSchedulerEnabled(false);
    }

    private static void assertJobSchedulerEnabled(boolean enabled) {
        TestAPI.refreshStatusEntries();

        int maxTries = 20;
        boolean foundEnabled = !enabled;
        int tries = 0;
        while (foundEnabled != enabled) {
            tries++;
            if (tries > maxTries) {
                assertEquals("" + enabled, foundEnabled);
                return;
            }
            TestAPI.waitMilliSeconds(300);
            Map<String, String> entries = TestAPI.listStatusEntries();
            String value = entries.get("status.scheduler.enabled");
            foundEnabled = Boolean.valueOf(value);
        }

    }

    public R and() {
        return returnTarget;
    }

    public AssertSchedulerJob canFindJob(UUID jobUUID) {
        return canFindJob(jobUUID, DEFAULT_TIMEOUT_MS);
    }

    public AssertSchedulerJob canFindJob(UUID jobUUID, long timeOutInMilliseconds) {
        return new AssertSchedulerJob(jobUUID, true, timeOutInMilliseconds);
    }

    public AssertJobScheduler<R> canNotFindJob(UUID jobUUID) {
        return canNotFindJob(jobUUID, DEFAULT_TIMEOUT_MS);
    }

    public AssertJobScheduler<R> canNotFindJob(UUID jobUUID, long timeOutInMilliseconds) {
        new AssertSchedulerJob(jobUUID, false, timeOutInMilliseconds);
        return this;
    }

    public enum TestExecutionResult {

        NONE,

        OK,

        FAILED,;
    }

    public enum TestExecutionState {

        INITIALIZING,

        READY_TO_START,

        STARTED,

        CANCEL_REQUESTED,

        SUSPENDED,

        ENDED;

    }

    public class AssertSchedulerJob {

        private String json;

        public AssertSchedulerJob(UUID jobUUID, boolean expected, long timeOutInMilliseconds) {
            internalCheck(jobUUID, expected, timeOutInMilliseconds);
        }

        private void internalCheck(UUID jobUUID, boolean expected, long timeOutInMilliseconds) {
            long start = System.currentTimeMillis();
            boolean timeElapsed = false;
            while (!timeElapsed) { /* NOSONAR */

                long waitedTimeInMilliseconds = System.currentTimeMillis() - start;
                timeElapsed = waitedTimeInMilliseconds > timeOutInMilliseconds;

                json = getRestHelper(user).getJSON(getUrlBuilder().buildGetJobStatusUrl(project.getProjectId(), jobUUID.toString()));
                /* very simple ... maybe this should be improved... */
                boolean found = json != null && json.contains("\"" + jobUUID);
                if (expected) {
                    if (found) {
                        /* oh found - done */
                        break;
                    } else if (timeElapsed) {
                        fail("JSON did not contain:\n" + jobUUID + "\nwas:\n" + json + "\n (waited :" + waitedTimeInMilliseconds + " milliseconds!)");
                    }
                } else {
                    if (!found) {
                        /* oh not found - done */
                        break;
                    } else if (timeElapsed) {
                        fail("JSON DID contain:\n" + jobUUID + "\nwas:\n" + json + "\n (waited :" + waitedTimeInMilliseconds + " milliseconds!)");
                    }
                }
                TestAPI.waitMilliSeconds(500);
            }
        }

        /**
         * Means one the given states is current state of job
         *
         * @param executionStates
         * @return
         */
        public AssertSchedulerJob havingOneOfExecutionStates(TestExecutionState... executionStates) {
            boolean foundOne = false;
            for (TestExecutionState state : executionStates) {
                if (json.contains(state.name())) {
                    foundOne = true;
                    break;
                }

            }
            if (!foundOne) {
                fail("Job data contains not any of " + Arrays.asList(executionStates) + " but:\n" + json);
            }
            return this;
        }

        public AssertSchedulerJob havingExecutionState(TestExecutionState state) {
            if (!hasExecutionState(state)) {
                fail("Job data contains not " + state.name() + " but:\n" + json);
            }
            return this;
        }

        boolean hasExecutionState(TestExecutionState state) {
            return json.contains(state.name());
        }

        public AssertSchedulerJob havingExecutionResult(TestExecutionResult result) {
            if (!json.contains(result.name())) {
                fail("Job data contains not " + result.name() + " but:\n" + json);
            }
            return this;
        }

        public R and() {
            return AssertJobScheduler.this.and();
        }
    }

}
