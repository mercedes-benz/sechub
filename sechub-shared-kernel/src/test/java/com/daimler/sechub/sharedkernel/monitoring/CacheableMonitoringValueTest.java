// SPDX-License-Identifier: MIT
package com.daimler.sechub.sharedkernel.monitoring;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.daimler.sechub.test.junit4.ExpectedExceptionFactory;

public class CacheableMonitoringValueTest {

    private CacheableMonitoringValue valueToTest;
    public ExpectedException expected = ExpectedExceptionFactory.none();
   

    @Before
    public void before() throws Exception {
        valueToTest = new CacheableMonitoringValue(100);
    }

    @Test
    public void last_set_value_returned() {
        /* execute */
        valueToTest.setValue(1.0);
        valueToTest.setValue(1.3); // will override 1.0 even when cache time is not over

        /* test */
        assertEquals(1.3,valueToTest.getValue(),0.0);
    }
    
    @Test
    public void last_set_addtiional_data_returned() {
        /* execute */
        valueToTest.setAdditionalData("key1", "value1");
        valueToTest.setAdditionalData("key1", "value2");

        /* test */
        assertEquals("value2",valueToTest.getAdditionalData("key1"));
    }

    
    @Test
    public void newValue_has_not_valid_cache_after_initialized() throws InterruptedException {
        /* test */
        assertFalse(valueToTest.isCacheValid());
        
    }
    
    @Test
    public void isCacheValid_changed_by_time() throws InterruptedException {
        valueToTest.setValue(0.1); // do this to set cache value + mark timestamp
        /* check precondition */
        assertTrue(valueToTest.isCacheValid());
        
        /* execute - just pass time */
        Thread.sleep(105);

        /* test */
        assertFalse(valueToTest.isCacheValid());
        
    }
    
    @Test
    public void isCacheValid_changed_by_set_value() throws InterruptedException {
        /* prepare */
        Thread.sleep(105);
        
        /* check precondition */
        assertFalse(valueToTest.isCacheValid());
        
        /* execute + test */
        valueToTest.setValue(0.9); // change value must set cached value - and so cache is valid again
        assertTrue(valueToTest.isCacheValid());
        
    }
    
    
    @Test
    public void isCacheValid_changed_by_set_additional_data_and_time() throws InterruptedException {

        /* prepare */
        Thread.sleep(105);
        
        /* check precondition */
        assertFalse(valueToTest.isCacheValid());
        
        /* execute */
        valueToTest.setAdditionalData("mykey", "myvalue");
        
        /* test */
        assertTrue(valueToTest.isCacheValid());
        
    }
}
