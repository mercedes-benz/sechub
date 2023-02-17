// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.schedule.strategy;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class SchedulerStrategyIdTest {

    @Test
    void test_value_to_enum_mapping_succesful() {
        /* execute */
        SchedulerStrategyId fifoServe = SchedulerStrategyId.getByIdentifier("first-come-first-serve");
        SchedulerStrategyId onlyOneScanAtATime = SchedulerStrategyId.getByIdentifier("only-one-scan-per-project-at-a-time");

        /* test */
        assertEquals(SchedulerStrategyId.FIRST_COME_FIRST_SERVE, fifoServe);
        assertEquals(SchedulerStrategyId.ONE_SCAN_PER_PROJECT, onlyOneScanAtATime);
    }

    @Test
    void test_unknown_value_to_enum_failure() {
        /* prepare */
        SchedulerStrategyId strategyId = SchedulerStrategyId.getByIdentifier("gibberish");

        /* test */
        assertNull(strategyId);
    }

}
