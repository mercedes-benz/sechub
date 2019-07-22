// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.schedule;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;

import com.daimler.sechub.domain.schedule.job.ScheduleSecHubJob;
import com.daimler.sechub.sharedkernel.cluster.ClusterEnvironmentService;

public class SecHubJobBatchTriggerServiceTest {

	private SchedulerJobBatchTriggerService serviceToTest;

	private ScheduleJobLauncherService launcherService;
	private ScheduleJobMarkerService markerService;
	private ClusterEnvironmentService environmentService;
	
	@Before
	public void before() throws Exception {
		serviceToTest = new SchedulerJobBatchTriggerService();

		launcherService = mock(ScheduleJobLauncherService.class);
		markerService = mock(ScheduleJobMarkerService.class);
		environmentService = mock(ClusterEnvironmentService.class);

		serviceToTest.launcherService=launcherService;
		serviceToTest.markerService=markerService;
		serviceToTest.environmentService=environmentService;
		
	}

	@Test
	public void triggerExecutionOfNextJob__calls_marker_service_markNextJobExecutedByThisPOD() throws Exception {
		/* prepare */

		/* execute */
		serviceToTest.triggerExecutionOfNextJob();

		/* test */
		verify(markerService).markNextJobExecutedByThisPOD();
	}

	@Test
	public void when_marker_service_returns_job_launcher_service_is_called()
			throws Exception {
		/* prepare */
		ScheduleSecHubJob job = mock(ScheduleSecHubJob.class);
		when(markerService.markNextJobExecutedByThisPOD()).thenReturn(job);

		/* execute */
		serviceToTest.triggerExecutionOfNextJob();

		/* test */
		verify(launcherService).executeJob(job);
	}
	
	@Test
	public void when_marker_service_returns_NO_job_launcher_service_is_NOT_called()
			throws Exception {
		/* prepare */
		when(markerService.markNextJobExecutedByThisPOD()).thenReturn(null);

		/* execute */
		serviceToTest.triggerExecutionOfNextJob();

		/* test */
		verify(launcherService,never()).executeJob(any());
	}

}
