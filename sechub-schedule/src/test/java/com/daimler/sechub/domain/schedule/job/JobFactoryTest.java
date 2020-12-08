// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.schedule.job;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.daimler.sechub.sharedkernel.UserContextService;
import com.daimler.sechub.sharedkernel.configuration.SecHubConfiguration;
import com.daimler.sechub.test.junit4.ExpectedExceptionFactory;

public class JobFactoryTest {

	private SecHubJobFactory factoryToTest;
	private SecHubConfiguration configuration;

	@Rule
	public ExpectedException expected = ExpectedExceptionFactory.none();
	
	@Before
	public void before() {
		factoryToTest = new SecHubJobFactory();
		configuration = mock(SecHubConfiguration.class);
		factoryToTest.userContextService=mock(UserContextService.class);
	}

	@Test
	public void new_jobs_have_creation_time_stamp() throws Exception {
		/* prepare */
		when(factoryToTest.userContextService.getUserId()).thenReturn("hugo");

		/* execute */
		ScheduleSecHubJob job = factoryToTest.createJob(configuration);

		/* test */
		assertNotNull(job);
		assertNotNull(job.getCreated());

	}
	
	@Test
	public void factory_throws_illegal_state_exception_when_no_user() throws Exception {
		/* prepare */
		when(factoryToTest.userContextService.getUserId()).thenReturn(null);

		/* test */
		expected.expect(IllegalStateException.class);
		
		/* execute */
		factoryToTest.createJob(configuration);


	}

}
