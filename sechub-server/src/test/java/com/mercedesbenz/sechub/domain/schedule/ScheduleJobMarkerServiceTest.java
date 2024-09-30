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
import com.mercedesbenz.sechub.domain.schedule.strategy.SchedulerNextJobResolver;

public class ScheduleJobMarkerServiceTest {

    private SecHubJobRepository jobRepository;

    private ScheduleSecHubJob secHubJob;

    private UUID uuid;

    private ScheduleJobMarkerService serviceToTest;

    private SchedulerNextJobResolver nextJobResolver;

    @Before
    public void before() throws Exception {
        serviceToTest = new ScheduleJobMarkerService();
        jobRepository = mock(SecHubJobRepository.class);
        nextJobResolver = mock(SchedulerNextJobResolver.class);
        uuid = UUID.randomUUID();

        serviceToTest.jobRepository = jobRepository;
        serviceToTest.nextJobResolver = nextJobResolver;

        secHubJob = mock(ScheduleSecHubJob.class);

        when(jobRepository.getJobWhenExecutable(uuid)).thenReturn(Optional.of(secHubJob));
    }

    @Test
    public void markNextJobExecutedByThisPOD__calls_nextJobResolver() throws Exception {
        /* execute */
        serviceToTest.markNextJobToExecuteByThisInstance();

        /* test */
        verify(nextJobResolver).resolveNextJobUUID();
    }

    @Test
    public void markNextJobExecutedByThisPOD__next_job_found_updates_execution_state_to_started() throws Exception {
        /* prepare */
        when(nextJobResolver.resolveNextJobUUID()).thenReturn(uuid);
        when(jobRepository.save(secHubJob)).thenReturn(secHubJob);

        /* execute */
        ScheduleSecHubJob result = serviceToTest.markNextJobToExecuteByThisInstance();

        /* test */
        verify(nextJobResolver).resolveNextJobUUID();
        verify(jobRepository).save(secHubJob);

        verify(secHubJob).setStarted(any());
        verify(secHubJob).setExecutionState(eq(ExecutionState.STARTED));

        assertEquals(secHubJob, result);
    }

    @Test
    public void markNextJobExecutedByThisPOD__next_job_not_found() throws Exception {
        /* prepare */
        when(nextJobResolver.resolveNextJobUUID()).thenReturn(null);

        /* execute */
        ScheduleSecHubJob result = serviceToTest.markNextJobToExecuteByThisInstance();

        /* test */
        verify(nextJobResolver).resolveNextJobUUID();
        verifyNoInteractions(jobRepository);
        assertEquals(null,result);
    }

}
