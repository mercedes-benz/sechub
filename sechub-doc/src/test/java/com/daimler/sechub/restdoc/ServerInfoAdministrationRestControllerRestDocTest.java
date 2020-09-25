// SPDX-License-Identifier: MIT
package com.daimler.sechub.restdoc;

import static com.daimler.sechub.test.TestURLBuilder.*;
import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.*;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import com.daimler.sechub.docgen.util.RestDocPathFactory;
import com.daimler.sechub.server.core.InfoService;
import com.daimler.sechub.server.core.ServerInfoAdministrationRestController;
import com.daimler.sechub.sharedkernel.Profiles;
import com.daimler.sechub.sharedkernel.RoleConstants;
import com.daimler.sechub.sharedkernel.configuration.AbstractAllowSecHubAPISecurityConfiguration;
import com.daimler.sechub.sharedkernel.usecases.UseCaseRestDoc;
import com.daimler.sechub.sharedkernel.usecases.admin.status.UseCaseAdministratorChecksServerVersion;
import com.daimler.sechub.test.ExampleConstants;
import com.daimler.sechub.test.TestPortProvider;

@RunWith(SpringRunner.class)
@WebMvcTest(ServerInfoAdministrationRestController.class)
@ContextConfiguration(classes = { ServerInfoAdministrationRestController.class,
		ServerInfoAdministrationRestControllerRestDocTest.SimpleTestConfiguration.class })
@WithMockUser(authorities = RoleConstants.ROLE_SUPERADMIN)
@ActiveProfiles({Profiles.TEST, Profiles.ADMIN_ACCESS})
@AutoConfigureRestDocs(uriScheme="https",uriHost=ExampleConstants.URI_SECHUB_SERVER,uriPort=443)
public class ServerInfoAdministrationRestControllerRestDocTest {

	private static final int PORT_USED = TestPortProvider.DEFAULT_INSTANCE.getRestDocTestPort();
	
	private static final String SERVER_VERSION = "0.12.3";

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	InfoService serverInfoService;

	@Test
	@UseCaseRestDoc(useCase = UseCaseAdministratorChecksServerVersion.class)
	public void restdoc_admin_get_server_version() throws Exception {
		/*  prepare */
		when(serverInfoService.getVersionAsString()).thenReturn(SERVER_VERSION);
		
		/* execute + test @formatter:off */
		this.mockMvc.perform(
				get(https(PORT_USED).buildGetServerVersionUrl()).
					contentType(MediaType.TEXT_PLAIN_VALUE)
				).
					andExpect(status().isOk()).
					andExpect(content().string(SERVER_VERSION)).
					andDo(document(RestDocPathFactory.createPath(UseCaseAdministratorChecksServerVersion.class))
				);
		/* @formatter:on */
	}

	@Profile(Profiles.TEST)
	@EnableAutoConfiguration
	public static class SimpleTestConfiguration extends AbstractAllowSecHubAPISecurityConfiguration {

	}
}
