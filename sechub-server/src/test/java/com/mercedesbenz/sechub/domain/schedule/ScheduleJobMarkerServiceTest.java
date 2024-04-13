// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.schedule;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import com.mercedesbenz.sechub.commons.model.job.ExecutionState;
import com.mercedesbenz.sechub.domain.schedule.job.ScheduleSecHubJob;
import com.mercedesbenz.sechub.domain.schedule.job.SecHubJobRepository;
import com.mercedesbenz.sechub.domain.schedule.strategy.FirstComeFirstServeSchedulerStrategy;
import com.mercedesbenz.sechub.domain.schedule.strategy.SchedulerStrategyFactory;

public class ScheduleJobMarkerServiceTest {

    private SecHubJobRepository jobRepository;
    private SchedulerStrategyFactory factory;
    private FirstComeFirstServeSchedulerStrategy strategy;

    private ScheduleSecHubJob secHubJob;

    private UUID uuid;

    private ScheduleJobMarkerService serviceToTest;

    @Before
    public void before() throws Exception {
        serviceToTest = new ScheduleJobMarkerService();
        factory = mock(SchedulerStrategyFactory.class);
        strategy = mock(FirstComeFirstServeSchedulerStrategy.class);
        jobRepository = mock(SecHubJobRepository.class);

        uuid = UUID.randomUUID();

        serviceToTest.jobRepository = jobRepository;
        serviceToTest.schedulerStrategyFactory = factory;
        strategy.jobRepository = jobRepository;

        secHubJob = mock(ScheduleSecHubJob.class);

        when(factory.build()).thenReturn(strategy);
        when(strategy.nextJobId()).thenReturn(uuid);
        when(jobRepository.getJob(uuid)).thenReturn(Optional.of(secHubJob));
    }

    @Test
    public void markNextJobExecutedByThisPOD__calls_jobrepository_getjob_executed() throws Exception {
        /* execute */
        serviceToTest.markNextJobToExecuteByThisInstance();

        /* test */
        verify(jobRepository).getJob(uuid);
    }

    @Test
    public void markNextJobExecutedByThisPOD__updates_execution_state_to_started() throws Exception {
        /* prepare */
        when(jobRepository.save(secHubJob)).thenReturn(secHubJob);

        /* execute */
        ScheduleSecHubJob result = serviceToTest.markNextJobToExecuteByThisInstance();

        /* test */

        verify(secHubJob).setStarted(any());
        verify(secHubJob).setExecutionState(eq(ExecutionState.STARTED));

        assertEquals(secHubJob, result);
    }

}
