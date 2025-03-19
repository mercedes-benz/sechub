package com.mercedesbenz.sechub.domain.administration.project;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.net.URI;
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
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessageService;
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
    void create_project_success() {
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
        assertThat(storedProject.getUsers()).describedAs("Owner must be inside user set - means assigned automatically").contains(ownerUser);

        verify(eventBus,times(2)).sendAsynchron(any());
    }

    @Test
    void create_project_already_exists() {
        /* prepare */
        when(projectRepository.findById(PROJECT_ID)).thenReturn(Optional.of(new Project()));

        AlreadyExistsException exception = assertThrows(AlreadyExistsException.class, () -> {
            projectCreationService.createProject(PROJECT_ID, DESCRIPTION, OWNER, WHITELIST, META_DATA);
        });

        /* execute + test */
        assertThat(exception.getMessage()).isEqualTo("Project 'testProject' already exists");
    }

    @Test
    void create_project_owner_not_found() {
        /* prepare */
        when(projectRepository.findById(PROJECT_ID)).thenReturn(Optional.empty());
        when(userRepository.findById(OWNER)).thenReturn(Optional.empty());

        /* execute + test */
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            projectCreationService.createProject(PROJECT_ID, DESCRIPTION, OWNER, WHITELIST, META_DATA);
        });

        /* test */
        assertThat(exception.getMessage()).isEqualTo("Owner 'testOwner' not found");
    }
}