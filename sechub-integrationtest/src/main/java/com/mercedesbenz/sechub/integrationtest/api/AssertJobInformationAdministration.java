// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.api;

import static org.junit.Assert.*;

import java.util.List;
import java.util.UUID;

import com.mercedesbenz.sechub.commons.model.JSONConverter;
import com.mercedesbenz.sechub.domain.administration.job.JobInformation;
import com.mercedesbenz.sechub.domain.administration.job.JobStatus;

public class AssertJobInformationAdministration<R> extends AbstractAssert {

    private static final int DEFAULT_TIMEOUT_MS = 6000;
    private TestUser user;
    private R returnTarget;

    /**
     * Creates assert object - if user is able to fetch job list...
     *
     * @param user
     */
    public AssertJobInformationAdministration(R returnTarget, TestUser user) {
        this.user = user;
        this.returnTarget = returnTarget;
    }

    public R and() {
        return returnTarget;
    }

    public AssertJobInformation canFindRunningJob(UUID jobUUID) {
        return canFindRunningJob(jobUUID, DEFAULT_TIMEOUT_MS);
    }

    public AssertJobInformation canFindRunningJob(UUID jobUUID, long timeOutInMilliseconds) {
        return new AssertJobInformation(jobUUID, true, timeOutInMilliseconds);
    }

    public AssertJobInformationAdministration<R> canNotFindRunningJob(UUID jobUUID) {
        return canNotFindRunningJob(jobUUID, DEFAULT_TIMEOUT_MS);
    }

    public AssertJobInformationAdministration<R> canNotFindRunningJob(UUID jobUUID, long timeOutInMilliseconds) {
        new AssertJobInformation(jobUUID, false, timeOutInMilliseconds);
        return this;
    }

    public class AssertJobInformation {

        public AssertJobInformation(UUID jobUUID, boolean expected, long timeOutInMilliseconds) {
            internalCheck(jobUUID, expected, timeOutInMilliseconds);
        }

        private void internalCheck(UUID jobUUID, boolean expected, long timeOutInMilliseconds) {
            long start = System.currentTimeMillis();
            boolean timeElapsed = false;
            while (!timeElapsed) { /* NOSONAR */

                long waitedTimeInMilliseconds = System.currentTimeMillis() - start;
                timeElapsed = waitedTimeInMilliseconds > timeOutInMilliseconds;

                String json = getRestHelper(user).getJSON(getUrlBuilder().buildAdminFetchAllRunningJobsUrl());
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

        public R and() {
            return AssertJobInformation.this.and();
        }
    }

    public AssertJobInformationAdministration<R> canFindCreatedJob(UUID jobUUID) {
        /*
         * this is done different - the productive "listRunningJobs" REST call which is
         * used by existing asserts here would not list the CREATED parts! So we
         * implement this here special
         */
        assertJobFoundAndInStatus(jobUUID, JobStatus.CREATED);
        return this;
    }

    private void assertJobFoundAndInStatus(UUID jobUUID, JobStatus expectedJobStatus) {
        TestAPI.executeResilient(() -> {
            JobInformation found = tryToFindJobInformation(jobUUID);
            if (found == null) {
                failWithJobInformationList("Expected status " + expectedJobStatus + " but job information was generally not found for SecHub job:" + jobUUID);
            }
            JobStatus foundStatus = found.getStatus();
            if (!expectedJobStatus.equals(foundStatus)) {
                failWithJobInformationList("Expected status " + expectedJobStatus + " but was " + foundStatus + " for SecHub job:" + jobUUID);
            }
        });
    }

    private void failWithJobInformationList(String message) {

        StringBuilder sb = new StringBuilder();
        sb.append(message);
        sb.append("\nHere the current job information list:\n");

        List<JobInformation> list = TestAPI.fetchAllJobInformationEntriesFromAdministration();
        String json = JSONConverter.get().toJSON(list, true);
        sb.append(json);

        fail(sb.toString());
    }

    private JobInformation tryToFindJobInformation(UUID jobUUID) {
        List<JobInformation> list = TestAPI.fetchAllJobInformationEntriesFromAdministration();
        for (JobInformation info : list) {
            if (jobUUID.equals(info.getJobUUID())) {
                /* found entry */
                return info;
            }
        }
        return null;

    }

}
