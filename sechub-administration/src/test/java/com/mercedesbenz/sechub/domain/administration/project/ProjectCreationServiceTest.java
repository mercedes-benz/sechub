// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.administration.project;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.mercedesbenz.sechub.domain.administration.user.User;
import com.mercedesbenz.sechub.domain.administration.user.UserRepository;
import com.mercedesbenz.sechub.sharedkernel.error.AlreadyExistsException;
import com.mercedesbenz.sechub.sharedkernel.error.NotFoundException;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessageService;
import com.mercedesbenz.sechub.sharedkernel.messaging.MessageDataKeys;
import com.mercedesbenz.sechub.sharedkernel.messaging.MessageID;
import com.mercedesbenz.sechub.sharedkernel.security.UserContextService;
import com.mercedesbenz.sechub.sharedkernel.validation.URIValidation;
import com.mercedesbenz.sechub.sharedkernel.validation.UserInputAssertion;
import com.mercedesbenz.sechub.sharedkernel.validation.ValidationResult;

@ExtendWith(MockitoExtension.class)
public class ProjectCreationServiceTest {

    @Mock
    UserContextService userContext;

    @Mock
    ProjectRepository projectRepository;

    @Mock
    DomainMessageService eventBus;

    @Mock
    ProjectTransactionService persistenceService;

    @Mock
    UserRepository userRepository;

    @Mock
    URIValidation uriValidation;

    @Mock
    UserInputAssertion assertion;

    @InjectMocks
    ProjectCreationService projectCreationService;

    private static final String PROJECT_ID = "testProject";
    private static final String DESCRIPTION = "Test project description";
    private static final String OWNER = "testOwner";
    private static final URI WHITE_LISTED_URI = URI.create("http://example.com");
    private static final Set<URI> WHITELIST = Set.of(WHITE_LISTED_URI);
    private static final ProjectJsonInput.ProjectMetaData META_DATA = new ProjectJsonInput.ProjectMetaData();

    @BeforeEach
    void beforeEach() {
        when(userContext.getUserId()).thenReturn("admin");
    }

    @Test
    void create_project_success_stores_project_and_owner_is_in_user_list() {
        /* prepare */
        when(projectRepository.findById(PROJECT_ID)).thenReturn(Optional.empty());
        User ownerUser = mock(User.class, "project owner user object");
        when(ownerUser.getName()).thenReturn(OWNER);
        when(userRepository.findById(OWNER)).thenReturn(Optional.of(ownerUser));

        var okResult = mock(ValidationResult.class);
        when(okResult.isValid()).thenReturn(true);
        when(uriValidation.validate(WHITE_LISTED_URI)).thenReturn(okResult);

        /* execute + test */
        assertThatCode(() -> projectCreationService.createProject(PROJECT_ID, DESCRIPTION, OWNER, WHITELIST, META_DATA)).doesNotThrowAnyException();

        /* test */
        var projectCaptor = ArgumentCaptor.forClass(Project.class);
        verify(persistenceService).saveInOwnTransaction(projectCaptor.capture());
        Project storedProject = projectCaptor.getValue();
        assertThat(storedProject).isNotNull();
    }

    @Test
    void create_project_success_sends_events() {
        /* prepare */
        when(projectRepository.findById(PROJECT_ID)).thenReturn(Optional.empty());
        User ownerUser = mock(User.class, "project owner user object");
        when(ownerUser.getName()).thenReturn(OWNER);
        when(userRepository.findById(OWNER)).thenReturn(Optional.of(ownerUser));

        var okResult = mock(ValidationResult.class);
        when(okResult.isValid()).thenReturn(true);
        when(uriValidation.validate(WHITE_LISTED_URI)).thenReturn(okResult);

        /* execute + test */
        assertThatCode(() -> projectCreationService.createProject(PROJECT_ID, DESCRIPTION, OWNER, WHITELIST, META_DATA)).doesNotThrowAnyException();

        /* test */
        var messageCaptor = ArgumentCaptor.forClass(DomainMessage.class);
        verify(eventBus,times(2)).sendAsynchron(messageCaptor.capture());

        List<DomainMessage> messages = messageCaptor.getAllValues();
        DomainMessage firstMessage = messages.get(0);

        assertThat(firstMessage.getMessageId()).isEqualTo(MessageID.PROJECT_CREATED);
        assertThat(firstMessage.get(MessageDataKeys.PROJECT_CREATION_DATA).getProjectId()).isEqualTo(PROJECT_ID);
        assertThat(firstMessage.get(MessageDataKeys.PROJECT_CREATION_DATA).getProjectOwnerUserId()).isEqualTo(OWNER);

        DomainMessage secondMessage = messages.get(1);
        assertThat(secondMessage.getMessageId()).isEqualTo(MessageID.ASSIGN_OWNER_AS_USER_TO_PROJECT);
        assertThat(secondMessage.get(MessageDataKeys.PROJECT_TO_USER_DATA).getUserId()).isEqualTo(OWNER);
        assertThat(secondMessage.get(MessageDataKeys.PROJECT_TO_USER_DATA).getProjectId()).isEqualTo(PROJECT_ID);
    }

    @Test
    void create_project_already_exists() {
        /* prepare */
        when(projectRepository.findById(PROJECT_ID)).thenReturn(Optional.of(new Project()));

        /* execute + test */
        assertThatThrownBy(() -> projectCreationService.createProject(PROJECT_ID, DESCRIPTION, OWNER, WHITELIST, META_DATA))
            .isInstanceOf(AlreadyExistsException.class)
            .hasMessageContaining("already exists");
    }

    @Test
    void create_project_owner_not_found() {
        /* prepare */
        when(projectRepository.findById(PROJECT_ID)).thenReturn(Optional.empty());
        when(userRepository.findById(OWNER)).thenReturn(Optional.empty());

        /* execute + test */
        assertThatThrownBy(() -> projectCreationService.createProject(PROJECT_ID, DESCRIPTION, OWNER, WHITELIST, META_DATA))
            .isInstanceOf(NotFoundException.class)
            .hasMessageContaining("not found");
    }
}