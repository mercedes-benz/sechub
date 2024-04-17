// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.restdoc;

import static com.mercedesbenz.sechub.restdoc.RestDocumentation.defineRestService;
import static com.mercedesbenz.sechub.test.SecHubTestURLBuilder.https;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import com.mercedesbenz.sechub.server.core.ServerRuntimeData;
import com.mercedesbenz.sechub.sharedkernel.Profiles;
import com.mercedesbenz.sechub.sharedkernel.RoleConstants;
import com.mercedesbenz.sechub.sharedkernel.configuration.AbstractSecHubAPISecurityConfiguration;
import com.mercedesbenz.sechub.sharedkernel.usecases.UseCaseRestDoc;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.status.UseCaseAdminFetchesServerRuntimeData;
import com.mercedesbenz.sechub.test.ExampleConstants;
import com.mercedesbenz.sechub.test.TestIsNecessaryForDocumentation;
import com.mercedesbenz.sechub.test.TestPortProvider;

@RunWith(SpringRunner.class)
@WebMvcTest(ServerInfoAdministrationRestController.class)
@ContextConfiguration(classes = { ServerInfoAdministrationRestController.class,
        ServerInfoAdministrationRestControllerRestDocTest.SimpleTestConfiguration.class })
@WithMockUser(roles = RoleConstants.ROLE_SUPERADMIN)
@ActiveProfiles({ Profiles.TEST, Profiles.ADMIN_ACCESS })
@AutoConfigureRestDocs(uriScheme = "https", uriHost = ExampleConstants.URI_SECHUB_SERVER, uriPort = 443)
public class ServerInfoAdministrationRestControllerRestDocTest implements TestIsNecessaryForDocumentation {

    private static final int PORT_USED = TestPortProvider.DEFAULT_INSTANCE.getRestDocTestPort();

    private static final String SECHUB_SERVER_VERSION = "0.12.3";
    // private static final String SERVER_VERSION = "serverVersion";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    InfoService serverInfoService;

    @Test
    @UseCaseRestDoc(useCase = UseCaseAdminFetchesServerRuntimeData.class)
    public void restdoc_admin_get_server_version_as_Json() throws Exception {
        /* prepare */
        String apiEndpoint = https(PORT_USED).buildGetServerRuntimeDataUrl();
        Class<? extends Annotation> useCase = UseCaseAdminFetchesServerRuntimeData.class;

        when(serverInfoService.getServerRuntimeData()).thenReturn(new ServerRuntimeData(SECHUB_SERVER_VERSION));

        String expectedContent = "{\"serverVersion\":\"" + SECHUB_SERVER_VERSION + "\"}";

        /* execute + test @formatter:off */
		this.mockMvc.perform(
				get(apiEndpoint).
					contentType(MediaType.APPLICATION_JSON).
					header(AuthenticationHelper.HEADER_NAME, AuthenticationHelper.getHeaderValue())
				).
					andExpect(status().isOk()).
					andExpect(content().string(expectedContent)).
					andDo(defineRestService().
		                        with().
		                            useCaseData(useCase).
		                            tag(RestDocFactory.extractTag(apiEndpoint)).
		                            responseSchema(OpenApiSchema.SERVER_RUNTIME_DATA.getSchema()).
		                        and().
		                        document(
        	                            responseFields(
        	                                    fieldWithPath("serverVersion").description("The sechub server version.")
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
