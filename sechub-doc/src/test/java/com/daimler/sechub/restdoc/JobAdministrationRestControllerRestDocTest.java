// SPDX-License-Identifier: MIT
package com.daimler.sechub.restdoc;

import static com.daimler.sechub.test.TestURLBuilder.https;
import static com.daimler.sechub.test.TestURLBuilder.RestDocPathParameter.JOB_UUID;
import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

import com.daimler.sechub.docgen.util.RestDocFactory;
import com.daimler.sechub.domain.administration.job.JobAdministrationRestController;
import com.daimler.sechub.domain.administration.job.JobCancelService;
import com.daimler.sechub.domain.administration.job.JobInformation;
import com.daimler.sechub.domain.administration.job.JobInformationListService;
import com.daimler.sechub.domain.administration.job.JobRestartRequestService;
import com.daimler.sechub.domain.administration.job.JobStatus;
import com.daimler.sechub.sharedkernel.Profiles;
import com.daimler.sechub.sharedkernel.RoleConstants;
import com.daimler.sechub.sharedkernel.configuration.AbstractAllowSecHubAPISecurityConfiguration;
import com.daimler.sechub.sharedkernel.usecases.UseCaseRestDoc;
import com.daimler.sechub.sharedkernel.usecases.job.UseCaseAdminCancelsJob;
import com.daimler.sechub.sharedkernel.usecases.job.UseCaseAdminRestartsJob;
import com.daimler.sechub.sharedkernel.usecases.job.UseCaseAdminRestartsJobHard;
import com.daimler.sechub.sharedkernel.usecases.job.UseCaseAdminListsAllRunningJobs;
import com.daimler.sechub.test.ExampleConstants;
import com.daimler.sechub.test.TestPortProvider;
import com.epages.restdocs.apispec.ResourceSnippetParameters;

@RunWith(SpringRunner.class)
@WebMvcTest(JobAdministrationRestController.class)
@ContextConfiguration(classes = { JobAdministrationRestController.class,
		JobAdministrationRestControllerRestDocTest.SimpleTestConfiguration.class })
@WithMockUser(authorities = RoleConstants.ROLE_SUPERADMIN)
@ActiveProfiles({Profiles.TEST, Profiles.ADMIN_ACCESS})
@AutoConfigureRestDocs(uriScheme="https",uriHost=ExampleConstants.URI_SECHUB_SERVER,uriPort=443)
public class JobAdministrationRestControllerRestDocTest {

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
		List<JobInformation> list = new ArrayList<>();
		JobInformation info = new JobInformation();
		info.setJobUUID(UUID.randomUUID());
		info.setStatus(JobStatus.RUNNING);
		info.setProjectId("project-name");
		info.setConfiguration("{ config data }");
		info.setOwner("owner-userid");
		info.setSince(LocalDateTime.now());

		list.add(info);

