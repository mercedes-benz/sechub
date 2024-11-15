// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.restdoc;

import static com.mercedesbenz.sechub.restdoc.RestDocumentation.*;
import static com.mercedesbenz.sechub.test.SecHubTestURLBuilder.*;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.lang.annotation.Annotation;

import org.junit.Before;
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
import com.mercedesbenz.sechub.domain.administration.autocleanup.AdministrationAutoCleanupConfig;
import com.mercedesbenz.sechub.domain.administration.config.AdministrationConfigService;
import com.mercedesbenz.sechub.domain.administration.config.ConfigAdministrationRestController;
import com.mercedesbenz.sechub.domain.administration.scheduler.SchedulerAdministrationRestController;
import com.mercedesbenz.sechub.sharedkernel.Profiles;
import com.mercedesbenz.sechub.sharedkernel.security.AbstractSecHubAPISecurityConfiguration;
import com.mercedesbenz.sechub.sharedkernel.security.RoleConstants;
import com.mercedesbenz.sechub.sharedkernel.usecases.UseCaseRestDoc;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.config.UseCaseAdminFetchesAutoCleanupConfiguration;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.config.UseCaseAdminUpdatesAutoCleanupConfiguration;
import com.mercedesbenz.sechub.test.ExampleConstants;
import com.mercedesbenz.sechub.test.TestIsNecessaryForDocumentation;
import com.mercedesbenz.sechub.test.TestPortProvider;

@RunWith(SpringRunner.class)
@WebMvcTest(SchedulerAdministrationRestController.class)
@ContextConfiguration(classes = { ConfigAdministrationRestController.class, ConfigAdministrationRestControllerRestDocTest.SimpleTestConfiguration.class })
@WithMockUser(roles = RoleConstants.ROLE_SUPERADMIN)
@ActiveProfiles({ Profiles.TEST, Profiles.ADMIN_ACCESS })
@AutoConfigureRestDocs(uriScheme = "https", uriHost = ExampleConstants.URI_SECHUB_SERVER, uriPort = 443)
public class ConfigAdministrationRestControllerRestDocTest implements TestIsNecessaryForDocumentation {

    private static final int PORT_USED = TestPortProvider.DEFAULT_INSTANCE.getRestDocTestPort();

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    AdministrationConfigService configService;

    @Before
    public void before() {
    }

    @Test
    @UseCaseRestDoc(useCase = UseCaseAdminUpdatesAutoCleanupConfiguration.class)
    public void restdoc_admin_updates_auto_cleanup_configuration() throws Exception {
        /* prepare */
        String apiEndpoint = https(PORT_USED).buildAdminUpdatesAutoCleanupConfigurationUrl();
        Class<? extends Annotation> useCase = UseCaseAdminUpdatesAutoCleanupConfiguration.class;

        AdministrationAutoCleanupConfig config = new AdministrationAutoCleanupConfig();

        /* execute + test @formatter:off */
		this.mockMvc.perform(
					put(apiEndpoint).
					content(config.toJSON()).
					contentType(MediaType.APPLICATION_JSON_VALUE).
					header(AuthenticationHelper.HEADER_NAME, AuthenticationHelper.getHeaderValue())
				).
		andExpect(status().isAccepted()).
		andDo(defineRestService().
                with().
                    useCaseData(useCase).
                    tag(RestDocFactory.extractTag(apiEndpoint)).
                and().
                document(
	                		requestHeaders(

	                		)
                ));
		/* @formatter:on */
    }

    @Test
    @UseCaseRestDoc(useCase = UseCaseAdminFetchesAutoCleanupConfiguration.class)
    public void restdoc_admin_fetches_auto_cleanup_configuration() throws Exception {
        /* prepare */
        String apiEndpoint = https(PORT_USED).buildAdminFetchesAutoCleanupConfigurationUrl();
        Class<? extends Annotation> useCase = UseCaseAdminFetchesAutoCleanupConfiguration.class;

        AdministrationAutoCleanupConfig config = new AdministrationAutoCleanupConfig();
        when(configService.fetchAutoCleanupConfiguration()).thenReturn(config);

        /* execute + test @formatter:off */
		this.mockMvc.perform(
					get(apiEndpoint).
					contentType(MediaType.APPLICATION_JSON_VALUE).
					header(AuthenticationHelper.HEADER_NAME, AuthenticationHelper.getHeaderValue())
				).
		andExpect(status().isOk()).
		andExpect(content().json(config.toJSON())).
		andDo(defineRestService().
                with().
                    useCaseData(useCase).
                    tag(RestDocFactory.extractTag(apiEndpoint)).
                and().
                document(
                		requestHeaders(

                		)
        ));
		/* @formatter:on */
    }

    @Profile(Profiles.TEST)
    @EnableAutoConfiguration
    public static class SimpleTestConfiguration extends AbstractSecHubAPISecurityConfiguration {

    }

}
