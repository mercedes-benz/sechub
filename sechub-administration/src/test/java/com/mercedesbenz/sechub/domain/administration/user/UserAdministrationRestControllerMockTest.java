// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.administration.user;

import static com.mercedesbenz.sechub.test.SecHubTestURLBuilder.https;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Profile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import com.mercedesbenz.sechub.domain.administration.project.Project;
import com.mercedesbenz.sechub.domain.administration.signup.AnonymousSignupCreateService;
import com.mercedesbenz.sechub.domain.administration.signup.Signup;
import com.mercedesbenz.sechub.domain.administration.signup.SignupRepository;
import com.mercedesbenz.sechub.sharedkernel.Profiles;
import com.mercedesbenz.sechub.sharedkernel.security.AbstractSecHubAPISecurityConfiguration;
import com.mercedesbenz.sechub.sharedkernel.security.RoleConstants;
import com.mercedesbenz.sechub.test.TestPortProvider;

@RunWith(SpringRunner.class)
@WebMvcTest(UserAdministrationRestController.class)
@ContextConfiguration(classes = { UserAdministrationRestController.class, UserAdministrationRestControllerMockTest.SimpleTestConfiguration.class })
@WithMockUser(roles = RoleConstants.ROLE_SUPERADMIN)
@ActiveProfiles({ Profiles.TEST, Profiles.ADMIN_ACCESS })
public class UserAdministrationRestControllerMockTest {

    private static final int PORT_USED = TestPortProvider.DEFAULT_INSTANCE.getWebMVCTestHTTPSPort();

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AnonymousSignupCreateService mockedSignupCreateService;

    @MockBean
    private SignupRepository mockedSelfRegistrationRepo;

    @MockBean
    UserCreationService mockedUserCreationService;

    @MockBean
    UserDeleteService mockedUserDeleteService;

    @MockBean
    UserDetailInformationService mockedUserDetailInformationService;

    @MockBean
    private UserGrantSuperAdminRightsService userGrantSuperAdminRightsService;

    @MockBean
    private UserRevokeSuperAdminRightsService userRevokeSuperAdminRightsService;

    @MockBean
    private UserEmailAddressUpdateService userEmailAddressUpdateService;

    @MockBean
    UserListService mockedUserListService;

    @Test
    public void delete_user_calls_delte_service() throws Exception {
        /* execute + test @formatter:off */
        this.mockMvc.perform(
        		delete(https(PORT_USED).buildAdminDeletesUserUrl("user1"))
        		).
        			andExpect(status().isOk()
        		);

        /* @formatter:on */
        verify(mockedUserDeleteService).deleteUser("user1");
    }

    @Test
    public void show_user_details_returns_result_of_detail_service() throws Exception {
        /* prepare */
        User user = mock(User.class);
        when(user.getName()).thenReturn("user1");
        when(user.getEmailAddress()).thenReturn("user1@example.org");
        Set<Project> projects = new LinkedHashSet<>();

        Project project1 = mock(Project.class);
        when(project1.getId()).thenReturn("project1");
        projects.add(project1);
        when(user.getProjects()).thenReturn(projects);
        UserDetailInformation info = new UserDetailInformation(user);

        when(mockedUserDetailInformationService.fetchDetails("user1")).thenReturn(info);

        /* execute + test @formatter:off */
        this.mockMvc.perform(
        		get(https(PORT_USED).buildAdminShowsUserDetailsUrl("user1"))
        		).
        			andExpect(status().isOk()).
        			andExpect(jsonPath("$.userId", equalTo("user1"))).
        			andExpect(jsonPath("$.email", equalTo("user1@example.org"))).
        			andExpect(jsonPath("$.projects", equalTo(Arrays.asList("project1")))
        		);

        /* @formatter:on */
    }

    @Test
    public void show_user_details_for_email_address_returns_result_of_detail_service() throws Exception {
        /* prepare */
        String emailAddress = "testuser1@example.org";
        String userId = "user1";

        User user = mock(User.class);
        when(user.getName()).thenReturn(userId);
        when(user.getEmailAddress()).thenReturn(emailAddress);
        Set<Project> projects = new LinkedHashSet<>();

        Project project1 = mock(Project.class);
        when(project1.getId()).thenReturn("project1");
        projects.add(project1);
        when(user.getProjects()).thenReturn(projects);
        UserDetailInformation info = new UserDetailInformation(user);

        when(mockedUserDetailInformationService.fetchDetailsByEmailAddress(emailAddress)).thenReturn(info);

        /* execute + test @formatter:off */
        this.mockMvc.perform(
                get(https(PORT_USED).buildAdminShowsUserDetailsForEmailAddressUrl(emailAddress))
                ).
        andExpect(status().isOk()).
        andExpect(jsonPath("$.userId", equalTo(userId))).
        andExpect(jsonPath("$.email", equalTo(emailAddress))).
        andExpect(jsonPath("$.projects", equalTo(Arrays.asList("project1")))
                );

        /* @formatter:on */
    }

    @Test
    public void listUsers_results_in_a_filled_string_list_when_2_users_exist() throws Exception {
        /* prepare */
        List<String> list = new ArrayList<>();

        list.add("name1");
        list.add("name2");

        when(mockedUserListService.listUsers()).thenReturn(list);
        /* execute + test @formatter:off */
        this.mockMvc.perform(
        		get(https(PORT_USED).buildAdminListsUsersUrl())
        		).
        			andExpect(jsonPath("$.[0]", equalTo("name1"))).
        			andExpect(jsonPath("$.[1]", equalTo("name2"))).
        			andExpect(status().isOk()
        		);

        /* @formatter:on */
    }

    @Test
    public void calling_with_api_1_0_and_valid_userid_and_email_returns_HTTP_200() throws Exception {
        Signup selfReg = new Signup();
        selfReg.setUserId("user1");
        Optional<Signup> selfRegistration = Optional.ofNullable(selfReg);
        /* prepare */
        when(mockedSelfRegistrationRepo.findById("user1")).thenReturn(selfRegistration);

        /* execute + test @formatter:off */
        this.mockMvc.perform(
        		post(https(PORT_USED).buildAdminAcceptsUserSignUpUrl("user1"))
        		).
        			andExpect(status().isCreated()
        		);

        /* @formatter:on */
    }

    @TestConfiguration
    @Profile(Profiles.TEST)
    @EnableAutoConfiguration
    public static class SimpleTestConfiguration extends AbstractSecHubAPISecurityConfiguration {

    }

}
