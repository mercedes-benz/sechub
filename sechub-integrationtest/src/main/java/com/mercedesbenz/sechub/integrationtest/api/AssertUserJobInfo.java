package com.mercedesbenz.sechub.integrationtest.api;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mercedesbenz.sechub.integrationtest.internal.TestJSONHelper;

public class AssertUserJobInfo {

    private static final Logger LOG = LoggerFactory.getLogger(AssertUserJobInfo.class);

    private List<TestSecHubJobInfoForUser> list;

    private AssertUserJobInfo(List<TestSecHubJobInfoForUser> list) {
        assertNotNull(list);
        ;
        this.list = list;
    }

    public static AssertUserJobInfo assertInfo(TestSecHubJobInfoForUser info) {
        return assertInfo(Collections.singletonList(info));
    }

    public static AssertUserJobInfo assertInfo(List<TestSecHubJobInfoForUser> list) {
        return new AssertUserJobInfo(list);
    }

    public AssertUserJobInfoForJob hasJobInfoFor(UUID jobUUID) {
        return new AssertUserJobInfoForJob(jobUUID);
    }

    public class AssertUserJobInfoForJob {

        private TestSecHubJobInfoForUser info;

        private AssertUserJobInfoForJob(UUID jobUUID) {
            for (TestSecHubJobInfoForUser info : list) {
                if (jobUUID.equals(info.jobUUID)) {
                    this.info = info;
                    break;
                }
            }
            if (this.info == null) {
                dumpInfoAndfailWith("Did not contain a job with uud:" + jobUUID);
            }
        }

        public AssertUserJobInfoForJob withOneOfAllolowedExecutionState(String... acceptedStates) {
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
        String json = TestJSONHelper.get().createJSON(list, true);
        LOG.info("DUMP user job info:\n{}", json);

        fail(message + "\nJson was:\n" + json);
    }

    public AssertUserJobInfo hasEntries(int expected) {
        if (list.size() != expected) {
            dumpInfoAndfailWith("Not expected amount of job info. Expected:" + expected + ", but was:" + list.size());
        }
        return this;
    }

}
