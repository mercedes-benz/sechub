// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.administration.user;

import static com.mercedesbenz.sechub.test.RestDocPathParameter.EMAIL_ADDRESS;
import static com.mercedesbenz.sechub.test.SecHubTestURLBuilder.https;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Set;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import com.mercedesbenz.sechub.domain.administration.project.Project;
import com.mercedesbenz.sechub.sharedkernel.Profiles;
import com.mercedesbenz.sechub.sharedkernel.security.RoleConstants;
import com.mercedesbenz.sechub.test.TestPortProvider;

@RunWith(SpringRunner.class)
@WebMvcTest
@ContextConfiguration(classes = { UserRestController.class })
@ActiveProfiles({ Profiles.TEST })
public class UserRestControllerTest {

    private static final int PORT_USED = TestPortProvider.DEFAULT_INSTANCE.getWebMVCTestHTTPSPort();

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserDetailInformationService userDetailInformationService;

    @MockBean
    private UserEmailAddressUpdateService emailAddressUpdateService;

    @Test
    @WithMockUser(roles = RoleConstants.ROLE_USER)
    public void fetchUserDetailInformation__is_accessible_by_authenticated_user() throws Exception {
        /* prepare */
        User user = createUser();
        String userId = user.getName();
        UserDetailInformation userDetailInformation = new UserDetailInformation(user);
        when(userDetailInformationService.fetchDetails()).thenReturn(userDetailInformation);

        /* execute + test */
        /* @formatter:off */
        this.mockMvc.perform(
                get(https(PORT_USED).buildFetchUserDetailInformationUrl()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId", equalTo(userId)))
                .andExpect(jsonPath("$.email", equalTo(userDetailInformation.getEmail())))
                .andExpect(jsonPath("$.projects", equalTo(userDetailInformation.getProjects())))
                .andExpect(jsonPath("$.ownedProjects", equalTo(userDetailInformation.getOwnedProjects())))
                .andExpect(jsonPath("$.superAdmin", equalTo(userDetailInformation.isSuperAdmin())));
        /* @formatter:on */
        verify(userDetailInformationService).fetchDetails();
    }

    @Test
    public void fetchUserDetailInformation__is_not_accessible_by_unauthenticated_user() throws Exception {
        /* execute + test */
        /* @formatter:off */
        this.mockMvc.perform(
                get(https(PORT_USED).buildFetchUserDetailInformationUrl()))
                .andExpect(status().isUnauthorized());
        /* @formatter:on */
    }

    @Test
    @WithMockUser(roles = RoleConstants.ROLE_USER)
    public void updateUserEmailAddress__is_accessible_by_authenticated_user() throws Exception {
        /* prepare */
        String newEmailAddress = "test-user.new@example.org";
        String apiEndpoint = https(PORT_USED).buildUserRequestUpdatesEmailUrl(EMAIL_ADDRESS.pathElement());
        doNothing().when(emailAddressUpdateService).userRequestUpdateMailAddress(newEmailAddress);

        /* execute + test */
        /* @formatter:off */
        this.mockMvc.perform(
                        post(apiEndpoint, newEmailAddress)
                                .with(csrf()))
                .andExpect(status().isOk());
        /* @formatter:on */

        verify(emailAddressUpdateService).userRequestUpdateMailAddress(newEmailAddress);
    }

    @Test
    public void updateUserEmailAddress__is_not_accessible_by_unauthenticated_user() throws Exception {
        /* prepare */
        String newEmailAddress = "testuser.new@example.org";
        String apiEndpoint = https(PORT_USED).buildUserRequestUpdatesEmailUrl(EMAIL_ADDRESS.pathElement());

        /* execute + test */
        /* @formatter:off */
        this.mockMvc.perform(
                        post(apiEndpoint, newEmailAddress)
                                .with(csrf()))
                .andExpect(status().isUnauthorized());
        /* @formatter:on */
    }

    private static User createUser() {
        User user = new User();
        user.name = UUID.randomUUID().toString();
        user.emailAddress = "test-user@example.org";
        user.projects = Set.of(new Project());
        user.ownedProjects = Set.of(new Project());
        user.superAdmin = true;
        return user;
    }
}
