// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.administration.job;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.Test;

import com.mercedesbenz.sechub.domain.administration.project.Project;
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
    private static final JobCancelService serviceToTest = new JobCancelService(auditLogService, userInputAssertion, eventBusService, jobInformationRepository,
            userRepository);

    @Test
    public void userCancelJob_receives_not_found_exception_when_job_not_found() {
        /* prepare */
        UUID jobUUID = UUID.randomUUID();
        String userId = "user1";
        when(jobInformationRepository.findById(jobUUID)).thenReturn(Optional.empty());

        /* execute + test */
        assertThatThrownBy(() -> serviceToTest.userCancelJob(jobUUID, userId)).isInstanceOf(NotFoundException.class);
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
        Project project = mock(Project.class);
        when(project.getId()).thenReturn(otherProjectId);

        User user = mock(User.class);
        when(user.getProjects()).thenReturn(Set.of(project));

        when(userRepository.findOrFailUser(userId)).thenReturn(user);
        when(jobInformationRepository.findById(jobUUID)).thenReturn(Optional.of(jobInformation));

        /* execute + test */
        assertThatThrownBy(() -> serviceToTest.userCancelJob(jobUUID, userId)).isInstanceOf(NotFoundException.class);
    }

    @Test
    public void userCancelJob_receives_no_exception_when_job_found_and_authorized() {
        /* prepare */
        UUID jobUUID = UUID.randomUUID();
        String userId = "user1";
        String projectId = "project1";

        JobInformation jobInformation = mock(JobInformation.class);
        when(jobInformation.getProjectId()).thenReturn(projectId);
        Project project = mock(Project.class);
        when(project.getId()).thenReturn(projectId);

        User user = mock(User.class);
        when(user.getProjects()).thenReturn(Set.of(project));

        when(userRepository.findOrFailUser(userId)).thenReturn(user);
        when(jobInformationRepository.findById(jobUUID)).thenReturn(Optional.of(jobInformation));

        /* execute + test */
        assertThatCode(() -> serviceToTest.userCancelJob(jobUUID, userId)).doesNotThrowAnyException();
        verify(eventBusService, times(1)).sendAsynchron(any());
    }

}
