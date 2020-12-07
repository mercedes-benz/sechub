// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.schedule.batch;

import static org.junit.Assert.*;

import java.util.UUID;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.batch.core.JobParameters;

import com.daimler.sechub.domain.schedule.SchedulingConstants;
import com.daimler.sechub.test.junit4.ExpectedExceptionFactory;

public class SecHubBatchJobParameterBuilderTest {

    SecHubBatchJobParameterBuilder builderToTest;

    @Rule
    public ExpectedException expected = ExpectedExceptionFactory.none();

    @Before
    public void before() {
        builderToTest = new SecHubBatchJobParameterBuilder();
    }

    @Test
    public void null_throws_illegal_argument() {
        /* prepare */
        expected.expect(IllegalArgumentException.class);
        
        /* execute */
        builderToTest.buildParams(null);
    }
    
    @Test
    public void uuid_contained_in_build_params() {
        /* prepare */
        UUID uuid = UUID.randomUUID();
        
        /* execute */
        JobParameters params = builderToTest.buildParams(uuid);
        
        /* test */
        assertEquals(uuid.toString(), params.getString(SchedulingConstants.BATCHPARAM_SECHUB_UUID));
    }
    
    @Test
    public void random_value_contained_in_build_params() {
        /* prepare */
        UUID uuid = UUID.randomUUID();
        
        /* execute */
        JobParameters params = builderToTest.buildParams(uuid);
        
        /* test */
        assertNotNull(params.getString("random"));
    }

}
