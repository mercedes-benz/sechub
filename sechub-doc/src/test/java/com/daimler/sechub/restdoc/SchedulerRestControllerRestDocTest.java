// SPDX-License-Identifier: MIT
package com.daimler.sechub.restdoc;

import static com.daimler.sechub.sharedkernel.configuration.SecHubConfiguration.PROPERTY_API_VERSION;
import static com.daimler.sechub.sharedkernel.configuration.SecHubConfiguration.PROPERTY_CODE_SCAN;
import static com.daimler.sechub.sharedkernel.configuration.SecHubConfiguration.PROPERTY_INFRA_SCAN;
import static com.daimler.sechub.sharedkernel.configuration.SecHubConfiguration.PROPERTY_WEB_SCAN;
import static com.daimler.sechub.sharedkernel.configuration.TestSecHubConfigurationBuilder.configureSecHub;
import static com.daimler.sechub.test.TestURLBuilder.https;
import static com.daimler.sechub.test.TestURLBuilder.RestDocPathParameter.JOB_UUID;
import static com.daimler.sechub.test.TestURLBuilder.RestDocPathParameter.PROJECT_ID;
import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.fileUpload;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.put;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.partWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;
import static org.springframework.restdocs.request.RequestDocumentation.requestParts;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.StringUtils;

import com.daimler.sechub.adapter.ActionType;
import com.daimler.sechub.adapter.SecHubTimeUnit;
import com.daimler.sechub.commons.model.TrafficLight;
import com.daimler.sechub.docgen.util.RestDocPathFactory;
import com.daimler.sechub.docgen.util.RestDocTestFileSupport;
import com.daimler.sechub.domain.schedule.ExecutionResult;
import com.daimler.sechub.domain.schedule.ExecutionState;
import com.daimler.sechub.domain.schedule.ScheduleJobStatus;
import com.daimler.sechub.domain.schedule.SchedulerApproveJobService;
import com.daimler.sechub.domain.schedule.SchedulerCreateJobService;
import com.daimler.sechub.domain.schedule.SchedulerGetJobStatusService;
import com.daimler.sechub.domain.schedule.SchedulerRestController;
import com.daimler.sechub.domain.schedule.SchedulerResult;
import com.daimler.sechub.domain.schedule.SchedulerUploadService;
import com.daimler.sechub.domain.schedule.access.ScheduleAccess;
import com.daimler.sechub.domain.schedule.access.ScheduleAccess.ProjectAccessCompositeKey;
import com.daimler.sechub.domain.schedule.access.ScheduleAccessRepository;
import com.daimler.sechub.domain.schedule.job.ScheduleSecHubJob;
import com.daimler.sechub.domain.schedule.job.SecHubJobRepository;
import com.daimler.sechub.sharedkernel.Profiles;
import com.daimler.sechub.sharedkernel.configuration.AbstractAllowSecHubAPISecurityConfiguration;
import com.daimler.sechub.sharedkernel.configuration.SecHubCodeScanConfiguration;
import com.daimler.sechub.sharedkernel.configuration.SecHubConfiguration;
import com.daimler.sechub.sharedkernel.configuration.SecHubConfigurationValidator;
import com.daimler.sechub.sharedkernel.configuration.SecHubFileSystemConfiguration;
import com.daimler.sechub.sharedkernel.configuration.SecHubInfrastructureScanConfiguration;
import com.daimler.sechub.sharedkernel.configuration.SecHubWebScanConfiguration;
import com.daimler.sechub.sharedkernel.configuration.login.FormLoginConfiguration;
import com.daimler.sechub.sharedkernel.configuration.login.WebLoginConfiguration;
import com.daimler.sechub.sharedkernel.usecases.UseCaseRestDoc;
import com.daimler.sechub.sharedkernel.usecases.user.execute.UseCaseUserApprovesJob;
import com.daimler.sechub.sharedkernel.usecases.user.execute.UseCaseUserChecksJobStatus;
import com.daimler.sechub.sharedkernel.usecases.user.execute.UseCaseUserCreatesNewJob;
import com.daimler.sechub.sharedkernel.usecases.user.execute.UseCaseUserUploadsSourceCode;
import com.daimler.sechub.test.ExampleConstants;
import com.daimler.sechub.test.TestPortProvider;

@RunWith(SpringRunner.class)
@WebMvcTest(SchedulerRestController.class)
@ContextConfiguration(classes = { SchedulerRestController.class,
		SchedulerRestControllerRestDocTest.SimpleTestConfiguration.class })
