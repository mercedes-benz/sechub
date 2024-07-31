// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.administration.signup;

import static com.mercedesbenz.sechub.test.SecHubTestURLBuilder.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import com.mercedesbenz.sechub.sharedkernel.Profiles;
import com.mercedesbenz.sechub.sharedkernel.configuration.AbstractSecHubAPISecurityConfiguration;
import com.mercedesbenz.sechub.sharedkernel.validation.ApiVersionValidationFactory;
import com.mercedesbenz.sechub.sharedkernel.validation.EmailValidationImpl;
import com.mercedesbenz.sechub.sharedkernel.validation.UserIdValidationImpl;
import com.mercedesbenz.sechub.sharedkernel.validation.UserInputAssertion;
import com.mercedesbenz.sechub.test.TestPortProvider;

@RunWith(SpringRunner.class)
@WebMvcTest(AnonymousSignupRestController.class)
/* @formatter:off */
@ContextConfiguration(classes = {
        AnonymousSignupRestController.class,
        SignupJsonInputValidator.class,
        UserIdValidationImpl.class,
        EmailValidationImpl.class,
		ApiVersionValidationFactory.class,
		AnonymousSignupRestControllerMockTest.SimpleTestConfiguration.class })
/* @formatter:on */
@WithMockUser
@ActiveProfiles(Profiles.TEST)
public class AnonymousSignupRestControllerMockTest {

    private static final int PORT_USED = TestPortProvider.DEFAULT_INSTANCE.getWebMVCTestHTTPSPort();

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AnonymousSignupCreateService mockedSignupCreateService;

    @MockBean
    private UserInputAssertion assertion;

    @Test
    public void calling_with_api_1_0_and_valid_userid_and_email_calls_signup_create_service_and_returns_HTTP_200() throws Exception {
        /* prepare */

        /* execute */
        /* @formatter:off */
        this.mockMvc.perform(
        		post(https(PORT_USED).buildUserSignUpUrl()).
        			contentType(MediaType.APPLICATION_JSON_VALUE).
        			content("{\"apiVersion\":\"1.0\",\"userId\":\"valid_userid\",\"emailAddress\":\"valid_emailaddress@example.org\"}")
        		).
        			andExpect(status().isOk()
        		);

        /* @formatter:on */
        verify(mockedSignupCreateService).register(any());
    }

    @Test
    public void calling_with_api_X_0_and_valid_userid_and_email_returns_HTTP_400_BAD_REQUEST() throws Exception {
        /* prepare */

        /* execute + test @formatter:off */
        this.mockMvc.perform(
        		post(https(PORT_USED).buildUserSignUpUrl()).
        			contentType(MediaType.APPLICATION_JSON_VALUE).
        			content("{\"apiVersion\":\"X.0\",\"userId\":\"\",\"emailAddress\":\"test@example.org\"}")
        		).
        			andExpect(status().isBadRequest()
        		);

        /* @formatter:on */
    }

    @Test
    public void calling_empty_returns_HTTP_400_BAD_REQUEST() throws Exception {
        /* prepare */

        /* execute + test @formatter:off */
        this.mockMvc.perform(
        		post(https(PORT_USED).buildUserSignUpUrl()).
        			contentType(MediaType.APPLICATION_JSON_VALUE).
        			content("")
        		).
        			andExpect(status().isBadRequest()
        		);
        /* @formatter:on */
    }

    @Test
    public void calling_invalid_json_returns_HTTP_400_BAD_REQUEST() throws Exception {
        /* prepare */

        /* execute + test @formatter:off */
        this.mockMvc.perform(
        		post(https(PORT_USED).buildUserSignUpUrl()).
        			contentType(MediaType.APPLICATION_JSON_VALUE).
        			content("{")
        		).
        			andExpect(status().isBadRequest()
        		);
        /* @formatter:on */
    }

    @Test
    public void calling_with_api_1_0_and_userid_not_set_but_valid_email_returns_HTTP_400_BAD_REQUEST() throws Exception {
        /* prepare */

        /* execute + test @formatter:off */
        this.mockMvc.perform(
        		post(https(PORT_USED).buildUserSignUpUrl()).
        			contentType(MediaType.APPLICATION_JSON_VALUE).
        			content(createUserSelfRegistration("X.0", "test@example.org", null).toJSON())
        		).
        			andExpect(status().isBadRequest()
        		);

        /* @formatter:on */
    }

    @Test
    public void calling_with_api_1_0_and_userid_set_but_NO_valid_email_returns_HTTP_400_BAD_REQUEST() throws Exception {
        /* prepare */

        /* execute + test @formatter:off */
        this.mockMvc.perform(
        		post(https(PORT_USED).buildUserSignUpUrl()).
        			contentType(MediaType.APPLICATION_JSON_VALUE).
        			content("{\"apiVersion\":\"1.0\",\"userId\":\"the tester\"}")
        		).
        			andExpect(status().isBadRequest()
        		);

        /* @formatter:on */
    }

    @TestConfiguration
    @Profile(Profiles.TEST)
    @EnableAutoConfiguration
    public static class SimpleTestConfiguration extends AbstractSecHubAPISecurityConfiguration {

    }

    private SignupJsonInput createUserSelfRegistration(String api, String email, String name) {

        SignupJsonInput created = new SignupJsonInput();
        created.setApiVersion(api);
        created.setEmailAddress(email);
        created.setUserId(name);
        return created;
    }
}
