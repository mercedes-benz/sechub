// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.restdoc;

import static com.mercedesbenz.sechub.restdoc.RestDocumentation.*;
import static com.mercedesbenz.sechub.test.SecHubTestURLBuilder.*;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.lang.annotation.Annotation;

import org.junit.Before;
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
import com.mercedesbenz.sechub.domain.administration.scheduler.SchedulerAdministrationRestController;
import com.mercedesbenz.sechub.domain.administration.scheduler.SwitchSchedulerJobProcessingService;
import com.mercedesbenz.sechub.domain.administration.scheduler.TriggerSchedulerStatusRefreshService;
import com.mercedesbenz.sechub.sharedkernel.Profiles;
import com.mercedesbenz.sechub.sharedkernel.security.RoleConstants;
import com.mercedesbenz.sechub.sharedkernel.usecases.UseCaseRestDoc;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.schedule.UseCaseAdminDisablesSchedulerJobProcessing;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.schedule.UseCaseAdminEnablesSchedulerJobProcessing;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.schedule.UseCaseAdminTriggersRefreshOfSchedulerStatus;
import com.mercedesbenz.sechub.test.ExampleConstants;
import com.mercedesbenz.sechub.test.TestIsNecessaryForDocumentation;
import com.mercedesbenz.sechub.test.TestPortProvider;

@RunWith(SpringRunner.class)
@WebMvcTest
@ContextConfiguration(classes = { SchedulerAdministrationRestController.class })
@Import(TestRestDocSecurityConfiguration.class)
@WithMockUser(roles = RoleConstants.ROLE_SUPERADMIN)
@ActiveProfiles({ Profiles.TEST, Profiles.ADMIN_ACCESS })
@AutoConfigureRestDocs(uriScheme = "https", uriHost = ExampleConstants.URI_SECHUB_SERVER, uriPort = 443)
public class SchedulerAdministrationRestControllerRestDocTest implements TestIsNecessaryForDocumentation {

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
                		)
		);
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
                )
		);
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
                )
        );
		/* @formatter:on */
    }

}
