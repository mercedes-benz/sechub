// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.administration.job;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.mercedesbenz.sechub.sharedkernel.messaging.JobMessage;
import com.mercedesbenz.sechub.sharedkernel.validation.UserInputAssertion;

class JobInformationCreateServiceTest {

    private JobInformationCreateService serviceToTest;
    private JobInformationRepository repository;
    private UserInputAssertion assertion;
    private UUID jobUUID;

    @BeforeEach
    void beforeEach() {

        jobUUID = UUID.randomUUID();

        repository = mock(JobInformationRepository.class);
        assertion = mock(UserInputAssertion.class);

        serviceToTest = new JobInformationCreateService();

        serviceToTest.repository = repository;
        serviceToTest.assertion = assertion;
    }

    @Test
    void createByMessage_no_existing_entity_new_entry_will_be_created_and_saved() {
        /* prepare */
        String configuration = "{ dummy }";

        when(repository.findById(jobUUID)).thenReturn(Optional.empty());

        LocalDateTime since = LocalDateTime.now();

        JobMessage message = new JobMessage();
        message.setJobUUID(jobUUID);
        message.setOwner("newOwner");
        message.setProjectId("newProjectId");
        message.setSince(since);
        message.setConfiguration(configuration);

        JobStatus status = JobStatus.RUNNING;

        /* execute */
        serviceToTest.createByMessage(message, status);

        /* test */
        verify(repository).findById(jobUUID);

        ArgumentCaptor<JobInformation> captor = ArgumentCaptor.forClass(JobInformation.class);
        verify(repository).save(captor.capture());

        JobInformation savedJobInformation = captor.getValue();

        assertEquals(jobUUID, savedJobInformation.getJobUUID());
        assertEquals("newOwner", savedJobInformation.getOwner());
        assertEquals("newProjectId", savedJobInformation.getProjectId());
        assertEquals(since, savedJobInformation.getSince());
        assertEquals(configuration, savedJobInformation.getConfiguration());
        assertEquals(null, savedJobInformation.version);

        assertEquals(status, savedJobInformation.getStatus());

    }

    @Test
    void createByMessage_an_existing_entity_entry_will_be_reused_and_saved() {
        /* prepare */
        String configuration = "{ dummy }";

        JobInformation existingJobInfo = new JobInformation(jobUUID);
        existingJobInfo.version = Integer.valueOf(42);

        when(repository.findById(jobUUID)).thenReturn(Optional.ofNullable(existingJobInfo));

        LocalDateTime since = LocalDateTime.now();

        JobMessage message = new JobMessage();
        message.setJobUUID(jobUUID);
        message.setOwner("newOwner");
        message.setProjectId("newProjectId");
        message.setSince(since);
        message.setConfiguration(configuration);

        JobStatus status = JobStatus.RUNNING;

        /* execute */
        serviceToTest.createByMessage(message, status);

        /* test */
        verify(repository).findById(jobUUID);

        ArgumentCaptor<JobInformation> captor = ArgumentCaptor.forClass(JobInformation.class);
        verify(repository).save(captor.capture());

        JobInformation savedJobInformation = captor.getValue();

        assertEquals(existingJobInfo, savedJobInformation);
        assertEquals(jobUUID, savedJobInformation.getJobUUID());
        assertEquals("newOwner", savedJobInformation.getOwner());
        assertEquals("newProjectId", savedJobInformation.getProjectId());
        assertEquals(since, savedJobInformation.getSince());
        assertEquals(configuration, savedJobInformation.getConfiguration());
        assertEquals(42, savedJobInformation.version.intValue());

        assertEquals(status, savedJobInformation.getStatus());

    }

}