@WithMockUser
@ActiveProfiles(Profiles.TEST)
@AutoConfigureRestDocs(uriScheme="https",uriHost=ExampleConstants.URI_SECHUB_SERVER,uriPort=443)
public class SchedulerRestControllerRestDocTest {

	private static final String PROJECT1_ID = "project1";

	private static final int PORT_USED = TestPortProvider.DEFAULT_INSTANCE.getRestDocTestPort();
	
    private static final String FORM = WebLoginConfiguration.PROPERTY_FORM;
    private static final String SCRIPT = FormLoginConfiguration.PROPERTY_SCRIPT;

	@Autowired
	private MockMvc mockMvc;
	@MockBean
	private SchedulerApproveJobService mockedScheduleService;

	@MockBean
	private SchedulerCreateJobService mockedScheduleCreateJobService;

	@MockBean
	private SchedulerGetJobStatusService mockedScheduleJobStatusService;

	@MockBean
	private SecHubConfigurationValidator sechubConfigurationValidator;

	@MockBean
	private SchedulerUploadService mockeduploadService;

	@MockBean
	private SecHubJobRepository mockedJobRepository;

	@MockBean
	private ScheduleAccessRepository mockedProjectRepository;

	private ScheduleAccess project1;

	private UUID randomUUID;

	@Test
	@UseCaseRestDoc(useCase = UseCaseUserCreatesNewJob.class, variant = "Code Scan")
	public void restDoc_userCreatesNewJob_codescan() throws Exception {
		/* prepare */
		UUID randomUUID = UUID.randomUUID();
		SchedulerResult mockResult = new SchedulerResult(randomUUID);

		when(mockedScheduleCreateJobService.createJob(any(), any(SecHubConfiguration.class))).thenReturn(mockResult);

		/* execute + test @formatter:off */
	    this.mockMvc.perform(
	    		post(https(PORT_USED).buildAddJobUrl(PROJECT_ID.pathElement()),PROJECT1_ID).
	    			contentType(MediaType.APPLICATION_JSON_VALUE).
	    			content(configureSecHub().
	    					api("1.0").
	    					codeScanConfig().
	    						setFileSystemFolders("testproject1/src/main/java","testproject2/src/main/java").
	    					build().
	    					toJSON())
	    		).
	    			andExpect(status().isOk()).
	    			andExpect(content().json("{jobId:"+randomUUID.toString()+"}")).
	    			andDo(document(RestDocPathFactory.createPath(UseCaseUserCreatesNewJob.class,"Code Scan"),
	    						pathParameters(
										parameterWithName(PROJECT_ID.paramName()).description("The unique id of the project id where a new sechub job shall be created")
	    									),
	    						requestFields(
										fieldWithPath(PROPERTY_API_VERSION).description("The api version, currently only 1.0 is supported"),
										fieldWithPath(PROPERTY_CODE_SCAN).description("Code scan configuration block").optional(),
										fieldWithPath(PROPERTY_CODE_SCAN+"."+SecHubCodeScanConfiguration.PROPERTY_FILESYSTEM+"."+SecHubFileSystemConfiguration.PROPERTY_FOLDERS).description("Code scan sources from given file system folders").optional()

										),
	    						responseFields(
	    								fieldWithPath(SchedulerResult.PROPERTY_JOBID).description("A unique job id"))

	    						)
	    		);

	    /* @formatter:on */
	}
	