		when(jobListService.fetchRunningJobs()).thenReturn(list);
	}

	@Test
	@UseCaseRestDoc(useCase=UseCaseAdminListsAllRunningJobs.class)
	public void restdoc_list_all_running_jobs() throws Exception {
        /* prepare */
        String apiEndpoint = https(PORT_USED).buildAdminFetchAllRunningJobsUrl();
        Class<? extends Annotation> useCase = UseCaseAdminListsAllRunningJobs.class;
        
		/* execute + test @formatter:off */
		this.mockMvc.perform(
				get(apiEndpoint).
				contentType(MediaType.APPLICATION_JSON_VALUE)
				)./*
		andDo(print()).
				*/
		andExpect(status().isOk()).
		andDo(document(RestDocFactory.createPath(useCase),
                resource(
                        ResourceSnippetParameters.builder().
                            summary(RestDocFactory.createSummary(useCase)).
                            description(RestDocFactory.createDescription(useCase)).
                            tag(RestDocFactory.extractTag(apiEndpoint)).
                            responseSchema(OpenApiSchema.RUNNING_JOB_LIST.getSchema()).
                            responseFields(
                                    fieldWithPath(inArray(JobInformation.PROPERTY_JOB_UUID)).description("The uuid of the running job"),
                                    fieldWithPath(inArray(JobInformation.PROPERTY_PROJECT_ID)).description("The name of the project the job is running for"),
                                    fieldWithPath(inArray(JobInformation.PROPERTY_OWNER)).description("Owner of the job - means user which triggered it"),
                                    fieldWithPath(inArray(JobInformation.PROPERTY_STATUS)).description("A status information "),
                                    fieldWithPath(inArray(JobInformation.PROPERTY_SINCE)).description("Timestamp since when job has been started"),
                                    fieldWithPath(inArray(JobInformation.PROPERTY_CONFIGURATION)).description("Configuration used for this job")
                            ).
                            build()
                         )
				));

		/* @formatter:on */
	}

	@Test
	@UseCaseRestDoc(useCase=UseCaseAdminCancelsJob.class)
	public void restdoc_cancel_job() throws Exception {
        /* prepare */
        String apiEndpoint = https(PORT_USED).buildAdminCancelsJob(JOB_UUID.pathElement());
        Class<? extends Annotation> useCase = UseCaseAdminCancelsJob.class;
        
		/* execute + test @formatter:off */
		UUID jobUUID = UUID.randomUUID();

		this.mockMvc.perform(
				post(apiEndpoint, jobUUID).
				contentType(MediaType.APPLICATION_JSON_VALUE)
				)./*
		andDo(print()).
				*/
		andExpect(status().isOk()).
		andDo(document(RestDocFactory.createPath(useCase),
                resource(
                        ResourceSnippetParameters.builder().
                            summary(RestDocFactory.createSummary(useCase)).
                            description(RestDocFactory.createDescription(useCase)).
                            tag(RestDocFactory.extractTag(apiEndpoint)).
                            pathParameters(
                                    parameterWithName(JOB_UUID.paramName()).description("The job UUID")
                                ).
                            build()
                         )
		        ));

		/* @formatter:on */
	}
	
	@Test
    @UseCaseRestDoc(useCase=UseCaseAdminRestartsJob.class)
    public void restdoc_restart_job() throws Exception {
        /* prepare */
        String apiEndpoint = https(PORT_USED).buildAdminRestartsJob(JOB_UUID.pathElement());
        Class<? extends Annotation> useCase = UseCaseAdminRestartsJob.class;
        
        /* execute + test @formatter:off */
        UUID jobUUID = UUID.randomUUID();

        this.mockMvc.perform(
                post(apiEndpoint, jobUUID).
                contentType(MediaType.APPLICATION_JSON_VALUE)
                )./*
        andDo(print()).
                */
        andExpect(status().isOk()).
        andDo(document(RestDocFactory.createPath(useCase),
                resource(
                        ResourceSnippetParameters.builder().
                            summary(RestDocFactory.createSummary(useCase)).
                            description(RestDocFactory.createDescription(useCase)).
                            tag(RestDocFactory.extractTag(apiEndpoint)).
                            pathParameters(
                                    parameterWithName(JOB_UUID.paramName()).description("The job UUID")
                            ).
                            build()
                         )
                ));

        /* @formatter:on */
    }
	
	@Test
    @UseCaseRestDoc(useCase=UseCaseAdminRestartsJobHard.class)
    public void restdoc_restart_job_hard() throws Exception {
        /* prepare */
        String apiEndpoint = https(PORT_USED).buildAdminRestartsJobHard(JOB_UUID.pathElement());
        Class<? extends Annotation> useCase = UseCaseAdminRestartsJobHard.class;
        
        /* execute + test @formatter:off */
        UUID jobUUID = UUID.randomUUID();

        this.mockMvc.perform(
                post(apiEndpoint,jobUUID).
                contentType(MediaType.APPLICATION_JSON_VALUE)
                )./*
        andDo(print()).
                */
        andExpect(status().isOk()).
        andDo(document(RestDocFactory.createPath(useCase),
                resource(
                        ResourceSnippetParameters.builder().
                            summary(RestDocFactory.createSummary(useCase)).
                            description(RestDocFactory.createDescription(useCase)).
                            tag(RestDocFactory.extractTag(apiEndpoint)).
                            pathParameters(
                                    parameterWithName(JOB_UUID.paramName()).description("The job UUID")
                            ).
                            build()
                         )
              ));

        /* @formatter:on */
    }
	
	

	// see https://docs.spring.io/spring-restdocs/docs/current/reference/html5/#documenting-your-api-request-response-payloads-fields-json
	private static String inArray(String field) {
		return "[]."+field;
	}
	@EnableAutoConfiguration
	public static class SimpleTestConfiguration extends AbstractAllowSecHubAPISecurityConfiguration {

	}

}
