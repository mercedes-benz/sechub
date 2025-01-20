package com.mercedesbenz.sechub.domain.administration.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.domain.administration.project.Project;
import com.mercedesbenz.sechub.sharedkernel.logging.LogSanitizer;
import com.mercedesbenz.sechub.sharedkernel.security.UserContextService;
import com.mercedesbenz.sechub.sharedkernel.validation.UserInputAssertion;

class UserDetailInformationServiceTest {

    private static final String USER_ID = UUID.randomUUID().toString();
    private static final String EMAIL_ADDRESS = "test-user@example.org";
    private static final Set<Project> PROJECTS = Set.of(createProject());
    private static final Set<Project> OWNED_PROJECTS = Set.of(createProject());
    private static final User USER = createUser();

    private static final UserContextService userContextService = mock();
    private static final UserRepository userRepository = mock();
    private static final LogSanitizer logSanitizer = mock();
    private static final UserInputAssertion userInputAssertion = mock();
    private static final UserDetailInformationService serviceToTest = new UserDetailInformationService(userContextService, userRepository, logSanitizer,
            userInputAssertion);

    @BeforeEach
    void beforeEach() {
        reset(userContextService, userRepository, logSanitizer, userInputAssertion);
        when(userContextService.getUserId()).thenReturn(USER_ID);
        when(userRepository.findOrFailUser(USER_ID)).thenReturn(USER);
    }

    @Test
    void fetchDetails__returns_user_details_for_the_authenticated_user() {
        /* execute */
        UserDetailInformation userDetailInformation = serviceToTest.fetchDetails();

        /* test */
        assertThat(userDetailInformation).isNotNull();
        assertThat(userDetailInformation.getUserId()).isEqualTo(USER_ID);
        assertThat(userDetailInformation.getEmail()).isEqualTo(EMAIL_ADDRESS);
        assertThat(userDetailInformation.getProjects()).isNotEmpty();
        assertThat(userDetailInformation.getProjects()).isEqualTo(getProjectIds(PROJECTS));
        assertThat(userDetailInformation.getOwnedProjects()).isNotEmpty();
        assertThat(userDetailInformation.getOwnedProjects()).isEqualTo(getProjectIds(OWNED_PROJECTS));
        assertThat(userDetailInformation.isSuperAdmin()).isTrue();
        verify(userContextService).getUserId();
        verify(userRepository).findOrFailUser(USER_ID);
    }

    @Test
    void fetchDetailsById__returns_user_details_for_given_user_id() {
        /* execute */
        UserDetailInformation userDetailInformation = serviceToTest.fetchDetailsById(USER_ID);

        /* test */
        assertThat(userDetailInformation).isNotNull();
        assertThat(userDetailInformation.getUserId()).isEqualTo(USER_ID);
        assertThat(userDetailInformation.getEmail()).isEqualTo(EMAIL_ADDRESS);
        assertThat(userDetailInformation.getProjects()).isNotEmpty();
        assertThat(userDetailInformation.getProjects()).isEqualTo(getProjectIds(PROJECTS));
        assertThat(userDetailInformation.getOwnedProjects()).isNotEmpty();
        assertThat(userDetailInformation.getOwnedProjects()).isEqualTo(getProjectIds(OWNED_PROJECTS));
        assertThat(userDetailInformation.isSuperAdmin()).isTrue();
        verify(logSanitizer).sanitize(USER_ID, 30);
        verify(userContextService).getUserId();
        verify(userInputAssertion).assertIsValidUserId(USER_ID);
        verify(userRepository).findOrFailUser(USER_ID);
    }

    @Test
    void fetchDetailsByEmail__returns_user_details_for_given_email() {
        /* prepare */
        when(userRepository.findOrFailUserByEmailAddress(EMAIL_ADDRESS)).thenReturn(USER);

        /* execute */
        UserDetailInformation userDetailInformation = serviceToTest.fetchDetailsByEmailAddress(EMAIL_ADDRESS);

        /* test */
        assertThat(userDetailInformation).isNotNull();
        assertThat(userDetailInformation.getUserId()).isEqualTo(USER_ID);
        assertThat(userDetailInformation.getEmail()).isEqualTo(EMAIL_ADDRESS);
        assertThat(userDetailInformation.getProjects()).isNotEmpty();
        assertThat(userDetailInformation.getProjects()).isEqualTo(getProjectIds(PROJECTS));
        assertThat(userDetailInformation.getOwnedProjects()).isNotEmpty();
        assertThat(userDetailInformation.getOwnedProjects()).isEqualTo(getProjectIds(OWNED_PROJECTS));
        assertThat(userDetailInformation.isSuperAdmin()).isTrue();
        verify(logSanitizer).sanitize(EMAIL_ADDRESS, 30);
        verify(userContextService).getUserId();
        verify(userInputAssertion).assertIsValidEmailAddress(EMAIL_ADDRESS);
        verify(userRepository).findOrFailUserByEmailAddress(EMAIL_ADDRESS);
    }

    private static Project createProject() {
        Project project = new Project();
        /*
         * working around package private visibility of the id column by using
         * reflections
         */
        setField(project, "id", UUID.randomUUID().toString());
        return project;
    }

    private static User createUser() {
        User user = new User();
        user.name = UserDetailInformationServiceTest.USER_ID;
        user.emailAddress = UserDetailInformationServiceTest.EMAIL_ADDRESS;
        user.projects = UserDetailInformationServiceTest.PROJECTS;
        user.ownedProjects = UserDetailInformationServiceTest.OWNED_PROJECTS;
        user.superAdmin = true;
        return user;
    }

    private static List<String> getProjectIds(Set<Project> projects) {
        return projects.stream().map(Project::getId).toList();
    }

}
