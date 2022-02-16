// SPDX-License-Identifier: MIT
package com.daimler.sechub.restdoc;

import static com.daimler.sechub.test.TestURLBuilder.RestDocPathParameter.EMAIL_ADDRESS;
import static com.daimler.sechub.test.TestURLBuilder.https;
import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

import com.daimler.sechub.docgen.util.RestDocFactory;
import com.daimler.sechub.domain.administration.user.AnonymousUserRequestNewApiTokenRestController;
import com.daimler.sechub.domain.administration.user.AnonymousUserRequestsNewApiTokenService;
import com.daimler.sechub.sharedkernel.Profiles;
import com.daimler.sechub.sharedkernel.configuration.AbstractAllowSecHubAPISecurityConfiguration;
import com.daimler.sechub.sharedkernel.usecases.UseCaseRestDoc;
import com.daimler.sechub.sharedkernel.usecases.user.UseCaseUserRequestsNewApiToken;
import com.daimler.sechub.test.ExampleConstants;
import com.daimler.sechub.test.TestPortProvider;
import com.epages.restdocs.apispec.ResourceSnippetParameters;

@RunWith(SpringRunner.class)
@WebMvcTest(AnonymousUserRequestNewApiTokenRestController.class)
@ContextConfiguration(classes = { AnonymousUserRequestNewApiTokenRestController.class,
        AnonymousUserRequestsNewApiTokenRestDocTest.SimpleTestConfiguration.class })
@WithMockUser
@ActiveProfiles(Profiles.TEST)
@AutoConfigureRestDocs(uriScheme = "https", uriHost = ExampleConstants.URI_SECHUB_SERVER, uriPort = 443)
public class AnonymousUserRequestsNewApiTokenRestDocTest {

    private static final int PORT_USED = TestPortProvider.DEFAULT_INSTANCE.getRestDocTestPort();

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AnonymousUserRequestsNewApiTokenService newApiTokenService;

    @Test
    @UseCaseRestDoc(useCase = UseCaseUserRequestsNewApiToken.class)
    public void calling_with_api_1_0_and_valid_userid_and_email_returns_HTTP_200() throws Exception {
        /* prepare */
        String apiEndpoint = https(PORT_USED).buildAnonymousRequestNewApiToken(EMAIL_ADDRESS.pathElement());
        Class<? extends Annotation> useCase = UseCaseUserRequestsNewApiToken.class;

        /* execute */
        /* @formatter:off */
        this.mockMvc.perform(
        		post(apiEndpoint,"emailAdress@test.com").
        		contentType(MediaType.APPLICATION_JSON_VALUE)
        		).
        			andExpect(status().isOk()).
        			andDo(document(RestDocFactory.createPath(useCase),
        	                resource(
        	                        ResourceSnippetParameters.builder().
        	                            summary(RestDocFactory.createSummary(useCase)).
        	                            description(RestDocFactory.createDescription(useCase)).
        	                            tag(RestDocFactory.extractTag(apiEndpoint)).
                                        pathParameters(
                                                parameterWithName(EMAIL_ADDRESS.paramName()).description("Email address for user where api token shall be refreshed.")
                                        ).
        	                            build()
        	                         )
        			        ));

        /* @formatter:on */
    }

    @TestConfiguration
    @Profile(Profiles.TEST)
    @EnableAutoConfiguration
    public static class SimpleTestConfiguration extends AbstractAllowSecHubAPISecurityConfiguration {

    }

}
