// SPDX-License-Identifier: MIT
package com.daimler.sechub.integrationtest.internal;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.daimler.sechub.test.TestUtil;

public class PersistentScenarioTestDataProviderTest {

    private PersistentScenarioTestDataProvider providerToTest;

    @Before
    public void before() throws Exception {
        providerToTest = new PersistentScenarioTestDataProvider(TestUtil.createTempFileInBuildFolder("sechub-scenario_testdata", "properties").toFile());
    }

    @Test
    public void new_one_is_0000() {
        assertEquals("0000", providerToTest.getGrowId());
    }
    
    @Test
    public void growed_one_time_is_0001() {
        /* execute */
        providerToTest.increaseGrowId();
        
        /* test */
        assertEquals("0001", providerToTest.getGrowId());
    }
    
    @Test
    public void when_12_times_growed_it_is_0012() {
        /* execute */
        for (int i=0;i<12;i++) {
            providerToTest.increaseGrowId();
        }
        
        /* test */
        assertEquals("0012", providerToTest.getGrowId());
    }
    
    @Test
    public void grow_id_is_reused_when_same_file() {
        /* prepare */
        providerToTest.increaseGrowId();
        providerToTest.increaseGrowId();
        providerToTest.increaseGrowId();
        /* check precondition */
        assertEquals("0003", providerToTest.getGrowId());
        
        /* must reload former state when created*/
        PersistentScenarioTestDataProvider provider2ToTest = new PersistentScenarioTestDataProvider(providerToTest.file);
        assertEquals("0003", provider2ToTest.getGrowId());
    }

}
