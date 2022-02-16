// SPDX-License-Identifier: MIT
package com.daimler.sechub.restdoc;

import static com.daimler.sechub.test.TestURLBuilder.https;
import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

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
import com.daimler.sechub.domain.administration.scheduler.SchedulerStatusEntryKeys;
import com.daimler.sechub.domain.administration.status.ListStatusService;
import com.daimler.sechub.domain.administration.status.StatusAdministrationRestController;
import com.daimler.sechub.domain.administration.status.StatusEntry;
import com.daimler.sechub.sharedkernel.Profiles;
import com.daimler.sechub.sharedkernel.RoleConstants;
import com.daimler.sechub.sharedkernel.configuration.AbstractAllowSecHubAPISecurityConfiguration;
import com.daimler.sechub.sharedkernel.usecases.UseCaseRestDoc;
import com.daimler.sechub.sharedkernel.usecases.admin.status.UseCaseAdminListsStatusInformation;
import com.daimler.sechub.test.ExampleConstants;
import com.daimler.sechub.test.TestPortProvider;
import com.epages.restdocs.apispec.ResourceSnippetParameters;

@RunWith(SpringRunner.class)
@WebMvcTest(StatusAdministrationRestController.class)
@ContextConfiguration(classes = { StatusAdministrationRestController.class, MappingAdministrationRestControllerRestDocTest.SimpleTestConfiguration.class })
@WithMockUser(authorities = RoleConstants.ROLE_SUPERADMIN)
@ActiveProfiles({ Profiles.TEST, Profiles.ADMIN_ACCESS })
@AutoConfigureRestDocs(uriScheme = "https", uriHost = ExampleConstants.URI_SECHUB_SERVER, uriPort = 443)
public class MappingAdministrationRestControllerRestDocTest {

    private static final int PORT_USED = TestPortProvider.DEFAULT_INSTANCE.getRestDocTestPort();

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    ListStatusService listStatusService;

    @Before
    public void before() {
        List<StatusEntry> list = new ArrayList<StatusEntry>();
        StatusEntry enabled = new StatusEntry(SchedulerStatusEntryKeys.SCHEDULER_ENABLED);
        enabled.setValue("true");
        list.add(enabled);

        StatusEntry allJobs = new StatusEntry(SchedulerStatusEntryKeys.SCHEDULER_JOBS_ALL);
        allJobs.setValue("200");
        list.add(allJobs);

        StatusEntry runningJobs = new StatusEntry(SchedulerStatusEntryKeys.SCHEDULER_JOBS_RUNNING);
        runningJobs.setValue("3");
        list.add(runningJobs);

        StatusEntry waitingJobs = new StatusEntry(SchedulerStatusEntryKeys.SCHEDULER_JOBS_WAITING);
        waitingJobs.setValue("42");
        list.add(waitingJobs);

        /*
         * there could be more status examples in future - currently only scheduler
         * status info available
         */
        when(listStatusService.fetchAllStatusEntries()).thenReturn(list);
    }

    @Test
    @UseCaseRestDoc(useCase = UseCaseAdminListsStatusInformation.class)
    public void restdoc_admin_lists_status_information() throws Exception {
        /* prepare */
        String apiEndpoint = https(PORT_USED).buildAdminListsStatusEntries();
        Class<? extends Annotation> useCase = UseCaseAdminListsStatusInformation.class;

        /* execute + test @formatter:off */
		this.mockMvc.perform(
				get(apiEndpoint).
				contentType(MediaType.APPLICATION_JSON_VALUE)
				)./*
				*/
		andDo(print()).
		andExpect(status().isOk()).
		andDo(document(RestDocFactory.createPath(useCase),
                resource(
                        ResourceSnippetParameters.builder().
                            summary(RestDocFactory.createSummary(useCase)).
                            description(RestDocFactory.createDescription(useCase)).
                            tag(RestDocFactory.extractTag(apiEndpoint)).
                            responseSchema(OpenApiSchema.STATUS_INFORMATION.getSchema()).
                            responseFields(
                                    fieldWithPath("[]."+StatusEntry.PROPERTY_KEY).description("Status key identifier"),
                                    fieldWithPath("[]."+StatusEntry.PROPERTY_VALUE).description("Status value")
                            ).
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
