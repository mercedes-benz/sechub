// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.schedule.strategy;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SchedulerStrategyProviderTest {

    private static final String FIRST_COME_FIRST_SERVE = "first-come-first-serve";
    private static final String ONLY_ONE_SCAN_PER_PROJECT = "only-one-scan-per-project-at-a-time";
    private static final String ONLY_ONE_SCAN_PER_PROJECT_AND_MODULE_GROUP = "only-one-scan-per-project-and-module-group";

    private SchedulerStrategyProvider factoryToTest;
    private FirstComeFirstServeSchedulerStrategy firstComeFirstServeStrategy;
    private OnlyOneScanPerProjectAtSameTimeStrategy onlyOneScanPerProjectStrategy;
    private OnlyOneScanPerProjectAndModuleGroupAtSameTimeStrategy onlyOneScanPerProjectAndModuleGroupStrategy;

    @BeforeEach
    void beforeEach() {

        firstComeFirstServeStrategy = mock(FirstComeFirstServeSchedulerStrategy.class);
        onlyOneScanPerProjectStrategy = mock(OnlyOneScanPerProjectAtSameTimeStrategy.class);
        onlyOneScanPerProjectAndModuleGroupStrategy = mock(OnlyOneScanPerProjectAndModuleGroupAtSameTimeStrategy.class);

        factoryToTest = new SchedulerStrategyProvider();

        factoryToTest.firstComeFirstServeStrategy = firstComeFirstServeStrategy;
        factoryToTest.onlyOneScanPerProjectStrategy = onlyOneScanPerProjectStrategy;
        factoryToTest.onlyOneScanPerProjectAndModuleGroupStrategy = onlyOneScanPerProjectAndModuleGroupStrategy;
    }

    @Test
    void strategy_id_set_to_known_value_fifo() {
        /* prepare */
        factoryToTest.setStrategyIdentifier(FIRST_COME_FIRST_SERVE);

        /* execute */
        SchedulerStrategy strategy = factoryToTest.build();

        /* test */
        assertEquals(strategy, firstComeFirstServeStrategy);
    }

    @Test
    void strategy_id_set_to_known_value_one_scan_per_project() {
        /* prepare */
        factoryToTest.setStrategyIdentifier(ONLY_ONE_SCAN_PER_PROJECT);

        /* execute */
        SchedulerStrategy strategy = factoryToTest.build();

        /* test */
        assertEquals(strategy, onlyOneScanPerProjectStrategy);
    }

    @Test
    void strategy_id_set_to_known_value_one_scan_per_project_and_group() {
        /* prepare */
        factoryToTest.setStrategyIdentifier(ONLY_ONE_SCAN_PER_PROJECT_AND_MODULE_GROUP);

        /* execute */
        SchedulerStrategy strategy = factoryToTest.build();

        /* test */
        assertEquals(strategy, onlyOneScanPerProjectAndModuleGroupStrategy);
    }

    @Test
    void strategy_id_set_to_epmpty_string() {
        /* prepare */
        factoryToTest.setStrategyIdentifier("");

        /* execute */
        SchedulerStrategy strategy = factoryToTest.build();

        /* test */
        assertEquals(strategy, firstComeFirstServeStrategy);
    }

    @Test
    void strategy_id_set_to_null() {
        /* prepare */
        factoryToTest.setStrategyIdentifier(null);

        /* execute */
        SchedulerStrategy strategy = factoryToTest.build();

        /* test */
        assertEquals(strategy, firstComeFirstServeStrategy);
    }

    @Test
    void set_strategy_id_default_for_integrationtests() {
        /* execute */
        factoryToTest.setStrategyIdentifier("123");

        /* execute */
        SchedulerStrategy result = factoryToTest.build();

        /* test */
        assertEquals(result, firstComeFirstServeStrategy);
    }

    @Test
    void set_strategy_id_fifo_for_integrationtests() {
        /* execute */
        factoryToTest.setStrategyIdentifier(FIRST_COME_FIRST_SERVE);

        /* execute */
        SchedulerStrategy result = factoryToTest.build();

        /* test */
        assertEquals(result, firstComeFirstServeStrategy);
    }

    @Test
    void set_strategy_id_only_one_scan_per_project_for_integrationtests() {

        /* prepare */
        factoryToTest.setStrategyIdentifier(ONLY_ONE_SCAN_PER_PROJECT);

        /* execute */
        SchedulerStrategy result = factoryToTest.build();

        /* test */
        assertEquals(result, onlyOneScanPerProjectStrategy);
    }

}
