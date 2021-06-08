// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.schedule.strategy;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class SchedulerStrategyFactoryTest {

    private static final String FIFO_STRATEGY_ID = "first-come-first-serve";
    private static final String OOSP_STRATEGY_ID = "only-one-scan-per-project-at-a-time";

    private SchedulerStrategyFactory factoryToTest;
    private FirstComeFirstServeSchedulerStrategy fifoStrategy;
    private OnlyOneScanPerProjectAtSameTimeStrategy oosppStrategy;

    @BeforeEach
    void beforeEach() {

        fifoStrategy = mock(FirstComeFirstServeSchedulerStrategy.class);
        oosppStrategy = mock(OnlyOneScanPerProjectAtSameTimeStrategy.class);

        factoryToTest = new SchedulerStrategyFactory();

        factoryToTest.fifoStrategy = fifoStrategy;
        factoryToTest.oosppStrategy = oosppStrategy;
    }

    @Test
    void test_strategy_id_set_to_known_value_fifo() {
        /* prepare */
        ReflectionTestUtils.setField(factoryToTest, "strategyId", FIFO_STRATEGY_ID);

        /* execute */
        SchedulerStrategy strategy = factoryToTest.build();

        /* test */
        assertEquals(strategy, fifoStrategy);
    }

    @Test
    void test_strategy_id_set_to_known_value_one_scan_per_project() {
        /* prepare */
        factoryToTest.setStrategyId(OOSP_STRATEGY_ID);

        /* execute */
        SchedulerStrategy strategy = factoryToTest.build();

        /* test */
        assertEquals(strategy, oosppStrategy);
    }

    @Test
    void test_strategy_id_set_to_epmpty_string() {
        /* prepare */
        factoryToTest.setStrategyId("");

        /* execute */
        SchedulerStrategy strategy = factoryToTest.build();

        /* test */
        assertEquals(strategy, fifoStrategy);
    }
    
    @Test
    void test_strategy_id_set_to_null() {
        /* prepare */
        factoryToTest.setStrategyId(null);

        /* execute */
        SchedulerStrategy strategy = factoryToTest.build();

        /* test */
        assertEquals(strategy, fifoStrategy);
    }

    @Test
    void test_set_strategy_id_default_for_integrationtests() {
        /* execute */
        factoryToTest.setStrategyId("123");

        /* execute */
        SchedulerStrategy result = factoryToTest.build();

        /* test */
        assertEquals(result, fifoStrategy);
    }

    @Test
    void test_set_strategy_id_fifo_for_integrationtests() {
        /* execute */
        factoryToTest.setStrategyId(FIFO_STRATEGY_ID);

        /* execute */
        SchedulerStrategy result = factoryToTest.build();

        /* test */
        assertEquals(result, fifoStrategy);
    }

    @Test
    void test_set_strategy_id_only_one_scan_per_project_for_integrationtests() {

        /* prepare */
        factoryToTest.setStrategyId(OOSP_STRATEGY_ID);

        /* execute */
        SchedulerStrategy result = factoryToTest.build();

        /* test */
        assertEquals(result, oosppStrategy);
    }

}
