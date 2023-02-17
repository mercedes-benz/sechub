// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.statistic.job;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import com.mercedesbenz.sechub.domain.statistic.StatisticDataKey;

class JobStatisticDataTypeTest {

    @EnumSource(UploadJobStatisticDataKeys.class)
    @ParameterizedTest
    void upload_statistic_data_keys_are_accepted(StatisticDataKey key) {
        assertTrue(JobStatisticDataType.UPLOAD_SOURCES.isKeyAccepted(key));
    }

    @EnumSource(AnalyticStatisticDataKey.class)
    @ParameterizedTest
    void loc_job_run_data_keys_are_not_accepted(StatisticDataKey key) {
        assertFalse(JobStatisticDataType.UPLOAD_SOURCES.isKeyAccepted(key));
    }

}
