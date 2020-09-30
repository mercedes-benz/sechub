// SPDX-License-Identifier: MIT
package com.daimler.sechub.restdoc;

import static com.daimler.sechub.test.TestURLBuilder.*;
import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import com.daimler.sechub.docgen.util.RestDocPathFactory;
import com.daimler.sechub.server.core.AnonymousCheckAliveRestController;
import com.daimler.sechub.sharedkernel.Profiles;
import com.daimler.sechub.sharedkernel.configuration.AbstractAllowSecHubAPISecurityConfiguration;
import com.daimler.sechub.sharedkernel.usecases.UseCaseRestDoc;
import com.daimler.sechub.sharedkernel.usecases.anonymous.UseCaseAnonymousCheckAlive;
import com.daimler.sechub.test.ExampleConstants;
import com.daimler.sechub.test.TestPortProvider;

@RunWith(SpringRunner.class)
@WebMvcTest(AnonymousCheckAliveRestController.class)
@ContextConfiguration(classes= {AnonymousCheckAliveRestController.class, AnonymousCheckAliveRestDocTest.SimpleTestConfiguration.class})
@WithMockUser
@ActiveProfiles(Profiles.TEST)
@AutoConfigureRestDocs(uriScheme="https",uriHost=ExampleConstants.URI_SECHUB_SERVER,uriPort=443)
public class AnonymousCheckAliveRestDocTest {
	private static final int PORT_USED = TestPortProvider.DEFAULT_INSTANCE.getRestDocTestPort();

	@Autowired
	private MockMvc mockMvc;

	@Test
	@UseCaseRestDoc(useCase=UseCaseAnonymousCheckAlive.class, variant = "HEAD")
	public void calling_check_alive_head_returns_HTTP_200() throws Exception {

		/* execute */
		/* @formatter:off */
        this.mockMvc.perform(
        			head(https(PORT_USED).buildCheckIsAliveUrl())
        		).
        andExpect(status().isOk()).
        andDo(document(RestDocPathFactory.createPath(UseCaseAnonymousCheckAlive.class, "HEAD")));

        /* @formatter:on */
	}
	
	@Test
	@UseCaseRestDoc(useCase=UseCaseAnonymousCheckAlive.class, variant = "GET")
	public void calling_check_alive_get_returns_HTTP_200() throws Exception {

		/* execute */
		/* @formatter:off */
        this.mockMvc.perform(
        			get(https(PORT_USED).buildCheckIsAliveUrl())
        		).
        andExpect(status().isOk()).
        andDo(document(RestDocPathFactory.createPath(UseCaseAnonymousCheckAlive.class, "GET")));

        /* @formatter:on */
	}

	@TestConfiguration
	@Profile(Profiles.TEST)
	@EnableAutoConfiguration
	public static class SimpleTestConfiguration extends AbstractAllowSecHubAPISecurityConfiguration{

	}
}
