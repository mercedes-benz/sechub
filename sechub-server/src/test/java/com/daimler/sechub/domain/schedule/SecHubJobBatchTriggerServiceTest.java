// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.schedule;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;

import com.daimler.sechub.domain.schedule.config.SchedulerConfigService;
import com.daimler.sechub.domain.schedule.job.ScheduleSecHubJob;
import com.daimler.sechub.sharedkernel.cluster.ClusterEnvironmentService;
import com.daimler.sechub.sharedkernel.monitoring.SystemMonitorService;

public class SecHubJobBatchTriggerServiceTest {

	private SchedulerJobBatchTriggerService serviceToTest;

	private ScheduleJobLauncherService launcherService;
	private ScheduleJobMarkerService markerService;
	private ClusterEnvironmentService environmentService;

	private SchedulerConfigService configService;

    private SystemMonitorService monitoringService;

	@Before
	public void before() throws Exception {
		serviceToTest = new SchedulerJobBatchTriggerService();

		launcherService = mock(ScheduleJobLauncherService.class);
		markerService = mock(ScheduleJobMarkerService.class);
		environmentService = mock(ClusterEnvironmentService.class);
		configService=mock(SchedulerConfigService.class);
		monitoringService=mock(SystemMonitorService.class);
		
		serviceToTest.launcherService=launcherService;
		serviceToTest.markerService=markerService;
		serviceToTest.environmentService=environmentService;
		serviceToTest.configService=configService;
		serviceToTest.monitorService=monitoringService;


	}


	@Test
	public void triggerExecutionOfNextJob__calls_marker_service_markNextJobExecutedByThisPOD() throws Exception {
		/* prepare */
		when(configService.isJobProcessingEnabled()).thenReturn(true);

		/* execute */
		serviceToTest.triggerExecutionOfNextJob();

		/* test */
		verify(markerService).markNextJobToExecuteByThisInstance();
	}

	@Test
	public void when_processsing_is_disabled_but_marker_service_returns_job_launcher_service_is_NOT_called()
			throws Exception {
		/* prepare */
		ScheduleSecHubJob job = mock(ScheduleSecHubJob.class);
		when(markerService.markNextJobToExecuteByThisInstance()).thenReturn(job);
		when(configService.isJobProcessingEnabled()).thenReturn(false);

		/* execute */
		serviceToTest.triggerExecutionOfNextJob();

		/* test */
		verify(launcherService,never()).executeJob(job);
	}

	@Test
	public void when_marker_service_returns_job_launcher_service_is_called()
			throws Exception {
		/* prepare */
		ScheduleSecHubJob job = mock(ScheduleSecHubJob.class);
		when(markerService.markNextJobToExecuteByThisInstance()).thenReturn(job);
		when(configService.isJobProcessingEnabled()).thenReturn(true);

		/* execute */
		serviceToTest.triggerExecutionOfNextJob();

		/* test */
		verify(launcherService).executeJob(job);
	}

	@Test
	public void when_marker_service_returns_NO_job_launcher_service_is_NOT_called()
			throws Exception {
		/* prepare */
		when(configService.isJobProcessingEnabled()).thenReturn(true);
		when(markerService.markNextJobToExecuteByThisInstance()).thenReturn(null);

		/* execute */
		serviceToTest.triggerExecutionOfNextJob();

		/* test */
		verify(launcherService,never()).executeJob(any());
	}

}
