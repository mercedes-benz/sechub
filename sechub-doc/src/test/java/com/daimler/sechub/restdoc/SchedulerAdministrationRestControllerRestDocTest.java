// SPDX-License-Identifier: MIT
package com.daimler.sechub.restdoc;

import static com.daimler.sechub.test.TestURLBuilder.https;
import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

import com.daimler.sechub.docgen.util.RestDocFactory;
import com.daimler.sechub.domain.administration.scheduler.SchedulerAdministrationRestController;
import com.daimler.sechub.domain.administration.scheduler.SwitchSchedulerJobProcessingService;
import com.daimler.sechub.domain.administration.scheduler.TriggerSchedulerStatusRefreshService;
import com.daimler.sechub.sharedkernel.Profiles;
import com.daimler.sechub.sharedkernel.RoleConstants;
import com.daimler.sechub.sharedkernel.configuration.AbstractAllowSecHubAPISecurityConfiguration;
import com.daimler.sechub.sharedkernel.usecases.UseCaseRestDoc;
import com.daimler.sechub.sharedkernel.usecases.admin.schedule.UseCaseAdminDisablesSchedulerJobProcessing;
import com.daimler.sechub.sharedkernel.usecases.admin.schedule.UseCaseAdminEnablesSchedulerJobProcessing;
import com.daimler.sechub.sharedkernel.usecases.admin.schedule.UseCaseAdminTriggersRefreshOfSchedulerStatus;
import com.daimler.sechub.test.ExampleConstants;
import com.daimler.sechub.test.TestPortProvider;
import com.epages.restdocs.apispec.ResourceSnippetParameters;

@RunWith(SpringRunner.class)
@WebMvcTest(SchedulerAdministrationRestController.class)
@ContextConfiguration(classes = { SchedulerAdministrationRestController.class, SchedulerAdministrationRestControllerRestDocTest.SimpleTestConfiguration.class })
@WithMockUser(authorities = RoleConstants.ROLE_SUPERADMIN)
@ActiveProfiles({ Profiles.TEST, Profiles.ADMIN_ACCESS })
@AutoConfigureRestDocs(uriScheme = "https", uriHost = ExampleConstants.URI_SECHUB_SERVER, uriPort = 443)
public class SchedulerAdministrationRestControllerRestDocTest {

    private static final int PORT_USED = TestPortProvider.DEFAULT_INSTANCE.getRestDocTestPort();

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    SwitchSchedulerJobProcessingService switchJobProcessingService;

    @MockBean
    TriggerSchedulerStatusRefreshService triggerRefreshService;

    @Before
    public void before() {
    }

    @Test
    @UseCaseRestDoc(useCase = UseCaseAdminTriggersRefreshOfSchedulerStatus.class)
    public void restdoc_admin_triggers_refresh_scheduler_status() throws Exception {
        /* prepare */
        String apiEndpoint = https(PORT_USED).buildAdminTriggersRefreshOfSchedulerStatus();
        Class<? extends Annotation> useCase = UseCaseAdminTriggersRefreshOfSchedulerStatus.class;

        /* execute + test @formatter:off */
		this.mockMvc.perform(
				post(apiEndpoint).
				contentType(MediaType.APPLICATION_JSON_VALUE)
				).
		andExpect(status().isAccepted()).
		andDo(document(RestDocFactory.createPath(useCase),
                resource(
                        ResourceSnippetParameters.builder().
                            summary(RestDocFactory.createSummary(useCase)).
                            description(RestDocFactory.createDescription(useCase)).
                            tag(RestDocFactory.extractTag(apiEndpoint)).
                            build()
                        )
		        ));
		/* @formatter:on */
    }

    @Test
    @UseCaseRestDoc(useCase = UseCaseAdminDisablesSchedulerJobProcessing.class)
    public void restdoc_admin_disables_scheduler_job_processing() throws Exception {
        /* prepare */
        String apiEndpoint = https(PORT_USED).buildAdminDisablesSchedulerJobProcessing();
        Class<? extends Annotation> useCase = UseCaseAdminDisablesSchedulerJobProcessing.class;

        /* execute + test @formatter:off */
		this.mockMvc.perform(
				post(apiEndpoint).
				contentType(MediaType.APPLICATION_JSON_VALUE)
				).
		andExpect(status().isAccepted()).
		andDo(document(RestDocFactory.createPath(useCase),
                    resource(
                            ResourceSnippetParameters.builder().
                                summary(RestDocFactory.createSummary(useCase)).
                                description(RestDocFactory.createDescription(useCase)).
                                tag(RestDocFactory.extractTag(apiEndpoint)).
                                build()
                            )
		        ));
		/* @formatter:on */
    }

    @Test
    @UseCaseRestDoc(useCase = UseCaseAdminEnablesSchedulerJobProcessing.class)
    public void restdoc_admin_enables_scheduler_job_processing() throws Exception {
        /* prepare */
        String apiEndpoint = https(PORT_USED).buildAdminDisablesSchedulerJobProcessing();
        Class<? extends Annotation> useCase = UseCaseAdminEnablesSchedulerJobProcessing.class;

        /* execute + test @formatter:off */
		this.mockMvc.perform(
				post(https(PORT_USED).buildAdminEnablesSchedulerJobProcessing()).
				contentType(MediaType.APPLICATION_JSON_VALUE)
				).
		andExpect(status().isAccepted()).
		andDo(document(RestDocFactory.createPath(useCase),
                    resource(
                            ResourceSnippetParameters.builder().
                                summary(RestDocFactory.createSummary(useCase)).
                                description(RestDocFactory.createDescription(useCase)).
                                tag(RestDocFactory.extractTag(apiEndpoint)).
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
