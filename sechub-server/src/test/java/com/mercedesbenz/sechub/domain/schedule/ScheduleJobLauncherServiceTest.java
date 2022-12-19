// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.schedule;

import static com.mercedesbenz.sechub.domain.schedule.SchedulingConstants.*;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;

import com.mercedesbenz.sechub.domain.schedule.batch.AsyncJobLauncher;
import com.mercedesbenz.sechub.domain.schedule.batch.SecHubBatchJobParameterBuilder;
import com.mercedesbenz.sechub.domain.schedule.job.ScheduleSecHubJob;
import com.mercedesbenz.sechub.domain.schedule.job.SecHubJobRepository;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessageService;
import com.mercedesbenz.sechub.sharedkernel.messaging.MessageID;

public class ScheduleJobLauncherServiceTest {

    private static final String MOCKED_PARAM_BUILDER_SECHUB_UUID = "PARAM_UUID";

    private ScheduleJobLauncherService serviceToTest;

    private SecHubJobRepository jobRepository;
    private AsyncJobLauncher asyncJobLauncher;
    private JobExecution execution;

    private ScheduleSecHubJob secHubJob;

    private UUID uuid;

    private Job job;

    private DomainMessageService eventBus;

    private SecHubBatchJobParameterBuilder parametersbuilder;

    @Before
    public void before() throws Exception {
        serviceToTest = new ScheduleJobLauncherService();

        uuid = UUID.randomUUID();

        parametersbuilder = mock(SecHubBatchJobParameterBuilder.class);
        jobRepository = mock(SecHubJobRepository.class);
        asyncJobLauncher = mock(AsyncJobLauncher.class);
        execution = mock(JobExecution.class);
        job = mock(Job.class);
        eventBus = mock(DomainMessageService.class);

        serviceToTest.jobLauncher = asyncJobLauncher;
        serviceToTest.job = job;
        serviceToTest.eventBus = eventBus;
        serviceToTest.parameterBuilder = parametersbuilder;

        secHubJob = mock(ScheduleSecHubJob.class);

        when(secHubJob.getUUID()).thenReturn(uuid);
        when(asyncJobLauncher.run(any(Job.class), any(JobParameters.class))).thenReturn(execution);
        JobParameters fakeJobParams = mock(JobParameters.class);
        when(fakeJobParams.getString(BATCHPARAM_SECHUB_UUID)).thenReturn(MOCKED_PARAM_BUILDER_SECHUB_UUID);
        when(parametersbuilder.buildParams(any())).thenReturn(fakeJobParams);
    }

    @Test
    public void executeJob__calls_job_launcher_with_job_uuid_as_parameter() throws Exception {
        /* prepare */
        UUID jobUUID = UUID.randomUUID();

        ScheduleSecHubJob secHubJob = mock(ScheduleSecHubJob.class);
        when(secHubJob.getJsonConfiguration()).thenReturn("jsonConfig");
        when(secHubJob.getUUID()).thenReturn(jobUUID);
        when(jobRepository.findNextJobToExecute()).thenReturn(Optional.of(secHubJob));

        /* execute */
        serviceToTest.executeJob(secHubJob);

        /* test */
        ArgumentCaptor<JobParameters> captor = ArgumentCaptor.forClass(JobParameters.class);
        verify(asyncJobLauncher).run(any(Job.class), captor.capture());

        String sechubUUID = captor.getValue().getString(BATCHPARAM_SECHUB_UUID);
        assertEquals(MOCKED_PARAM_BUILDER_SECHUB_UUID, sechubUUID);
    }

    @Test
    public void executeJob__sends_domain_message_about_JOB_STARTED() throws Exception {
        /* prepare */
        UUID jobUUID = UUID.randomUUID();

        ScheduleSecHubJob secHubJob = mock(ScheduleSecHubJob.class);
        when(secHubJob.getJsonConfiguration()).thenReturn("jsonConfig");
        when(secHubJob.getUUID()).thenReturn(jobUUID);
        when(jobRepository.findNextJobToExecute()).thenReturn(Optional.of(secHubJob));

        /* execute */
        serviceToTest.executeJob(secHubJob);

        /* test */
        ArgumentCaptor<DomainMessage> message = ArgumentCaptor.forClass(DomainMessage.class);
        verify(eventBus).sendAsynchron(message.capture());

        assertEquals(MessageID.JOB_STARTED, message.getValue().getMessageId());

    }

}
