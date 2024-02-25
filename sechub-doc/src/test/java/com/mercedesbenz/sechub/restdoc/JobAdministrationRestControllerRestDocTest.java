// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.restdoc;

import static com.mercedesbenz.sechub.restdoc.RestDocumentation.*;
import static com.mercedesbenz.sechub.test.RestDocPathParameter.*;
import static com.mercedesbenz.sechub.test.SecHubTestURLBuilder.*;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.lang.annotation.Annotation;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
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
import com.mercedesbenz.sechub.domain.administration.job.JobAdministrationRestController;
import com.mercedesbenz.sechub.domain.administration.job.JobCancelService;
import com.mercedesbenz.sechub.domain.administration.job.JobInformationListEntry;
import com.mercedesbenz.sechub.domain.administration.job.JobInformationListService;
import com.mercedesbenz.sechub.domain.administration.job.JobRestartRequestService;
import com.mercedesbenz.sechub.domain.administration.job.JobStatus;
import com.mercedesbenz.sechub.sharedkernel.Profiles;
import com.mercedesbenz.sechub.sharedkernel.RoleConstants;
import com.mercedesbenz.sechub.sharedkernel.configuration.AbstractAllowSecHubAPISecurityConfiguration;
import com.mercedesbenz.sechub.sharedkernel.usecases.UseCaseRestDoc;
import com.mercedesbenz.sechub.sharedkernel.usecases.job.UseCaseAdminCancelsJob;
import com.mercedesbenz.sechub.sharedkernel.usecases.job.UseCaseAdminListsAllRunningJobs;
import com.mercedesbenz.sechub.sharedkernel.usecases.job.UseCaseAdminRestartsJob;
import com.mercedesbenz.sechub.sharedkernel.usecases.job.UseCaseAdminRestartsJobHard;
import com.mercedesbenz.sechub.test.ExampleConstants;
import com.mercedesbenz.sechub.test.TestIsNecessaryForDocumentation;
import com.mercedesbenz.sechub.test.TestPortProvider;

@RunWith(SpringRunner.class)
@WebMvcTest(JobAdministrationRestController.class)
@ContextConfiguration(classes = { JobAdministrationRestController.class, JobAdministrationRestControllerRestDocTest.SimpleTestConfiguration.class })
@WithMockUser(authorities = RoleConstants.ROLE_SUPERADMIN)
@ActiveProfiles({ Profiles.TEST, Profiles.ADMIN_ACCESS })
@AutoConfigureRestDocs(uriScheme = "https", uriHost = ExampleConstants.URI_SECHUB_SERVER, uriPort = 443)
public class JobAdministrationRestControllerRestDocTest implements TestIsNecessaryForDocumentation {

    private static final int PORT_USED = TestPortProvider.DEFAULT_INSTANCE.getRestDocTestPort();

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    JobInformationListService jobListService;

    @MockBean
    JobCancelService jobCancelService;

    @MockBean
    JobRestartRequestService jobRestartRequestService;

    @Before
    public void before() {
        List<JobInformationListEntry> list = new ArrayList<>();

        JobInformationListEntry info = new JobInformationListEntry(UUID.randomUUID(), LocalDateTime.now(), JobStatus.RUNNING, "project-id");

        list.add(info);

        when(jobListService.fetchRunningJobs()).thenReturn(list);
    }

    @Test
    @UseCaseRestDoc(useCase = UseCaseAdminListsAllRunningJobs.class)
    public void restdoc_list_all_running_jobs() throws Exception {
        /* prepare */
        String apiEndpoint = https(PORT_USED).buildAdminFetchAllRunningJobsUrl();
        Class<? extends Annotation> useCase = UseCaseAdminListsAllRunningJobs.class;

        /* execute + test @formatter:off */
		this.mockMvc.perform(
					get(apiEndpoint).
					contentType(MediaType.APPLICATION_JSON_VALUE).
					header(AuthenticationHelper.HEADER_NAME, AuthenticationHelper.getHeaderValue())
				).
		andExpect(status().isOk()).
		andDo(defineRestService().
                with().
                    useCaseData(useCase).
                    tag(RestDocFactory.extractTag(apiEndpoint)).
                    responseSchema(OpenApiSchema.RUNNING_JOB_LIST.getSchema()).
                and().
                document(
	                		requestHeaders(

	                		),
	                        responseFields(
	                                    fieldWithPath(inArray(JobInformationListEntry.PROPERTY_JOB_UUID)).description("The uuid of the running job"),
	                                    fieldWithPath(inArray(JobInformationListEntry.PROPERTY_SINCE)).description("Timestamp since when job has been started"),
	                                    fieldWithPath(inArray(JobInformationListEntry.PROPERTY_STATUS)).description("A status information "),
	                                    fieldWithPath(inArray(JobInformationListEntry.PROPERTY_PROJECT_ID)).description("The name of the project the job is running for")
	                         )
				));

		/* @formatter:on */
    }

