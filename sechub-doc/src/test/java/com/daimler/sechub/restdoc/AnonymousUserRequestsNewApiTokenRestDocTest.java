// SPDX-License-Identifier: MIT
package com.daimler.sechub.restdoc;

import static com.daimler.sechub.test.TestURLBuilder.*;
import static com.daimler.sechub.test.TestURLBuilder.RestDocPathParameter.*;
import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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

import com.daimler.sechub.docgen.util.RestDocPathFactory;
import com.daimler.sechub.domain.administration.user.AnonymousUserRequestNewApiTokenRestController;
import com.daimler.sechub.domain.administration.user.AnonymousUserRequestsNewApiTokenService;
import com.daimler.sechub.sharedkernel.Profiles;
import com.daimler.sechub.sharedkernel.configuration.AbstractAllowSecHubAPISecurityConfiguration;
import com.daimler.sechub.sharedkernel.usecases.UseCaseRestDoc;
import com.daimler.sechub.sharedkernel.usecases.user.UseCaseUserRequestsNewApiToken;
import com.daimler.sechub.test.ExampleConstants;
import com.daimler.sechub.test.TestPortProvider;

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

		/* execute */
		/* @formatter:off */
        this.mockMvc.perform(
        		post(https(PORT_USED).buildAnonymousRequestNewApiToken(EMAIL_ADDRESS.pathElement()),"emailAdress@test.com").
        		contentType(MediaType.APPLICATION_JSON_VALUE)
        		)./*andDo(print()).*/
        			andExpect(status().isOk()).andDo(
        					document(RestDocPathFactory.createPath(UseCaseUserRequestsNewApiToken.class),
        							pathParameters(
        									parameterWithName(EMAIL_ADDRESS.paramName()).description("Email address for user where api token shall be refreshed.")
        									)
        							)
        		);

        /* @formatter:on */
	}

	@TestConfiguration
	@Profile(Profiles.TEST)
	@EnableAutoConfiguration
	public static class SimpleTestConfiguration extends AbstractAllowSecHubAPISecurityConfiguration {

	}

}
