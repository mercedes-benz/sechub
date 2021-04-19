// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.schedule.strategy;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class SchedulerStrategyFactoryTest {
    
    private String fifo = "first-come-first-serve";
    private String oospp = "only-one-scan-per-project-at-a-time"; 

    private SchedulerStrategyFactory factory = new SchedulerStrategyFactory();
    
    @Test
    void test_strategy_id_set_to_known_value_fifo() {
        /* prepare */
        ReflectionTestUtils.setField(factory, "strategyId", fifo);
        
        /* execute */
        SchedulerStrategy strategy = factory.build();
        
        /* test */
        assertEquals(FirstComeFirstServeSchedulerStrategy.class, strategy.getClass());
    }
    
    @Test
    void test_strategy_id_set_to_known_value_one_scan_per_project() {
        /* prepare */
        ReflectionTestUtils.setField(factory, "strategyId", oospp);
        
        /* execute */
        SchedulerStrategy strategy = factory.build();
        
        /* test */
        assertEquals(OnlyOneScanPerProjectAtSameTimeStrategy.class, strategy.getClass());
    }
    
    @Test
    void test_strategy_id_set_to_unknown_value() {
        /* prepare */
        ReflectionTestUtils.setField(factory, "strategyId", "");
        
        /* execute */
        SchedulerStrategy strategy = factory.build();
        
        /* test */
        assertEquals(FirstComeFirstServeSchedulerStrategy.class, strategy.getClass());
    }
    
    @Test
    void test_set_strategy_id_default_for_integrationtests() {
        /* execute */
        factory.setStrategyId("123");
        
        /* test */
        assertEquals(FirstComeFirstServeSchedulerStrategy.class, factory.build().getClass());
    }
    
    @Test
    void test_set_strategy_id_fifo_for_integrationtests() {
        /* execute */
        factory.setStrategyId(fifo);
        
        /* test */
        assertEquals(FirstComeFirstServeSchedulerStrategy.class, factory.build().getClass());
    }

    @Test
    void test_set_strategy_id_only_one_scan_per_project_for_integrationtests() {
        /* execute */
        factory.setStrategyId(oospp);
        
        /* test */
        assertEquals(FirstComeFirstServeSchedulerStrategy.class, factory.build().getClass());
    }
    
}
