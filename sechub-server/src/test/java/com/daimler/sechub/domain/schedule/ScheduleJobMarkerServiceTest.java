// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.schedule;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import com.daimler.sechub.domain.schedule.job.ScheduleSecHubJob;
import com.daimler.sechub.domain.schedule.job.SecHubJobRepository;

public class ScheduleJobMarkerServiceTest {

	private SecHubJobRepository jobRepository;

	private ScheduleSecHubJob secHubJob;

	private UUID uuid;

	private ScheduleJobMarkerService serviceToTest;

	@Before
	public void before() throws Exception {
		serviceToTest = new ScheduleJobMarkerService();

		uuid = UUID.randomUUID();

		jobRepository = mock(SecHubJobRepository.class);

		serviceToTest.jobRepository = jobRepository;
		
		secHubJob = mock(ScheduleSecHubJob.class);
		
		when(secHubJob.getUUID()).thenReturn(uuid);
	}

	@Test
	public void markNextJobExecutedByThisPOD__calls_jobrepository_findNextJobToExecute() throws Exception {
		/* prepare */
		ScheduleSecHubJob job = mock(ScheduleSecHubJob.class);
		when(job.getUUID()).thenReturn(UUID.randomUUID());
		when(jobRepository.findNextJobToExecute()).thenReturn(Optional.of(job));

		/* execute */
		serviceToTest.markNextJobExecutedByThisPOD();

		/* test */
		verify(jobRepository).findNextJobToExecute();
	}
	
	@Test
	public void markNextJobExecutedByThisPOD__updates_execution_state_to_started() throws Exception {
		/* prepare */
		ScheduleSecHubJob job = mock(ScheduleSecHubJob.class);
		when(job.getUUID()).thenReturn(UUID.randomUUID());
		when(jobRepository.findNextJobToExecute()).thenReturn(Optional.of(job));
		when(jobRepository.save(job)).thenReturn(job);

		/* execute */
		ScheduleSecHubJob result = serviceToTest.markNextJobExecutedByThisPOD();

		/* test */
		verify(job).setStarted(any());
		verify(job).setExecutionState(eq(ExecutionState.STARTED));
		
		assertEquals(job,result);
	}

}
