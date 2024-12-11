// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.restdoc;

import static com.mercedesbenz.sechub.restdoc.RestDocumentation.*;
import static com.mercedesbenz.sechub.test.RestDocPathParameter.*;
import static com.mercedesbenz.sechub.test.SecHubTestURLBuilder.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.lang.annotation.Annotation;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import com.mercedesbenz.sechub.docgen.util.RestDocFactory;
import com.mercedesbenz.sechub.domain.administration.user.AnonymousUserRequestNewApiTokenRestController;
import com.mercedesbenz.sechub.domain.administration.user.AnonymousUserRequestsNewApiTokenService;
import com.mercedesbenz.sechub.sharedkernel.Profiles;
import com.mercedesbenz.sechub.sharedkernel.usecases.UseCaseRestDoc;
import com.mercedesbenz.sechub.sharedkernel.usecases.user.UseCaseUserRequestsNewApiToken;
import com.mercedesbenz.sechub.test.ExampleConstants;
import com.mercedesbenz.sechub.test.TestIsNecessaryForDocumentation;
import com.mercedesbenz.sechub.test.TestPortProvider;

@RunWith(SpringRunner.class)
@WebMvcTest
@ContextConfiguration(classes = { AnonymousUserRequestNewApiTokenRestController.class })
@Import(TestRestDocSecurityConfiguration.class)
@WithMockUser
@ActiveProfiles(Profiles.TEST)
@AutoConfigureRestDocs(uriScheme = "https", uriHost = ExampleConstants.URI_SECHUB_SERVER, uriPort = 443)
public class AnonymousUserRequestsNewApiTokenRestDocTest implements TestIsNecessaryForDocumentation {

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
        		post(apiEndpoint,"emailAddress@example.com").
        		contentType(MediaType.APPLICATION_JSON_VALUE)
        		).
        			andExpect(status().isOk()).
        			andDo(defineRestService().
        	                with().
        	                    useCaseData(useCase).
        	                    tag(RestDocFactory.extractTag(apiEndpoint)).
        	                and().
        	                document(
                                        pathParameters(
                                                parameterWithName(EMAIL_ADDRESS.paramName()).description("Email address for user where api token shall be refreshed.")
        	                         )
        			        ));

        /* @formatter:on */
    }

}
