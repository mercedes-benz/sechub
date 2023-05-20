// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.api;

import static org.junit.Assert.*;

import java.math.BigInteger;
import java.util.List;
import java.util.UUID;

import com.mercedesbenz.sechub.test.TestUtil;

public class AssertStatistic {

    private TestJobStatistic statisticData;
    private UUID sechubJobUUID;
    private List<TestJobStatisticData> statisticDataList;
    private List<TestJobRunStatistic> jobRunStatisticList;

    public static AssertStatistic assertStatistic(UUID sechubJobUUID) {
        return new AssertStatistic(sechubJobUUID);
    }

    private AssertStatistic(UUID sechubJobUUID) {
        this.sechubJobUUID = sechubJobUUID;
    }

    public AssertStatistic isForProject(TestProject project) {
        TestJobStatistic data = ensureJobStatistic();
        assertEquals(project.getProjectId(), data.projectId);
        return this;
    }

    /**
     * Assert data is found. When a wrong value is found it will fail directly (with
     * value compare)
     *
     * @param type
     * @param id
     * @param value different values will not directly fail, but only at the end
     *              when no no entry with the value was found at all.
     * @return assert object
     */
    public AssertStatistic hasData(String type, String id, long value) {
        return hasData(type, id, BigInteger.valueOf(value), true);
    }

    /**
     * Assert data is found
     *
     * @param type
     * @param id
     * @param value
     * @param fastFailOnWrongValue when <code>false</code> duplicated entries with
     *                             different values will not directly fail, but only
     *                             at the end when no no entry with the value was
     *                             found at all. When <code>true</code> a wrong
     *                             value is found it will fail directly (with value
     *                             compare)
     * @return assert object
     */
    public AssertStatistic hasData(String type, String id, BigInteger value, boolean fastFailOnWrongValue) {
        boolean found = false;
        List<TestJobStatisticData> list = ensureStatisticData();
        for (TestJobStatisticData data : list) {
            if (!type.equals(data.type)) {
                continue;
            }
            if (!id.equals(data.id)) {
                continue;
            }
            /* found but wrong value */
            if (fastFailOnWrongValue) {
                assertEquals("A value for given type:" + type + " and id:" + id + " was found, but was not as expected", value, data.value);
            }
            if (value.equals(data.value)) {
                found = true;
                break;
            }

        }
        if (!found) {
            failForJobData("Wanted statistic data not found: type:" + type + ", id:" + id + ", valule:" + value, list);
        }
        return this;
    }

    public AssertStatisticExecution firstExecution() {
        return execution(0);
    }

    public AssertStatisticExecution execution(int index) {
        List<TestJobRunStatistic> statisticData = ensureStatisticRunData();
        if (statisticData.size() <= index) {
            fail("The try to access execution from index:" + index + " was not possible, because we have only " + statisticData.size() + " entries");
        }
        TestJobRunStatistic jobRunStatistic = statisticData.get(index);

        return new AssertStatisticExecution(jobRunStatistic);
    }

    private List<TestJobRunStatistic> ensureStatisticRunData() {
        if (jobRunStatisticList == null) {
            jobRunStatisticList = TestAPI.fetchJobRunStatisticListForSecHubJob(sechubJobUUID);
        }
        return jobRunStatisticList;
    }

    public class AssertStatisticExecution {

        private List<TestJobRunStatisticData> statisticJobRunDataList;
        private UUID executionUUID;

        private AssertStatisticExecution(TestJobRunStatistic jobRunStatistic) {
            this.executionUUID = jobRunStatistic.executionUUID;
            if (executionUUID == null) {
                throw new IllegalStateException("Execution uuid not found, the job run statistic is not valid:" + jobRunStatistic);
            }
        }

        /**
         * Assert run data is found. When a wrong value is found it will fail directly
         * (with value compare)
         *
         * @param type
         * @param id
         * @param value different values will not directly fail, but only at the end
         *              when no no entry with the value was found at all.
         * @return assert object
         */
        public AssertStatisticExecution hasRunData(String type, String id, long value) {
            return hasRunData(type, id, BigInteger.valueOf(value), true);
        }

