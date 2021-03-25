// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.schedule.strategy;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class SchedulerStrategyFactoryTest {

    private SchedulerStrategyFactory factory = new SchedulerStrategyFactory();
    
    @Test
    void test_strategy_id_set_to_known_value_fifo() {
        /* prepare */
        ReflectionTestUtils.setField(factory, "strategyId", "first-come-first-serve");
        
        /* execute */
        SchedulerStrategy strategy = factory.build();
        
        assertEquals(FirstComeFirstServeSchedulerStrategy.class, strategy.getClass());
    }
    
    @Test
    void test_strategy_id_set_to_known_value_one_scan_per_project() {
        /* prepare */
        ReflectionTestUtils.setField(factory, "strategyId", "only-one-scan-per-project-at-a-time");
        
        /* execute */
        SchedulerStrategy strategy = factory.build();
        
        assertEquals(OnlyOneScanPerProjectAtSameTimeStrategy.class, strategy.getClass());
    }
    
    @Test
    void test_strategy_id_set_to_unknown_value() {
        /* prepare */
        ReflectionTestUtils.setField(factory, "strategyId", "");
        
        /* execute */
        SchedulerStrategy strategy = factory.build();
        
        assertEquals(FirstComeFirstServeSchedulerStrategy.class, strategy.getClass());
    }

}