	@Test
	@UseCaseRestDoc(useCase = UseCaseUserCreatesNewJob.class, variant = "Infrastructure scan")
	public void restDoc_userCreatesNewJob_infrascan() throws Exception {
		/* prepare */
		UUID randomUUID = UUID.randomUUID();
		SchedulerResult mockResult = new SchedulerResult(randomUUID);
	
		when(mockedScheduleCreateJobService.createJob(any(), any(SecHubConfiguration.class))).thenReturn(mockResult);
	
		/* execute + test @formatter:off */
	    this.mockMvc.perform(
	    		post(https(PORT_USED).buildAddJobUrl(PROJECT_ID.pathElement()),PROJECT1_ID).
	    			contentType(MediaType.APPLICATION_JSON_VALUE).
	    			content(configureSecHub().
	    					api("1.0").
	    					infraConfig().
	    						addURI("https://localhost").
	    						addIP("127.0.0.1").
	    					build().
	    					toJSON())
	    		).
	    			andExpect(status().isOk()).
	    			andExpect(content().json("{jobId:"+randomUUID.toString()+"}")).
	    			andDo(document(RestDocPathFactory.createPath(UseCaseUserCreatesNewJob.class,"Infrastructure scan"),
	    						pathParameters(
										parameterWithName(PROJECT_ID.paramName()).description("The unique id of the project id where a new sechub job shall be created")
	    									),
	    						requestFields(
										fieldWithPath(PROPERTY_API_VERSION).description("The api version, currently only 1.0 is supported"),
										fieldWithPath(PROPERTY_INFRA_SCAN).description("Infrastructure configuration block").optional(),
										fieldWithPath(PROPERTY_INFRA_SCAN+"."+SecHubInfrastructureScanConfiguration.PROPERTY_URIS).description("Infrastructure URIs to scan for").optional(),
										fieldWithPath(PROPERTY_INFRA_SCAN+"."+SecHubInfrastructureScanConfiguration.PROPERTY_IPS).description("Infrastructure IPs to scan for").optional()
	
										),
	    						responseFields(
	    								fieldWithPath(SchedulerResult.PROPERTY_JOBID).description("A unique job id"))
	
	    						)
	    		);
	
	    /* @formatter:on */
	}

	@Test
	@UseCaseRestDoc(useCase = UseCaseUserCreatesNewJob.class, variant = "Web Scan anonymous")
	public void restDoc_userCreatesNewJob_webscan_anonymous() throws Exception {
		/* prepare */
		UUID randomUUID = UUID.randomUUID();
		SchedulerResult mockResult = new SchedulerResult(randomUUID);

		when(mockedScheduleCreateJobService.createJob(any(), any(SecHubConfiguration.class))).thenReturn(mockResult);

		/* execute + test @formatter:off */
	    this.mockMvc.perform(
	    		post(https(PORT_USED).buildAddJobUrl(PROJECT_ID.pathElement()),PROJECT1_ID).
	    			contentType(MediaType.APPLICATION_JSON_VALUE).
	    			content(configureSecHub().
	    					api("1.0").
	    					webConfig().
	    						addURI("https://localhost/mywebapp/login").
	    					build().
	    					toJSON())
	    		).
	    			andExpect(status().isOk()).
	    			andExpect(content().json("{jobId:"+randomUUID.toString()+"}")).
	    			andDo(document(RestDocPathFactory.createPath(UseCaseUserCreatesNewJob.class,"Web Scan anonymous"),
	    						pathParameters(
										parameterWithName(PROJECT_ID.paramName()).description("The unique id of the project id where a new sechub job shall be created")
	    									),
	    						requestFields(
										fieldWithPath(PROPERTY_API_VERSION).description("The api version, currently only 1.0 is supported"),
										fieldWithPath(PROPERTY_WEB_SCAN).description("Webscan configuration block").optional(),
										fieldWithPath(PROPERTY_WEB_SCAN+"."+SecHubWebScanConfiguration.PROPERTY_URIS).description("Webscan URIs to scan for").optional()

										),
	    						responseFields(
	    								fieldWithPath(SchedulerResult.PROPERTY_JOBID).description("A unique job id"))

	    						)
	    		);

	    /* @formatter:on */
	}
	
