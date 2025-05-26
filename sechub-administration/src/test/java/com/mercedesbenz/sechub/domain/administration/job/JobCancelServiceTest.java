// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.administration.job;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;

import com.mercedesbenz.sechub.domain.administration.project.ProjectRepository;
import com.mercedesbenz.sechub.domain.administration.user.User;
import com.mercedesbenz.sechub.domain.administration.user.UserRepository;
import com.mercedesbenz.sechub.sharedkernel.error.NotFoundException;
import com.mercedesbenz.sechub.sharedkernel.logging.AuditLogService;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessageService;
import com.mercedesbenz.sechub.sharedkernel.validation.UserInputAssertion;

public class JobCancelServiceTest {

    private static final AuditLogService auditLogService = mock();
    private static final UserInputAssertion userInputAssertion = mock();
    private static final DomainMessageService eventBusService = mock();
    private static final JobInformationRepository jobInformationRepository = mock();
    private static final UserRepository userRepository = mock();
    private static final ProjectRepository projectRepository = mock();
    private static final JobCancelService serviceToTest = new JobCancelService(auditLogService, userInputAssertion, eventBusService, jobInformationRepository,
            userRepository, projectRepository);

    @BeforeEach
    void beforeEach() {
        Mockito.reset(userRepository, projectRepository, jobInformationRepository, eventBusService, userInputAssertion, auditLogService);
    }

    @Test
    public void userCancelJob_receives_not_found_exception_when_job_not_found() {
        /* prepare */
        UUID jobUUID = UUID.randomUUID();
        String userId = "user1";
        when(jobInformationRepository.findById(jobUUID)).thenReturn(Optional.empty());

        /* execute + test */
        assertThatThrownBy(() -> serviceToTest.userCancelJob(jobUUID, userId)).isInstanceOf(NotFoundException.class);

        verify(eventBusService, never()).sendAsynchron(any());

    }

    @Test
    public void userCancelJob_receives_not_found_exception_when_project_not_assigned() {
        /* prepare */
        UUID jobUUID = UUID.randomUUID();
        String userId = "user1";
        String projectId = "project1";
        String otherProjectId = "project2";

        JobInformation jobInformation = mock(JobInformation.class);
        when(jobInformation.getProjectId()).thenReturn(projectId);

        User user = mock(User.class);
        when(projectRepository.findAllProjectIdsWhereUserIsAssigned(userId)).thenReturn(Set.of(otherProjectId));

        when(userRepository.findOrFailUser(userId)).thenReturn(user);
        when(jobInformationRepository.findById(jobUUID)).thenReturn(Optional.of(jobInformation));

        /* execute + test */
        assertThatThrownBy(() -> serviceToTest.userCancelJob(jobUUID, userId)).isInstanceOf(NotFoundException.class);

        verify(eventBusService, never()).sendAsynchron(any());
    }

    @Test
    public void userCancelJob_receives_no_exception_when_job_found_and_project_assigned_to_user() {
        /* prepare */
        UUID jobUUID = UUID.randomUUID();
        String userId = "user1";
        String projectId = "project1";

        JobInformation jobInformation = mock(JobInformation.class);
        when(jobInformation.getProjectId()).thenReturn(projectId);

        User user = mock(User.class);
        when(user.getName()).thenReturn(userId);

        when(userRepository.findOrFailUser(userId)).thenReturn(user);
        when(jobInformationRepository.findById(jobUUID)).thenReturn(Optional.of(jobInformation));
        when(projectRepository.findAllProjectIdsWhereUserIsAssigned(userId)).thenReturn(Set.of(projectId));

        /* execute + test */
        assertThatCode(() -> serviceToTest.userCancelJob(jobUUID, userId)).doesNotThrowAnyException();

        verify(eventBusService, times(1)).sendAsynchron(any());
    }

}
