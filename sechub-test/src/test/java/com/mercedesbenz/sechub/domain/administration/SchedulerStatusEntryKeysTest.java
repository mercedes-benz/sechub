// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.administration;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import com.mercedesbenz.sechub.commons.model.job.ExecutionState;
import com.mercedesbenz.sechub.domain.administration.scheduler.SchedulerStatusEntryKeys;

class SchedulerStatusEntryKeysTest {

    @ParameterizedTest
    @EnumSource(ExecutionState.class)
    void executionStates_are_all_mapped_to_corresponding_status_keys(ExecutionState state) {
        boolean found = false;
        String expectedStatusEntryKey = "status.scheduler.jobs." + state.name().toLowerCase();

        for (SchedulerStatusEntryKeys key : SchedulerStatusEntryKeys.values()) {
            if (expectedStatusEntryKey.equals(key.getStatusEntryKey())) {
                found = true;
                break;
            }
        }
        if (!found) {
            fail("The expected status key:" + expectedStatusEntryKey + " is not found inside SchedulerStatusEntryKeys enum");
        }
    }

}