	@Test
	@UseCaseRestDoc(useCase = UseCaseUserCreatesNewJob.class, variant = "Web Scan login basic")
	public void restDoc_userCreatesNewJob_webscan_login_basic() throws Exception {
		/* prepare */
		UUID randomUUID = UUID.randomUUID();
		SchedulerResult mockResult = new SchedulerResult(randomUUID);

		when(mockedScheduleCreateJobService.createJob(any(), any(SecHubConfiguration.class))).thenReturn(mockResult);

		/* execute + test @formatter:off */
	    this.mockMvc.perform(
	    		post(https(PORT_USED).buildAddJobUrl(PROJECT_ID.pathElement()),PROJECT1_ID).
	    			contentType(MediaType.APPLICATION_JSON_VALUE).
	    			content(configureSecHub().
	    					api("1.0").
	    					webConfig().
	    						addURI("https://localhost/mywebapp").
	    						login("https://localhost/mywebapp/login").basic("username1","password1").
	    					build().
	    					toJSON())
	    		).
	    			andExpect(status().isOk()).
	    			andExpect(content().json("{jobId:"+randomUUID.toString()+"}")).
	    			andDo(document(RestDocPathFactory.createPath(UseCaseUserCreatesNewJob.class,"Web Scan login basic"),
	    						pathParameters(
										parameterWithName(PROJECT_ID.paramName()).description("The unique id of the project id where a new sechub job shall be created")
	    									),
	    						requestFields(
										fieldWithPath(PROPERTY_API_VERSION).description("The api version, currently only 1.0 is supported"),
										fieldWithPath(PROPERTY_WEB_SCAN).description("Webscan configuration block").optional(),
										fieldWithPath(PROPERTY_WEB_SCAN+"."+SecHubWebScanConfiguration.PROPERTY_URIS).description("Webscan URIs to scan for").optional(),
										fieldWithPath(PROPERTY_WEB_SCAN+"."+SecHubWebScanConfiguration.PROPERTY_LOGIN).description("Webscan login definition").optional(),
										fieldWithPath(PROPERTY_WEB_SCAN+"."+SecHubWebScanConfiguration.PROPERTY_LOGIN+".url").description("Login URL").optional(),
										fieldWithPath(PROPERTY_WEB_SCAN+"."+SecHubWebScanConfiguration.PROPERTY_LOGIN+"."+WebLoginConfiguration.PROPERTY_BASIC).description("basic login definition").optional(),
										fieldWithPath(PROPERTY_WEB_SCAN+"."+SecHubWebScanConfiguration.PROPERTY_LOGIN+"."+WebLoginConfiguration.PROPERTY_BASIC+".user").description("username").optional(),
										fieldWithPath(PROPERTY_WEB_SCAN+"."+SecHubWebScanConfiguration.PROPERTY_LOGIN+"."+WebLoginConfiguration.PROPERTY_BASIC+".password").description("password").optional()

										),
	    						responseFields(
	    								fieldWithPath(SchedulerResult.PROPERTY_JOBID).description("A unique job id"))

	    						)
	    		);

	    /* @formatter:on */
	}
	
	@Test
	@UseCaseRestDoc(useCase = UseCaseUserCreatesNewJob.class, variant = "Web Scan login form auto dection")
	public void restDoc_userCreatesNewJob_webScan_login_form_autodetect() throws Exception {
		/* prepare */
		UUID randomUUID = UUID.randomUUID();
		SchedulerResult mockResult = new SchedulerResult(randomUUID);

		when(mockedScheduleCreateJobService.createJob(any(), any(SecHubConfiguration.class))).thenReturn(mockResult);

		/* execute + test @formatter:off */
	    this.mockMvc.perform(
	    		post(https(PORT_USED).buildAddJobUrl(PROJECT_ID.pathElement()),PROJECT1_ID).
	    			contentType(MediaType.APPLICATION_JSON_VALUE).
	    			content(configureSecHub().
	    					api("1.0").
	    					webConfig().
	    						addURI("https://localhost/mywebapp").
	    						login("https://localhost/mywebapp/login").formAuto("username1","password1").
	    					build().
	    					toJSON())
	    		).
	    			andExpect(status().isOk()).
	    			andExpect(content().json("{jobId:"+randomUUID.toString()+"}")).
	    			andDo(document(RestDocPathFactory.createPath(UseCaseUserCreatesNewJob.class,"Web Scan login form auto dection"),
	    						pathParameters(
										parameterWithName(PROJECT_ID.paramName()).description("The unique id of the project id where a new sechub job shall be created")
	    									),
	    						requestFields(
										fieldWithPath(PROPERTY_API_VERSION).description("The api version, currently only 1.0 is supported"),
										fieldWithPath(PROPERTY_WEB_SCAN).description("Webscan configuration block").optional(),
										fieldWithPath(PROPERTY_WEB_SCAN+"."+SecHubWebScanConfiguration.PROPERTY_URIS).description("Webscan URIs to scan for").optional(),
										fieldWithPath(PROPERTY_WEB_SCAN+"."+SecHubWebScanConfiguration.PROPERTY_LOGIN).description("Webscan login definition").optional(),
										fieldWithPath(PROPERTY_WEB_SCAN+"."+SecHubWebScanConfiguration.PROPERTY_LOGIN+".url").description("Login URL").optional(),
										fieldWithPath(PROPERTY_WEB_SCAN+"."+SecHubWebScanConfiguration.PROPERTY_LOGIN+"."+WebLoginConfiguration.PROPERTY_FORM).description("form login definition").optional(),
										fieldWithPath(PROPERTY_WEB_SCAN+"."+SecHubWebScanConfiguration.PROPERTY_LOGIN+"."+WebLoginConfiguration.PROPERTY_FORM+".autodetect").description("login field auto detection").optional(),
										fieldWithPath(PROPERTY_WEB_SCAN+"."+SecHubWebScanConfiguration.PROPERTY_LOGIN+"."+WebLoginConfiguration.PROPERTY_FORM+".autodetect.user").description("username").optional(),
										fieldWithPath(PROPERTY_WEB_SCAN+"."+SecHubWebScanConfiguration.PROPERTY_LOGIN+"."+WebLoginConfiguration.PROPERTY_FORM+".autodetect.password").description("password").optional()

										),
	    						responseFields(
	    								fieldWithPath(SchedulerResult.PROPERTY_JOBID).description("A unique job id"))

	    						)
	    		);

	    /* @formatter:on */
	}
	
