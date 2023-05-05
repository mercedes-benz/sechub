// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.api;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mercedesbenz.sechub.integrationtest.internal.TestJSONHelper;

public class AssertUserJobInfo {

    public static AssertUserJobInfo assertInfo(TestSecHubJobInfoForUserListPage page) {
        return new AssertUserJobInfo(page);
    }

    private static final Logger LOG = LoggerFactory.getLogger(AssertUserJobInfo.class);

    private TestSecHubJobInfoForUserListPage listPage;

    private AssertUserJobInfo(TestSecHubJobInfoForUserListPage listPage) {
        assertNotNull(listPage);
        this.listPage = listPage;
    }

    /**
     * Asserts the fetched job info list does contain an entry for the job - no
     * matter which position
     *
     * @param jobUUID
     * @return assert object
     */
    public AssertUserJobInfoForJob hasJobInfoFor(UUID jobUUID) {
        return hasJobInfoFor(jobUUID, -1);
    }

    /**
     * Asserts the fetched job info list does contain an entry for the job at the
     * expected position
     *
     * @param jobUUID
     * @param expectedPosition
     * @return assert object
     */
    public AssertUserJobInfoForJob hasJobInfoFor(UUID jobUUID, int expectedPosition) {
        return new AssertUserJobInfoForJob(jobUUID, expectedPosition);
    }

    public class AssertUserJobInfoForJob {

        private TestSecHubJobInfoForUser info;

        private AssertUserJobInfoForJob(UUID jobUUID, int expectedPosition) {
            int pos = 0;
            for (TestSecHubJobInfoForUser info : listPage.getContent()) {
                if (jobUUID.equals(info.jobUUID)) {
                    this.info = info;
                    break;
                }
                pos++;
            }
            if (this.info == null) {
                dumpInfoAndfailWith("Did not contain a job with uuid:" + jobUUID);
            }

            if (expectedPosition >= 0) {
                if (expectedPosition != pos) {
                    fail("A job info for the job with uuid:" + jobUUID + " is found, but\nposition is:" + pos + " and expected was:" + expectedPosition);
                }
            }
        }

        public AssertUserJobInfoForJob withOneOfAllowedExecutionStates(String... acceptedStates) {
            String executionState = info.executionState;

            boolean stateFound = false;

            for (String state : acceptedStates) {
                if (state.equalsIgnoreCase(executionState)) {
                    stateFound = true;
                    break;
                }

            }
            if (!stateFound) {
                fail("Execution state was:" + executionState + " - accepted where: " + Arrays.asList(acceptedStates));
            }
            return this;
        }

        public AssertUserJobInfoForJob withExecutionResult(String result) {
            assertEquals("Execution result not as expected for " + info.jobUUID, result, info.executionResult);
            return this;
        }

        public AssertUserJobInfo and() {
            return AssertUserJobInfo.this;
        }

    }

    private void dumpInfoAndfailWith(String message) {
        String json = TestJSONHelper.get().createJSON(listPage, true);
        LOG.info("DUMP user job info:\n{}", json);

        fail(message + "\nJson was:\n" + json);
    }

    public AssertUserJobInfo hasEntries(int expected) {
        List<TestSecHubJobInfoForUser> content = listPage.getContent();
        if (content.size() != expected) {
            dumpInfoAndfailWith("Not expected amount of job info. Expected:" + expected + ", but was:" + content.size());
        }
        return this;
    }

    public AssertUserJobInfo hasProjectId(String projectId) {
        if (!projectId.equals(listPage.getProjectId())) {
            dumpInfoAndfailWith("Expected projectId: " + projectId + " but projectId was: " + listPage.getProjectId());
        }
        return this;
    }

    public AssertUserJobInfo hasPage(int page) {
        if (listPage.getPage() != page) {
            dumpInfoAndfailWith("Expected pages: " + page + " but page was: " + listPage.getPage());
        }
        return this;
    }

    public AssertUserJobInfo hasTotalPages(int totalPages) {
        if (listPage.getTotalPages() != totalPages) {
            dumpInfoAndfailWith("Expected total pages: " + totalPages + " buttotalPages was: " + listPage.getTotalPages());
        }
        return this;
    }

}
