// SPDX-License-Identifier: MIT
package com.daimler.sechub.restdoc;

import static com.daimler.sechub.test.TestURLBuilder.*;
import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.*;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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

import com.daimler.sechub.docgen.util.RestDocPathFactory;
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
import com.daimler.sechub.sharedkernel.usecases.job.UseCaseAdministratorCancelsJob;
import com.daimler.sechub.sharedkernel.usecases.job.UseCaseAdministratorListsAllRunningJobs;
import com.daimler.sechub.sharedkernel.usecases.job.UseCaseAdministratorRestartsJob;
import com.daimler.sechub.sharedkernel.usecases.job.UseCaseAdministratorRestartsJobHard;
import com.daimler.sechub.test.ExampleConstants;
import com.daimler.sechub.test.TestPortProvider;

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
	@UseCaseRestDoc(useCase=UseCaseAdministratorListsAllRunningJobs.class)
	public void restdoc_list_all_running_jobs() throws Exception {

		/* execute + test @formatter:off */
		this.mockMvc.perform(
				get(https(PORT_USED).buildAdminFetchAllRunningJobsUrl()).
				contentType(MediaType.APPLICATION_JSON_VALUE)
				)./*
		andDo(print()).
				*/
		andExpect(status().isOk()).
		andDo(document(RestDocPathFactory.createPath(UseCaseAdministratorListsAllRunningJobs.class),
//				requestFields(
//						fieldWithPath(ProjectJsonInput.PROPERTY_API_VERSION).description("The api version, currently only 1.0 is supported"),
//						fieldWithPath(ProjectJsonInput.PROPERTY_WHITELIST+"."+ProjectWhiteList.PROPERTY_URIS).description("All URIS used now for whitelisting. Former parts will be replaced completely!"),
//						fieldWithPath(ProjectJsonInput.PROPERTY_NAME).description("Name of the project to create. Is also used as a unique ID!")
//						)
//				,
				responseFields(
						fieldWithPath(inArray(JobInformation.PROPERTY_JOB_UUID)).description("The uuid of the running job"),
						fieldWithPath(inArray(JobInformation.PROPERTY_PROJECT_ID)).description("The name of the project the job is running for"),
						fieldWithPath(inArray(JobInformation.PROPERTY_OWNER)).description("Owner of the job - means user which triggered it"),
						fieldWithPath(inArray(JobInformation.PROPERTY_STATUS)).description("A status information "),
						fieldWithPath(inArray(JobInformation.PROPERTY_SINCE)).description("Timestamp since when job has been started"),
						fieldWithPath(inArray(JobInformation.PROPERTY_CONFIGURATION)).description("Configuration used for this job")
					)
				)

				);

		/* @formatter:on */
	}

	@Test
	@UseCaseRestDoc(useCase=UseCaseAdministratorCancelsJob.class)
	public void restdoc_cancel_job() throws Exception {

		/* execute + test @formatter:off */
		UUID jobUUID = UUID.randomUUID();

		this.mockMvc.perform(
				post(https(PORT_USED).buildAdminCancelsJob(jobUUID)).
				contentType(MediaType.APPLICATION_JSON_VALUE)
				)./*
		andDo(print()).
				*/
		andExpect(status().isOk()).
		andDo(document(RestDocPathFactory.createPath(UseCaseAdministratorCancelsJob.class))

				);

		/* @formatter:on */
	}
	
	@Test
    @UseCaseRestDoc(useCase=UseCaseAdministratorRestartsJob.class)
    public void restdoc_restart_job() throws Exception {

        /* execute + test @formatter:off */
        UUID jobUUID = UUID.randomUUID();

        this.mockMvc.perform(
                post(https(PORT_USED).buildAdminRestartsJob(jobUUID)).
                contentType(MediaType.APPLICATION_JSON_VALUE)
                )./*
        andDo(print()).
                */
        andExpect(status().isOk()).
        andDo(document(RestDocPathFactory.createPath(UseCaseAdministratorRestartsJob.class))

                );

        /* @formatter:on */
    }
	
	@Test
    @UseCaseRestDoc(useCase=UseCaseAdministratorRestartsJobHard.class)
    public void restdoc_restart_job_jard() throws Exception {

        /* execute + test @formatter:off */
        UUID jobUUID = UUID.randomUUID();

        this.mockMvc.perform(
                post(https(PORT_USED).buildAdminRestartsJobHard(jobUUID)).
                contentType(MediaType.APPLICATION_JSON_VALUE)
                )./*
        andDo(print()).
                */
        andExpect(status().isOk()).
        andDo(document(RestDocPathFactory.createPath(UseCaseAdministratorRestartsJobHard.class))

                );

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