    @Test
    @UseCaseRestDoc(useCase = UseCaseAdminCancelsJob.class)
    public void restdoc_cancel_job() throws Exception {
        /* prepare */
        String apiEndpoint = https(PORT_USED).buildAdminCancelsJob(JOB_UUID.pathElement());
        Class<? extends Annotation> useCase = UseCaseAdminCancelsJob.class;

        /* execute + test @formatter:off */
		UUID jobUUID = UUID.randomUUID();

		this.mockMvc.perform(
					post(apiEndpoint, jobUUID).
					contentType(MediaType.APPLICATION_JSON_VALUE).
					header(AuthenticationHelper.HEADER_NAME, AuthenticationHelper.getHeaderValue())
				).
		andExpect(status().isOk()).
		andDo(defineRestService().
                with().
                    useCaseData(useCase).
                    tag(RestDocFactory.extractTag(apiEndpoint)).
                and().
                document(
                		requestHeaders(

                		),
                        pathParameters(
                                parameterWithName(JOB_UUID.paramName()).description("The job UUID")
                        )
		        ));

		/* @formatter:on */
    }

    @Test
    @UseCaseRestDoc(useCase = UseCaseAdminRestartsJob.class)
    public void restdoc_restart_job() throws Exception {
        /* prepare */
        String apiEndpoint = https(PORT_USED).buildAdminRestartsJob(JOB_UUID.pathElement());
        Class<? extends Annotation> useCase = UseCaseAdminRestartsJob.class;

        /* execute + test @formatter:off */
        UUID jobUUID = UUID.randomUUID();

        this.mockMvc.perform(
	                post(apiEndpoint, jobUUID).
	                contentType(MediaType.APPLICATION_JSON_VALUE).
	                header(AuthenticationHelper.HEADER_NAME, AuthenticationHelper.getHeaderValue())
                ).
        andExpect(status().isOk()).
        andDo(defineRestService().
                with().
                    useCaseData(useCase).
                    tag(RestDocFactory.extractTag(apiEndpoint)).
                and().
                document(
                		 requestHeaders(

                		 ),
                         pathParameters(
                                    parameterWithName(JOB_UUID.paramName()).description("The job UUID")
                         )
                ));

        /* @formatter:on */
    }

    @Test
    @UseCaseRestDoc(useCase = UseCaseAdminRestartsJobHard.class)
    public void restdoc_restart_job_hard() throws Exception {
        /* prepare */
        String apiEndpoint = https(PORT_USED).buildAdminRestartsJobHard(JOB_UUID.pathElement());
        Class<? extends Annotation> useCase = UseCaseAdminRestartsJobHard.class;

        /* execute + test @formatter:off */
        UUID jobUUID = UUID.randomUUID();

        this.mockMvc.perform(
	                post(apiEndpoint,jobUUID).
	                contentType(MediaType.APPLICATION_JSON_VALUE).
	                header(AuthenticationHelper.HEADER_NAME, AuthenticationHelper.getHeaderValue())
                ).
        andExpect(status().isOk()).
        andDo(defineRestService().
                with().
                    useCaseData(useCase).
                    tag(RestDocFactory.extractTag(apiEndpoint)).
                and().
                document(
                		requestHeaders(

                		),
                        pathParameters(
                                    parameterWithName(JOB_UUID.paramName()).description("The job UUID")
                        )
              ));

        /* @formatter:on */
    }

    // see
    // https://docs.spring.io/spring-restdocs/docs/current/reference/html5/#documenting-your-api-request-response-payloads-fields-json
    private static String inArray(String field) {
        return "[]." + field;
    }

    @EnableAutoConfiguration
    public static class SimpleTestConfiguration extends AbstractAllowSecHubAPISecurityConfiguration {

    }

}
