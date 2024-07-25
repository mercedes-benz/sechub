// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.restdoc;

import static com.mercedesbenz.sechub.restdoc.RestDocumentation.*;
import static com.mercedesbenz.sechub.test.RestDocPathParameter.*;
import static com.mercedesbenz.sechub.test.SecHubTestURLBuilder.*;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Profile;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import com.mercedesbenz.sechub.docgen.util.RestDocFactory;
import com.mercedesbenz.sechub.domain.administration.signup.Signup;
import com.mercedesbenz.sechub.domain.administration.signup.SignupAdministrationRestController;
import com.mercedesbenz.sechub.domain.administration.signup.SignupDeleteService;
import com.mercedesbenz.sechub.domain.administration.signup.SignupRepository;
import com.mercedesbenz.sechub.sharedkernel.Profiles;
import com.mercedesbenz.sechub.sharedkernel.RoleConstants;
import com.mercedesbenz.sechub.sharedkernel.configuration.AbstractSecHubAPISecurityConfiguration;
import com.mercedesbenz.sechub.sharedkernel.usecases.UseCaseRestDoc;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.signup.UseCaseAdminDeletesSignup;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.signup.UseCaseAdminListsOpenUserSignups;
import com.mercedesbenz.sechub.test.ExampleConstants;
import com.mercedesbenz.sechub.test.RestDocPathParameter;
import com.mercedesbenz.sechub.test.TestIsNecessaryForDocumentation;
import com.mercedesbenz.sechub.test.TestPortProvider;

@RunWith(SpringRunner.class)
@WebMvcTest(SignupAdministrationRestController.class)
@ContextConfiguration(classes = { SignupAdministrationRestController.class, SignupAdministrationRestControllerRestDocTest.SimpleTestConfiguration.class })
@WithMockUser(roles = RoleConstants.ROLE_SUPERADMIN)
@ActiveProfiles({ Profiles.TEST, Profiles.ADMIN_ACCESS })
@AutoConfigureRestDocs(uriScheme = "https", uriHost = ExampleConstants.URI_SECHUB_SERVER, uriPort = 443)
public class SignupAdministrationRestControllerRestDocTest implements TestIsNecessaryForDocumentation {

    private static final int PORT_USED = TestPortProvider.DEFAULT_INSTANCE.getRestDocTestPort();

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SignupDeleteService deleteService;

    @MockBean
    private SignupRepository signupRepository;

    @Before
    public void before() {
    }

    @Test
    @UseCaseRestDoc(useCase = UseCaseAdminListsOpenUserSignups.class)
    public void restdoc_list_user_signups() throws Exception {
        /* prepare */
        String apiEndpoint = https(PORT_USED).buildAdminListsUserSignupsUrl();
        Class<? extends Annotation> useCase = UseCaseAdminListsOpenUserSignups.class;

        Signup signup1 = new Signup();
        signup1.setEmailAddress("john.smith@example.com");
        signup1.setUserId("johnsmith");

        Signup signup2 = new Signup();
        signup2.setEmailAddress("jane.smith@example.com");
        signup2.setUserId("janesmith");

        List<Signup> signupList = new ArrayList<>();
        signupList.add(signup1);
        signupList.add(signup2);

        when(signupRepository.findAll()).thenReturn(signupList);

        /* execute + test @formatter:off */
        this.mockMvc.perform(
        		get(apiEndpoint).
        			header(AuthenticationHelper.HEADER_NAME, AuthenticationHelper.getHeaderValue())
        		).
        			andExpect(status().isOk()).
        			andExpect(content().json("[{\"userId\":\"johnsmith\",\"emailAddress\":\"john.smith@example.com\"},{\"userId\":\"janesmith\",\"emailAddress\":\"jane.smith@example.com\"}]")).
        			andDo(defineRestService().
        			        with().
        			            useCaseData(useCase).
                                tag(RestDocFactory.extractTag(apiEndpoint)).
                                responseSchema(OpenApiSchema.SIGNUP_LIST.getSchema()).
                            and().
            			    document(
        	                		requestHeaders(

        	                		),
	        	                    responseFields(
	        	                            fieldWithPath("[]").description("List of user signups").optional(),
	        	                            fieldWithPath("[]."+RestDocPathParameter.USER_ID.paramName()).type(JsonFieldType.STRING).description("The user id"),
	        	                            fieldWithPath("[].emailAddress").type(JsonFieldType.STRING).description("The email address")
	        	                    )
        	            )
        		);

		/* @formatter:on */
    }

    @Test
    @UseCaseRestDoc(useCase = UseCaseAdminDeletesSignup.class)
    public void restdoc_delete_signup() throws Exception {
        /* prepare */
        String apiEndpoint = https(PORT_USED).buildAdminDeletesUserSignUpUrl(USER_ID.pathElement());
        Class<? extends Annotation> useCase = UseCaseAdminDeletesSignup.class;

        /* execute + test @formatter:off */
        this.mockMvc.perform(
        		delete(apiEndpoint,"userId1").
        			header(AuthenticationHelper.HEADER_NAME, AuthenticationHelper.getHeaderValue())
        		).
        			andExpect(status().isOk()).
        			andDo(defineRestService().
                            with().
                                useCaseData(useCase).
                                tag(RestDocFactory.extractTag(apiEndpoint)).
                            and().
            			    document(
        	                	requestHeaders(

        	                	),
                                pathParameters(
                                        parameterWithName(USER_ID.paramName()).description("The userId of the signup which shall be deleted")
                                )
                            )
        			);

		/* @formatter:on */
    }

    @Profile(Profiles.TEST)
    @EnableAutoConfiguration
    public static class SimpleTestConfiguration extends AbstractSecHubAPISecurityConfiguration {

    }

}
