// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.schedule.strategy;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SchedulerStrategyFactoryTest {

    private static final String FIFO_STRATEGY_ID = "first-come-first-serve";
    private static final String OOSP_STRATEGY_ID = "only-one-scan-per-project-at-a-time";
    private static final String OOSPAG_STRATEGY_ID = "only-one-scan-per-project-and-module-group";

    private SchedulerStrategyFactory factoryToTest;
    private FirstComeFirstServeSchedulerStrategy fifoStrategy;
    private OnlyOneScanPerProjectAtSameTimeStrategy oosppStrategy;
    private OnlyOneScanPerProjectAndModuleGroupAtSameTimeStrategy oosppagStrategy;

    @BeforeEach
    void beforeEach() {

        fifoStrategy = mock(FirstComeFirstServeSchedulerStrategy.class);
        oosppStrategy = mock(OnlyOneScanPerProjectAtSameTimeStrategy.class);
        oosppagStrategy = mock(OnlyOneScanPerProjectAndModuleGroupAtSameTimeStrategy.class);

        factoryToTest = new SchedulerStrategyFactory();

        factoryToTest.fifoStrategy = fifoStrategy;
        factoryToTest.oosppStrategy = oosppStrategy;
        factoryToTest.oosppagStrategy = oosppagStrategy;
    }

    @Test
    void strategy_id_set_to_known_value_fifo() {
        /* prepare */
        factoryToTest.setStrategyIdentifier(FIFO_STRATEGY_ID);

        /* execute */
        SchedulerStrategy strategy = factoryToTest.build();

        /* test */
        assertEquals(strategy, fifoStrategy);
    }

    @Test
    void strategy_id_set_to_known_value_one_scan_per_project() {
        /* prepare */
        factoryToTest.setStrategyIdentifier(OOSP_STRATEGY_ID);

        /* execute */
        SchedulerStrategy strategy = factoryToTest.build();

        /* test */
        assertEquals(strategy, oosppStrategy);
    }

    @Test
    void strategy_id_set_to_known_value_one_scan_per_project_and_group() {
        /* prepare */
        factoryToTest.setStrategyIdentifier(OOSPAG_STRATEGY_ID);

        /* execute */
        SchedulerStrategy strategy = factoryToTest.build();

        /* test */
        assertEquals(strategy, oosppagStrategy);
    }

    @Test
    void strategy_id_set_to_epmpty_string() {
        /* prepare */
        factoryToTest.setStrategyIdentifier("");

        /* execute */
        SchedulerStrategy strategy = factoryToTest.build();

        /* test */
        assertEquals(strategy, fifoStrategy);
    }

    @Test
    void strategy_id_set_to_null() {
        /* prepare */
        factoryToTest.setStrategyIdentifier(null);

        /* execute */
        SchedulerStrategy strategy = factoryToTest.build();

        /* test */
        assertEquals(strategy, fifoStrategy);
    }

    @Test
    void set_strategy_id_default_for_integrationtests() {
        /* execute */
        factoryToTest.setStrategyIdentifier("123");

        /* execute */
        SchedulerStrategy result = factoryToTest.build();

        /* test */
        assertEquals(result, fifoStrategy);
    }

    @Test
    void set_strategy_id_fifo_for_integrationtests() {
        /* execute */
        factoryToTest.setStrategyIdentifier(FIFO_STRATEGY_ID);

        /* execute */
        SchedulerStrategy result = factoryToTest.build();

        /* test */
        assertEquals(result, fifoStrategy);
    }

    @Test
    void set_strategy_id_only_one_scan_per_project_for_integrationtests() {

        /* prepare */
        factoryToTest.setStrategyIdentifier(OOSP_STRATEGY_ID);

        /* execute */
        SchedulerStrategy result = factoryToTest.build();

        /* test */
        assertEquals(result, oosppStrategy);
    }

}
