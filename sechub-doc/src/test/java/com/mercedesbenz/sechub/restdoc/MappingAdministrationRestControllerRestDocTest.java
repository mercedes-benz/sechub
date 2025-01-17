// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.restdoc;

import static com.mercedesbenz.sechub.restdoc.RestDocumentationTest.*;
import static com.mercedesbenz.sechub.test.SecHubTestURLBuilder.*;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import com.mercedesbenz.sechub.docgen.util.RestDocFactory;
import com.mercedesbenz.sechub.domain.administration.scheduler.SchedulerStatusEntryKeys;
import com.mercedesbenz.sechub.domain.administration.status.ListStatusService;
import com.mercedesbenz.sechub.domain.administration.status.StatusAdministrationRestController;
import com.mercedesbenz.sechub.domain.administration.status.StatusEntry;
import com.mercedesbenz.sechub.sharedkernel.Profiles;
import com.mercedesbenz.sechub.sharedkernel.security.RoleConstants;
import com.mercedesbenz.sechub.sharedkernel.usecases.UseCaseRestDoc;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.status.UseCaseAdminListsStatusInformation;
import com.mercedesbenz.sechub.test.ExampleConstants;
import com.mercedesbenz.sechub.test.TestIsNecessaryForDocumentation;
import com.mercedesbenz.sechub.test.TestPortProvider;

@RunWith(SpringRunner.class)
@WebMvcTest
@ContextConfiguration(classes = { StatusAdministrationRestController.class })
@WithMockUser(roles = RoleConstants.ROLE_SUPERADMIN)
@ActiveProfiles({ Profiles.TEST, Profiles.ADMIN_ACCESS })
@AutoConfigureRestDocs(uriScheme = "https", uriHost = ExampleConstants.URI_SECHUB_SERVER, uriPort = 443)
public class MappingAdministrationRestControllerRestDocTest implements TestIsNecessaryForDocumentation {

    private static final int PORT_USED = TestPortProvider.DEFAULT_INSTANCE.getRestDocTestPort();

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    ListStatusService listStatusService;

    @Before
    public void before() {
        List<StatusEntry> list = new ArrayList<StatusEntry>();
        addStatusEntry(list, SchedulerStatusEntryKeys.SCHEDULER_ENABLED, "true");

        addStatusEntry(list, SchedulerStatusEntryKeys.SCHEDULER_JOBS_ALL, "100");

        addStatusEntry(list, SchedulerStatusEntryKeys.SCHEDULER_JOBS_INITIALIZING, "1");
        addStatusEntry(list, SchedulerStatusEntryKeys.SCHEDULER_JOBS_READY_TO_START, "19");
        addStatusEntry(list, SchedulerStatusEntryKeys.SCHEDULER_JOBS_STARTED, "20");
        addStatusEntry(list, SchedulerStatusEntryKeys.SCHEDULER_JOBS_ENDED, "50");
        addStatusEntry(list, SchedulerStatusEntryKeys.SCHEDULER_JOBS_CANCEL_REQUESTED, "2");
        addStatusEntry(list, SchedulerStatusEntryKeys.SCHEDULER_JOBS_CANCELED, "8");

        /*
         * there could be more status examples in future - currently only scheduler
         * status info available
         */
        when(listStatusService.fetchAllStatusEntries()).thenReturn(list);
    }

    private void addStatusEntry(List<StatusEntry> list, SchedulerStatusEntryKeys key, String value) {
        StatusEntry entry = new StatusEntry(key);
        entry.setValue(value);
        list.add(entry);
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
					contentType(MediaType.APPLICATION_JSON_VALUE).
					header(TestAuthenticationHelper.HEADER_NAME, TestAuthenticationHelper.getHeaderValue())
				).
		andDo(print()).
		andExpect(status().isOk()).
		andDo(defineRestService().
                with().
                    useCaseData(useCase).
                    tag(RestDocFactory.extractTag(apiEndpoint)).
                    responseSchema(TestOpenApiSchema.STATUS_INFORMATION.getSchema()).
                and().
                document(
                		requestHeaders(

                		),
                            responseFields(
                                    fieldWithPath("[]."+StatusEntry.PROPERTY_KEY).description("Status key identifier"),
                                    fieldWithPath("[]."+StatusEntry.PROPERTY_VALUE).description("Status value")
                         )
					));

		/* @formatter:on */
    }

}
