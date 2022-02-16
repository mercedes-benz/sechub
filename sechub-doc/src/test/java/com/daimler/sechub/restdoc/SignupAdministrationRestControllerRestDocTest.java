// SPDX-License-Identifier: MIT
package com.daimler.sechub.restdoc;

import static com.daimler.sechub.test.TestURLBuilder.*;
import static com.daimler.sechub.test.TestURLBuilder.RestDocPathParameter.*;
import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.*;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.lang.annotation.Annotation;
import java.util.Collections;

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

import com.daimler.sechub.docgen.util.RestDocFactory;
import com.daimler.sechub.domain.administration.signup.SignupAdministrationRestController;
import com.daimler.sechub.domain.administration.signup.SignupDeleteService;
import com.daimler.sechub.domain.administration.signup.SignupRepository;
import com.daimler.sechub.sharedkernel.Profiles;
import com.daimler.sechub.sharedkernel.RoleConstants;
import com.daimler.sechub.sharedkernel.configuration.AbstractAllowSecHubAPISecurityConfiguration;
import com.daimler.sechub.sharedkernel.usecases.UseCaseRestDoc;
import com.daimler.sechub.sharedkernel.usecases.admin.signup.UseCaseAdminDeletesSignup;
import com.daimler.sechub.sharedkernel.usecases.admin.signup.UseCaseAdminListsOpenUserSignups;
import com.daimler.sechub.test.ExampleConstants;
import com.daimler.sechub.test.TestPortProvider;
import com.epages.restdocs.apispec.ResourceSnippetParameters;

@RunWith(SpringRunner.class)
@WebMvcTest(SignupAdministrationRestController.class)
@ContextConfiguration(classes = { SignupAdministrationRestController.class, SignupAdministrationRestControllerRestDocTest.SimpleTestConfiguration.class })
@WithMockUser(authorities = RoleConstants.ROLE_SUPERADMIN)
@ActiveProfiles({ Profiles.TEST, Profiles.ADMIN_ACCESS })
@AutoConfigureRestDocs(uriScheme = "https", uriHost = ExampleConstants.URI_SECHUB_SERVER, uriPort = 443)
public class SignupAdministrationRestControllerRestDocTest {

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

        when(signupRepository.findAll()).thenReturn(Collections.emptyList());

        /* execute + test @formatter:off */
        this.mockMvc.perform(
        		get(apiEndpoint)
        		).
        			andExpect(status().isOk()).
        			andDo(document(RestDocFactory.createPath(useCase),
        	                resource(
        	                        ResourceSnippetParameters.builder().
        	                            summary(RestDocFactory.createSummary(useCase)).
        	                            description(RestDocFactory.createDescription(useCase)).
        	                            tag(RestDocFactory.extractTag(apiEndpoint)).
        	                            responseSchema(OpenApiSchema.SIGNUP_LIST.getSchema()).
        	                            responseFields(
        	                                    fieldWithPath("[]").description("List of user signups").optional(),
        	                                    fieldWithPath("[]."+RestDocPathParameter.USER_ID.paramName()).type(JsonFieldType.STRING).description("The user id"),
        	                                    fieldWithPath("[]."+RestDocPathParameter.EMAIL_ADDRESS.paramName()).type(JsonFieldType.STRING).description("The email address")
        	                            ).
        	                            build()
        	                        )
        		));

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
        		delete(apiEndpoint,"userId1")
        		).
        			andExpect(status().isOk()).
        			andDo(document(RestDocFactory.createPath(useCase),
                            resource(
                                    ResourceSnippetParameters.builder().
                                        summary(RestDocFactory.createSummary(useCase)).
                                        description(RestDocFactory.createDescription(useCase)).
                                        tag(RestDocFactory.extractTag(apiEndpoint)).
                                        pathParameters(
                                                parameterWithName(USER_ID.paramName()).description("The userId of the signup which shall be deleted")
                                        ).
                                        build()
                                    )
        			        ));

		/* @formatter:on */
    }

    @Profile(Profiles.TEST)
    @EnableAutoConfiguration
    public static class SimpleTestConfiguration extends AbstractAllowSecHubAPISecurityConfiguration {

    }

}
