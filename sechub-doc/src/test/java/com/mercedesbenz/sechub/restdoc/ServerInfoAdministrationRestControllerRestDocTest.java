// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.restdoc;

import static com.mercedesbenz.sechub.restdoc.RestDocumentation.*;
import static com.mercedesbenz.sechub.test.SecHubTestURLBuilder.*;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.lang.annotation.Annotation;

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

import com.mercedesbenz.sechub.docgen.util.RestDocFactory;
import com.mercedesbenz.sechub.server.core.InfoService;
import com.mercedesbenz.sechub.server.core.ServerInfoAdministrationRestController;
import com.mercedesbenz.sechub.sharedkernel.Profiles;
import com.mercedesbenz.sechub.sharedkernel.RoleConstants;
import com.mercedesbenz.sechub.sharedkernel.configuration.AbstractAllowSecHubAPISecurityConfiguration;
import com.mercedesbenz.sechub.sharedkernel.usecases.UseCaseRestDoc;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.status.UseCaseAdminChecksServerVersion;
import com.mercedesbenz.sechub.test.ExampleConstants;
import com.mercedesbenz.sechub.test.TestIsNecessaryForDocumentation;
import com.mercedesbenz.sechub.test.TestPortProvider;

@RunWith(SpringRunner.class)
@WebMvcTest(ServerInfoAdministrationRestController.class)
@ContextConfiguration(classes = { ServerInfoAdministrationRestController.class,
        ServerInfoAdministrationRestControllerRestDocTest.SimpleTestConfiguration.class })
@WithMockUser(authorities = RoleConstants.ROLE_SUPERADMIN)
@ActiveProfiles({ Profiles.TEST, Profiles.ADMIN_ACCESS })
@AutoConfigureRestDocs(uriScheme = "https", uriHost = ExampleConstants.URI_SECHUB_SERVER, uriPort = 443)
public class ServerInfoAdministrationRestControllerRestDocTest implements TestIsNecessaryForDocumentation {

    private static final int PORT_USED = TestPortProvider.DEFAULT_INSTANCE.getRestDocTestPort();

    private static final String SERVER_VERSION = "0.12.3";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    InfoService serverInfoService;

    @Test
    @UseCaseRestDoc(useCase = UseCaseAdminChecksServerVersion.class)
    public void restdoc_admin_get_server_version() throws Exception {
        /* prepare */
        String apiEndpoint = https(PORT_USED).buildGetServerVersionUrl();
        Class<? extends Annotation> useCase = UseCaseAdminChecksServerVersion.class;

        when(serverInfoService.getVersionAsString()).thenReturn(SERVER_VERSION);

        /* execute + test @formatter:off */
		this.mockMvc.perform(
				get(apiEndpoint).
					contentType(MediaType.TEXT_PLAIN_VALUE).
					header(AuthenticationHelper.HEADER_NAME, AuthenticationHelper.getHeaderValue())
				).
					andExpect(status().isOk()).
					andExpect(content().string(SERVER_VERSION)).
					andDo(defineRestService().
		                        with().
		                            useCaseData(useCase).
		                            tag(RestDocFactory.extractTag(apiEndpoint)).
		                            responseSchema(OpenApiSchema.SERVER_VERSION.getSchema()).
		                        and().
		                        document(
			    	                		requestHeaders(

			    	                		)
		                        )
					);
		/* @formatter:on */
    }

    @Profile(Profiles.TEST)
    @EnableAutoConfiguration
    public static class SimpleTestConfiguration extends AbstractAllowSecHubAPISecurityConfiguration {

    }
}