	@Test
	@UseCaseRestDoc(useCase = UseCaseUserCreatesNewJob.class, variant = "Web Scan login form scripted")
	public void restDoc_userCreatesNewJob_webScan_login_form_script() throws Exception {
		/* prepare */
		UUID randomUUID = UUID.randomUUID();
		SchedulerResult mockResult = new SchedulerResult(randomUUID);

		when(mockedScheduleCreateJobService.createJob(any(), any(SecHubConfiguration.class))).thenReturn(mockResult);

		/* execute + test @formatter:off */
	    this.mockMvc.perform(
	    		post(https(PORT_USED).buildAddJobUrl(PROJECT_ID.pathElement()),PROJECT1_ID).
	    			contentType(MediaType.APPLICATION_JSON_VALUE).
	    			content(configureSecHub().
	    					api("1.0").
	    					webConfig().
	    						addURI("https://localhost/mywebapp").
	    						login("https://localhost/mywebapp/login").
	    						  formScripted("username1","password1").
	    						    createPage().
    	    						    createAction().
    	    						        type(ActionType.USERNAME).
    	    						        selector("#example_login_userid").
    	    						        value("username1").
    	    						        description("the username field").
    	    						        add().
    	    						    createAction().
    	    						        type(ActionType.INPUT).
    	    						        selector("#example_login_email_id").
    	    						        value("user@example.com").
    	    						        description("The email id field.").
    	    						        add().
                                        add().
                                   createPage().
    	    						    createAction().
    	    						        type(ActionType.WAIT).
    	    						        value("2345").
    	    						        unit(SecHubTimeUnit.MILLISECOND).
    	    						        add().
    	    						    createAction().
    	    						        type(ActionType.PASSWORD).
    	    						        selector("#example_login_pwd").
    	    						        value("Super$ecret234!").
    	    						        add().
    	    						    createAction().
    	    						        type(ActionType.CLICK).
    	    						        selector("#example_login_button").
    	    						        add().
    	                                add().
	    						  done().
	    					build().
	    					toJSON())
	    		).
	    			andExpect(status().isOk()).
	    			andExpect(content().json("{jobId:"+randomUUID.toString()+"}")).
	    			andDo(document(RestDocPathFactory.createPath(UseCaseUserCreatesNewJob.class,"Web Scan login form scripted"),
	    						pathParameters(
										parameterWithName(PROJECT_ID.paramName()).description("The unique id of the project id where a new sechub job shall be created")
	    									),
	    						requestFields(
										fieldWithPath(PROPERTY_API_VERSION).description("The api version, currently only 1.0 is supported"),
										fieldWithPath(PROPERTY_WEB_SCAN).description("Webscan configuration block").optional(),
										fieldWithPath(PROPERTY_WEB_SCAN+"."+SecHubWebScanConfiguration.PROPERTY_URIS).description("Webscan URIs to scan for").optional(),
										fieldWithPath(PROPERTY_WEB_SCAN+"."+SecHubWebScanConfiguration.PROPERTY_LOGIN).description("Webscan login definition").optional(),
										fieldWithPath(PROPERTY_WEB_SCAN+"."+SecHubWebScanConfiguration.PROPERTY_LOGIN+".url").description("Login URL").optional(),
										fieldWithPath(PROPERTY_WEB_SCAN+"."+SecHubWebScanConfiguration.PROPERTY_LOGIN+"."+FORM).description("form login definition").optional(),
										fieldWithPath(PROPERTY_WEB_SCAN+"."+SecHubWebScanConfiguration.PROPERTY_LOGIN+"."+FORM+"."+SCRIPT).description("script").optional(),
										fieldWithPath(PROPERTY_WEB_SCAN+"."+SecHubWebScanConfiguration.PROPERTY_LOGIN+"."+FORM+"."+SCRIPT+".pages[].actions[].type").description("action type: username, password, input, click, wait").optional(),
										fieldWithPath(PROPERTY_WEB_SCAN+"."+SecHubWebScanConfiguration.PROPERTY_LOGIN+"."+FORM+"."+SCRIPT+".pages[].actions[].selector").description("css selector").optional(),
										fieldWithPath(PROPERTY_WEB_SCAN+"."+SecHubWebScanConfiguration.PROPERTY_LOGIN+"."+FORM+"."+SCRIPT+".pages[].actions[].value").description("value").optional(),
										fieldWithPath(PROPERTY_WEB_SCAN+"."+SecHubWebScanConfiguration.PROPERTY_LOGIN+"."+FORM+"."+SCRIPT+".pages[].actions[].description").description("description").optional(),
										fieldWithPath(PROPERTY_WEB_SCAN+"."+SecHubWebScanConfiguration.PROPERTY_LOGIN+"."+FORM+"."+SCRIPT+".pages[].actions[].unit").description("the time unit to wait: millisecond, second, minute, hour, day.").optional()
										),
	    						responseFields(
	    								fieldWithPath(SchedulerResult.PROPERTY_JOBID).description("A unique job id"))
	    						)
	    		);

	    /* @formatter:on */
	}
	
