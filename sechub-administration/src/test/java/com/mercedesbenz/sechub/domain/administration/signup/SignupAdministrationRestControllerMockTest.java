// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.administration.signup;

import static com.mercedesbenz.sechub.test.SecHubTestURLBuilder.*;
import static org.hamcrest.CoreMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.ArrayList;
import java.util.List;

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

import com.mercedesbenz.sechub.sharedkernel.Profiles;
import com.mercedesbenz.sechub.sharedkernel.RoleConstants;
import com.mercedesbenz.sechub.sharedkernel.configuration.AbstractSecHubAPISecurityConfiguration;
import com.mercedesbenz.sechub.test.TestPortProvider;

@RunWith(SpringRunner.class)
@WebMvcTest(SignupAdministrationRestController.class)
@ContextConfiguration(classes = { SignupAdministrationRestController.class, SignupAdministrationRestControllerMockTest.SimpleTestConfiguration.class })
@WithMockUser(roles = RoleConstants.ROLE_SUPERADMIN)
@ActiveProfiles({ Profiles.TEST, Profiles.ADMIN_ACCESS })
public class SignupAdministrationRestControllerMockTest {

    private static final int PORT_USED = TestPortProvider.DEFAULT_INSTANCE.getWebMVCTestHTTPSPort();

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SignupDeleteService mockedSignupDeleteService;

    @MockBean
    private SignupRepository mockedSelfRegistrationRepo;

    @Test
    public void listUserSignups_results_in_empty_text_when_no_signups_exist() throws Exception {
        /* prepare */
        List<Signup> list = new ArrayList<>();

        when(mockedSelfRegistrationRepo.findAll()).thenReturn(list);
        /* execute + test @formatter:off */
        this.mockMvc.perform(
        		get(https(PORT_USED).buildAdminListsUserSignupsUrl())
        		).
        			andExpect(status().isOk()).
        			andExpect(content().json("[]")
        		);

        /* @formatter:on */
    }

    @Test
    public void listUserSignups_results_in_a_filled_list_when_2_signups_exist() throws Exception {
        /* prepare */
        List<Signup> list = new ArrayList<>();
        Signup signup1 = new Signup();
        signup1.setEmailAdress("sechub.test1@example.org");
        signup1.setUserId("sechub.test1");

        Signup signup2 = new Signup();
        signup2.setEmailAdress("sechub.test2@example.org");
        signup2.setUserId("sechub.test2");

        list.add(signup1);
        list.add(signup2);

        when(mockedSelfRegistrationRepo.findAll()).thenReturn(list);
        /* execute + test @formatter:off */
        this.mockMvc.perform(
        		get(https(PORT_USED).buildAdminListsUserSignupsUrl())
        		).
        			andExpect(status().isOk()).
        			andExpect(jsonPath("$.[0].userId", equalTo("sechub.test1"))).
        			andExpect(jsonPath("$.[0].emailAdress", equalTo("sechub.test1@example.org"))).

        			andExpect(jsonPath("$.[1].userId", equalTo("sechub.test2"))).
        			andExpect(jsonPath("$.[1].emailAdress", equalTo("sechub.test2@example.org"))

        		);

        /* @formatter:on */
    }

    @TestConfiguration
    @Profile(Profiles.TEST)
    @EnableAutoConfiguration
    public static class SimpleTestConfiguration extends AbstractSecHubAPISecurityConfiguration {

    }

}