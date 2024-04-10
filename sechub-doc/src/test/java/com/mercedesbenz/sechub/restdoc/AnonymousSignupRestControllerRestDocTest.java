// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.restdoc;

import static com.mercedesbenz.sechub.restdoc.RestDocumentation.*;
import static com.mercedesbenz.sechub.test.SecHubTestURLBuilder.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.lang.annotation.Annotation;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
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

import com.mercedesbenz.sechub.docgen.util.RestDocFactory;
import com.mercedesbenz.sechub.domain.administration.signup.AnonymousSignupCreateService;
import com.mercedesbenz.sechub.domain.administration.signup.AnonymousSignupRestController;
import com.mercedesbenz.sechub.domain.administration.signup.SignupJsonInputValidator;
import com.mercedesbenz.sechub.sharedkernel.Profiles;
import com.mercedesbenz.sechub.sharedkernel.configuration.AbstractSecHubAPISecurityConfiguration;
import com.mercedesbenz.sechub.sharedkernel.usecases.UseCaseRestDoc;
import com.mercedesbenz.sechub.sharedkernel.usecases.user.UseCaseUserSignup;
import com.mercedesbenz.sechub.sharedkernel.validation.ApiVersionValidationFactory;
import com.mercedesbenz.sechub.sharedkernel.validation.EmailValidationImpl;
import com.mercedesbenz.sechub.sharedkernel.validation.UserIdValidationImpl;
import com.mercedesbenz.sechub.test.ExampleConstants;
import com.mercedesbenz.sechub.test.TestIsNecessaryForDocumentation;
import com.mercedesbenz.sechub.test.TestPortProvider;

@RunWith(SpringRunner.class)
@WebMvcTest(AnonymousSignupRestController.class)
@ContextConfiguration(classes = { AnonymousSignupRestController.class, SignupJsonInputValidator.class, UserIdValidationImpl.class,
        ApiVersionValidationFactory.class, EmailValidationImpl.class, AnonymousSignupRestControllerRestDocTest.SimpleTestConfiguration.class })
@WithMockUser
@ActiveProfiles(Profiles.TEST)
@AutoConfigureRestDocs(uriScheme = "https", uriHost = ExampleConstants.URI_SECHUB_SERVER, uriPort = 443)
public class AnonymousSignupRestControllerRestDocTest implements TestIsNecessaryForDocumentation {

    private static final int PORT_USED = TestPortProvider.DEFAULT_INSTANCE.getRestDocTestPort();

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AnonymousSignupCreateService mockedScheduleService;

    @Test
    @UseCaseRestDoc(useCase = UseCaseUserSignup.class)
    public void calling_with_api_1_0_and_valid_userid_and_email_returns_HTTP_200() throws Exception {
        /* prepare */
        String apiEndpoint = https(PORT_USED).buildUserSignUpUrl();
        Class<? extends Annotation> useCase = UseCaseUserSignup.class;

        /* execute */
        /* @formatter:off */
        this.mockMvc.perform(
        		post(apiEndpoint).
        			contentType(MediaType.APPLICATION_JSON_VALUE).
        			content("{\"apiVersion\":\"1.0\",\"userId\":\"valid_userid\",\"emailAdress\":\"valid_mailadress@test.com\"}")
        		)./*andDo(print()).*/
        			andExpect(status().isOk()).
        			andDo(defineRestService().
        			        with().
        			            identifier(RestDocFactory.createPath(useCase)).
        			            summary(RestDocFactory.createSummary(useCase)).
        			            description(RestDocFactory.createDescription(useCase)).
        			            tag(RestDocFactory.extractTag(apiEndpoint)).
        			            requestSchema(OpenApiSchema.USER_SIGNUP.getSchema()).
        			        and().
        			        document(
        	                    requestFields(
        	                            fieldWithPath("apiVersion").description("The api version, currently only 1.0 is supported"),
        	                            fieldWithPath("userId").description("Wanted userid, the userid must be lowercase only!"),
        	                            fieldWithPath("emailAdress").description("Email adress")
        	                    )
        			        )

        			);

        /* @formatter:on */
    }

    @TestConfiguration
    @Profile(Profiles.TEST)
    @EnableAutoConfiguration
    public static class SimpleTestConfiguration extends AbstractSecHubAPISecurityConfiguration {

    }

}