	@Test
	@UseCaseRestDoc(useCase = UseCaseUserUploadsSourceCode.class)
	public void restDoc_userUploadsSourceCode() throws Exception {
		/* prepare */

		ScheduleSecHubJob job = new ScheduleSecHubJob() {
			public UUID getUUID() {
				return randomUUID;
			};
		};
		job.setExecutionResult(ExecutionResult.OK);
		job.setStarted(LocalDateTime.now().minusMinutes(15));
		job.setEnded(LocalDateTime.now());
		job.setExecutionState(ExecutionState.INITIALIZING);
		job.setOwner("CREATOR1");
		job.setTrafficLight(TrafficLight.GREEN);

		ScheduleJobStatus status = new ScheduleJobStatus(job);

		when(mockedScheduleJobStatusService.getJobStatus(PROJECT1_ID, randomUUID)).thenReturn(status);

		InputStream inputStreamTo = RestDocTestFileSupport.getTestfileSupport()
				.getInputStreamTo("upload/zipfile_contains_only_test1.txt.zip");
		MockMultipartFile file1 = new MockMultipartFile("file", inputStreamTo);
		/* execute + test @formatter:off */
        this.mockMvc.perform(
        		fileUpload(https(PORT_USED).buildUploadSourceCodeUrl(PROJECT_ID.pathElement(), JOB_UUID.pathElement()) ,PROJECT1_ID,randomUUID).
        			file(file1).param("checkSum", "mychecksum")
        		).
        			andExpect(status().isOk()).
        					// https://docs.spring.io/spring-restdocs/docs/2.0.2.RELEASE/reference/html5/
        					andDo(document(RestDocPathFactory.createPath(UseCaseUserUploadsSourceCode.class),
        							requestParts(partWithName("file").description("The sourcecode as zipfile to upload")),
        							requestParameters(
        									parameterWithName("checkSum").description("A sha256 checksum for file upload validation")),
        							pathParameters(
        										parameterWithName("projectId").description("The id of the project where sourcecode shall be uploaded for"),
        										parameterWithName("jobUUID").description("The jobUUID for sechub job")
        									)
        			)
        		);

        /* @formatter:on */
	}

