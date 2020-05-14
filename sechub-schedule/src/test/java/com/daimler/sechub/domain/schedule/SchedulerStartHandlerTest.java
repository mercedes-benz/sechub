// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.schedule;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import com.daimler.sechub.domain.schedule.job.ScheduleSecHubJob;
import com.daimler.sechub.domain.schedule.job.SecHubJobRepository;
import com.daimler.sechub.sharedkernel.SecHubEnvironment;
import com.daimler.sechub.sharedkernel.messaging.ClusterMemberMessage;
import com.daimler.sechub.sharedkernel.messaging.DomainMessage;
import com.daimler.sechub.sharedkernel.messaging.DomainMessageService;
import com.daimler.sechub.sharedkernel.messaging.MessageDataKeys;
import com.daimler.sechub.sharedkernel.util.HostnameBuilder;

public class SchedulerStartHandlerTest {

    private static final String BASE_URL1 = "base-url";
    private static final String HOSTNAME_1 = "cluster-member-1";
    private SchedulerStartHandler handlerToTest;
    private DomainMessageService eventBus;
    private SecHubJobRepository repository;
    private HostnameBuilder hostnameBuilder;
    private SecHubEnvironment environment;

    @Before
    public void before() throws Exception {
        handlerToTest = new SchedulerStartHandler();
        eventBus=mock(DomainMessageService.class);
        repository = mock(SecHubJobRepository.class);
        hostnameBuilder=mock(HostnameBuilder.class);
        environment= mock(SecHubEnvironment.class);
        
        handlerToTest.hostnameBuilder = hostnameBuilder;
        handlerToTest.environment =environment;
        handlerToTest.repository=repository;
        handlerToTest.eventBus=eventBus;
        
        when(environment.getServerBaseUrl()).thenReturn(BASE_URL1);
        when(hostnameBuilder.buildHostname()).thenReturn(HOSTNAME_1);
    }

    @Test
    public void buildZombieInformation_noZombiesFound_remark_about_all_okay() {
        /* prepare */
        List<ScheduleSecHubJob> jobsRunningButStartedBefore = new ArrayList<>();
        
        /* execute */
        String result = handlerToTest.buildZombieInformation(jobsRunningButStartedBefore);
        
        /* test */
        assertEquals("OK: No zombie jobs found", result);
    }
    
    @Test
    public void buildZombieInformation_zombiesFound_infomrmation_with_job_UUID_is_contained() {
        /* prepare */
        List<ScheduleSecHubJob> jobsRunningButStartedBefore = new ArrayList<>();
        ScheduleSecHubJob job1 = mock(ScheduleSecHubJob.class);
        ScheduleSecHubJob job2 = mock(ScheduleSecHubJob.class);
        UUID uuid1 = UUID.randomUUID();
        UUID uuid2 = UUID.randomUUID();
        when(job1.getUUID()).thenReturn(uuid1);
        when(job2.getUUID()).thenReturn(uuid2);
        
        jobsRunningButStartedBefore.add(job1);
        jobsRunningButStartedBefore.add(job2);
        
        /* execute */
        String result = handlerToTest.buildZombieInformation(jobsRunningButStartedBefore);
        
        /* test */
        assertTrue(result.contains("ATTENTION: Potential zombie jobs found"));
        assertTrue(result.contains(uuid1.toString()));
        assertTrue(result.contains(uuid2.toString()));
    }
    
    @Test
    public void schedulerHasBeenStarted() throws Exception {
        /* prepare */
        List<ScheduleSecHubJob> jobsRunningButStartedBefore = new ArrayList<>();
        ScheduleSecHubJob job1 = mock(ScheduleSecHubJob.class);
        UUID uuid1 = UUID.randomUUID();
        when(job1.getUUID()).thenReturn(uuid1);
        
        jobsRunningButStartedBefore.add(job1);
        
        when(repository.findAllRunningJobsStartedBefore(any())).thenReturn(jobsRunningButStartedBefore);
        
        /* execute */
        handlerToTest.schedulerHasBeenStarted().run();
        
        /* test */
        ArgumentCaptor<DomainMessage> argument = ArgumentCaptor.forClass(DomainMessage.class); 
        verify(eventBus).sendAsynchron(argument.capture());
        DomainMessage domainMessage = argument.getValue();
        assertEquals(BASE_URL1, domainMessage.get(MessageDataKeys.ENVIRONMENT_BASE_URL));
        ClusterMemberMessage status = domainMessage.get(MessageDataKeys.ENVIRONMENT_CLUSTER_MEMBER_STATUS);
        assertNotNull(status);
        assertEquals(HOSTNAME_1,status.getHostName());
        
    }

}
