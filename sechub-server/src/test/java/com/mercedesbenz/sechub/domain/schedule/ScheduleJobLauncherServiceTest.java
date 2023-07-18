// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.schedule;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import com.mercedesbenz.sechub.domain.schedule.batch.SynchronSecHubJobExecutor;
import com.mercedesbenz.sechub.domain.schedule.job.ScheduleSecHubJob;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessageService;
import com.mercedesbenz.sechub.sharedkernel.messaging.MessageID;
import com.mercedesbenz.sechub.test.TestCanaryException;

public class ScheduleJobLauncherServiceTest {

    private ScheduleJobLauncherService serviceToTest;

    private ScheduleSecHubJob secHubJob;

    private UUID uuid;

    private DomainMessageService eventBus;

    private SynchronSecHubJobExecutor executor;

    @Before
    public void before() throws Exception {
        serviceToTest = new ScheduleJobLauncherService();

        uuid = UUID.randomUUID();

        eventBus = mock(DomainMessageService.class);
        executor = mock(SynchronSecHubJobExecutor.class);

        serviceToTest.eventBus = eventBus;
        serviceToTest.executor = executor;

        secHubJob = mock(ScheduleSecHubJob.class);

        when(secHubJob.getUUID()).thenReturn(uuid);
    }

    @Test
    public void executeJob__calls_executor_with_job_as_parameter() throws Exception {
        /* execute */
        serviceToTest.executeJob(secHubJob);

        /* test */
        verify(executor).execute(secHubJob);
    }

    @Test
    public void executeJob__sends_domain_message_about_JOB_STARTED() throws Exception {
        /* execute */
        serviceToTest.executeJob(secHubJob);

        /* test */
        ArgumentCaptor<DomainMessage> message = ArgumentCaptor.forClass(DomainMessage.class);
        verify(eventBus).sendAsynchron(message.capture());

        assertEquals(MessageID.JOB_STARTED, message.getValue().getMessageId());

    }

    @Test
    public void executeJob__does_NOT_send_any_domain_message_when_executor_throws_exception() throws Exception {

        /* prepare */
        doThrow(new TestCanaryException()).when(executor).execute(secHubJob);

        /* execute */
        assertThrows(TestCanaryException.class, () -> serviceToTest.executeJob(secHubJob));

        /* test */
        verify(eventBus, never()).sendAsynchron(any(DomainMessage.class));

    }

}