	@Test
	@UseCaseRestDoc(useCase = UseCaseUserApprovesJob.class)
	public void restDoc_userApprovesJob() throws Exception {
		/* prepare */

		ScheduleSecHubJob job = new ScheduleSecHubJob() {
			public UUID getUUID() {
				return randomUUID;
			};
		};
		job.setExecutionResult(ExecutionResult.OK);
		job.setStarted(LocalDateTime.now().minusMinutes(15));
		job.setEnded(LocalDateTime.now());
		job.setExecutionState(ExecutionState.ENDED);
		job.setOwner("CREATOR1");
		job.setTrafficLight(TrafficLight.GREEN);

		ScheduleJobStatus status = new ScheduleJobStatus(job);

		when(mockedScheduleJobStatusService.getJobStatus(PROJECT1_ID, randomUUID)).thenReturn(status);

		/* execute + test @formatter:off */
	    this.mockMvc.perform(
	    		put(https(PORT_USED).buildApproveJobUrl(PROJECT_ID.pathElement(), JOB_UUID.pathElement()),PROJECT1_ID,randomUUID).
	    			contentType(MediaType.APPLICATION_JSON_VALUE)
	    		).
	    			andExpect(status().isOk()).
	    					andDo(document(RestDocPathFactory.createPath(UseCaseUserApprovesJob.class),
	    							pathParameters(
	    										parameterWithName("projectId").description("The id of the project where sechub job shall be approved"),
	    										parameterWithName("jobUUID").description("The jobUUID for sechub job")
	    									)
	    			)
	    		);

	    /* @formatter:on */
	}

	@Test
	@UseCaseRestDoc(useCase = UseCaseUserChecksJobStatus.class)
	public void restDoc_userChecksJobState() throws Exception {
		/* prepare */

		ScheduleSecHubJob job = new ScheduleSecHubJob() {
			public UUID getUUID() {
				return randomUUID;
			};
		};
		job.setExecutionResult(ExecutionResult.OK);
		job.setStarted(LocalDateTime.now().minusMinutes(15));
		job.setEnded(LocalDateTime.now());
		job.setExecutionState(ExecutionState.ENDED);
		job.setOwner("CREATOR1");
		job.setTrafficLight(TrafficLight.GREEN);

		ScheduleJobStatus status = new ScheduleJobStatus(job);

		when(mockedScheduleJobStatusService.getJobStatus(PROJECT1_ID, randomUUID)).thenReturn(status);

		/* execute + test @formatter:off */
        this.mockMvc.perform(
        		get(https(PORT_USED).buildGetJobStatusUrl(PROJECT_ID.pathElement(), JOB_UUID.pathElement()),PROJECT1_ID,randomUUID).
        			contentType(MediaType.APPLICATION_JSON_VALUE)
        		).
        			andExpect(status().isOk()).
        			andExpect(content().json("{jobUUID:"+randomUUID.toString()+", result:OK, state:ENDED, trafficLight:GREEN}")).
        					andDo(document(RestDocPathFactory.createPath(UseCaseUserChecksJobStatus.class),
        							pathParameters(
        										parameterWithName("projectId").description("The id of the project where sechub job was started for"),
        										parameterWithName("jobUUID").description("The jobUUID for sechub job")
        									),

        							responseFields(
        									fieldWithPath(ScheduleJobStatus.PROPERTY_JOBUUID).description("The job uuid"),
        									fieldWithPath(ScheduleJobStatus.PROPERTY_CREATED).description("Creation timestamp of job"),
        									fieldWithPath(ScheduleJobStatus.PROPERTY_STARTED).description("Start timestamp of job execution"),
        									fieldWithPath(ScheduleJobStatus.PROPERTY_ENDED).description("End timestamp of job execution"),
        									fieldWithPath(ScheduleJobStatus.PROPERTY_OWNER).description("Owner / initiator of job"),
        									fieldWithPath(ScheduleJobStatus.PROPERTY_STATE).description("State of job"),
        									fieldWithPath(ScheduleJobStatus.PROPERTY_RESULT).description("Result of job"),
        									fieldWithPath(ScheduleJobStatus.PROPERTY_TRAFFICLIGHT).description("Trafficlight of job - but only available when job has been done. Possible states are "+StringUtils.arrayToDelimitedString(TrafficLight.values(),", "))

        									)
        							)
        		);

        /* @formatter:on */
	}

	@Before
	public void before() {
		randomUUID = UUID.randomUUID();
		project1 = mock(ScheduleAccess.class);

		ProjectAccessCompositeKey key = new ProjectAccessCompositeKey("user", PROJECT1_ID);
		when(project1.getKey()).thenReturn(key);

		when(mockedProjectRepository.findById(key)).thenReturn(Optional.of(project1));

		when(sechubConfigurationValidator.supports(SecHubConfiguration.class)).thenReturn(true);
	}

	@TestConfiguration
	@Profile(Profiles.TEST)
	@EnableAutoConfiguration
	public static class SimpleTestConfiguration extends AbstractAllowSecHubAPISecurityConfiguration {

	}
}