        /**
         * Assert run data is found
         *
         * @param type
         * @param id
         * @param value
         * @param fastFailOnWrongValue when <code>false</code> duplicated entries with
         *                             different values will not directly fail, but only
         *                             at the end when no no entry with the value was
         *                             found at all. When <code>true</code> a wrong
         *                             value is found it will fail directly (with value
         *                             compare)
         * @return assert object
         */
        public AssertStatisticExecution hasRunData(String type, String id, BigInteger value, boolean fastFailOnWrongValue) {
            boolean found = false;
            List<TestJobRunStatisticData> list = ensureStatisticRunData();
            for (TestJobRunStatisticData data : list) {
                if (!type.equals(data.type)) {
                    continue;
                }
                if (!id.equals(data.id)) {
                    continue;
                }
                /* found but wrong value */
                if (fastFailOnWrongValue) {
                    assertEquals("A value for given type:" + type + " and id:" + id + " was found, but was not as expected", value, data.value);
                }
                if (value.equals(data.value)) {
                    found = true;
                    break;
                }

            }
            if (!found) {
                failForJobRunData("Wanted statistic data not found: type:" + type + ", id:" + id + ", valule:" + value, list);
            }
            return this;
        }

        private void failForJobRunData(String message, List<TestJobRunStatisticData> data) {
            fail("Statistic failure for job run:" + sechubJobUUID + "\nExecution uuid:" + executionUUID + "\nMessage:" + message + "\nData was:\n"
                    + TestUtil.createInfoForList(data));

        }

        private List<TestJobRunStatisticData> ensureStatisticRunData() {
            if (statisticJobRunDataList == null) {
                statisticJobRunDataList = TestAPI.fetchJobRunStatisticData(executionUUID);
            }
            return statisticJobRunDataList;
        }

        public AssertStatistic and() {
            return AssertStatistic.this;
        }

        public AssertStatisticExecution hasRunData(int amount) {
            assertEquals("Amount of run data not as expected!", amount, ensureStatisticRunData().size());
            return this;
        }

        public AssertStatisticExecution hasNoRunData() {
            return hasRunData(0);
        }
    }

    /**
     * Assert data is found
     *
     * @param type
     * @param id
     * @param value
     * @param fastFailOnWrongValue when <code>false</code> duplicated entries with
     *                             different values will not directly fail, but only
     *                             at the end when no no entry with the value was
     *                             found at all. When <code>true</code> a wrong
     *                             value is found it will fail directly (with value
     *                             compare)
     * @return assert object
     */
    public AssertStatistic hasRunData(String type, String id, BigInteger value, boolean fastFailOnWrongValue) {
        boolean found = false;
        List<TestJobStatisticData> list = ensureStatisticData();
        for (TestJobStatisticData data : list) {
            if (!type.equals(data.type)) {
                continue;
            }
            if (!id.equals(data.id)) {
                continue;
            }
            /* found but wrong value */
            if (fastFailOnWrongValue) {
                assertEquals("A value for given type:" + type + " and id:" + id + " was found, but was not as expected", value, data.value);
            }
            if (value.equals(data.value)) {
                found = true;
                break;
            }

        }
        if (!found) {
            failForJobData("Wanted statistic data not found: type:" + type + ", id:" + id + ", valule:" + value, list);
        }
        return this;
    }

    private void failForJobData(String message, List<TestJobStatisticData> data) {
        fail("Statistic failure for job:" + sechubJobUUID + "\nMessage:" + message + "\nData was:\n" + TestUtil.createInfoForList(data));
    }

    private TestJobStatistic ensureJobStatistic() {
        if (statisticData == null) {
            statisticData = TestAPI.fetchJobStatistic(sechubJobUUID);
        }
        return statisticData;
    }

    private List<TestJobStatisticData> ensureStatisticData() {
        if (statisticDataList == null) {
            statisticDataList = TestAPI.fetchJobStatisticData(sechubJobUUID);
        }
        return statisticDataList;
    }

}
