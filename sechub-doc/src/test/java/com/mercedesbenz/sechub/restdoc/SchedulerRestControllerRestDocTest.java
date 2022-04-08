// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.restdoc;

import static com.mercedesbenz.sechub.commons.model.SecHubConfigurationModel.*;
import static com.mercedesbenz.sechub.commons.model.TestSecHubConfigurationBuilder.*;
import static com.mercedesbenz.sechub.restdoc.RestDocumentation.*;
import static com.mercedesbenz.sechub.test.TestURLBuilder.*;
import static com.mercedesbenz.sechub.test.TestURLBuilder.RestDocPathParameter.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
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

import com.mercedesbenz.sechub.commons.model.SecHubCodeScanConfiguration;
import com.mercedesbenz.sechub.commons.model.SecHubDataConfigurationUsageByName;
import com.mercedesbenz.sechub.commons.model.SecHubFileSystemConfiguration;
import com.mercedesbenz.sechub.commons.model.SecHubInfrastructureScanConfiguration;
import com.mercedesbenz.sechub.commons.model.SecHubTimeUnit;
import com.mercedesbenz.sechub.commons.model.SecHubWebScanConfiguration;
import com.mercedesbenz.sechub.commons.model.TrafficLight;
import com.mercedesbenz.sechub.commons.model.WebScanDurationConfiguration;
import com.mercedesbenz.sechub.commons.model.login.ActionType;
import com.mercedesbenz.sechub.commons.model.login.FormLoginConfiguration;
import com.mercedesbenz.sechub.commons.model.login.WebLoginConfiguration;
import com.mercedesbenz.sechub.docgen.util.RestDocFactory;
import com.mercedesbenz.sechub.docgen.util.RestDocTestFileSupport;
import com.mercedesbenz.sechub.domain.schedule.ExecutionResult;
import com.mercedesbenz.sechub.domain.schedule.ExecutionState;
import com.mercedesbenz.sechub.domain.schedule.ScheduleJobStatus;
import com.mercedesbenz.sechub.domain.schedule.SchedulerApproveJobService;
import com.mercedesbenz.sechub.domain.schedule.SchedulerCreateJobService;
import com.mercedesbenz.sechub.domain.schedule.SchedulerGetJobStatusService;
import com.mercedesbenz.sechub.domain.schedule.SchedulerRestController;
import com.mercedesbenz.sechub.domain.schedule.SchedulerResult;
import com.mercedesbenz.sechub.domain.schedule.SchedulerUploadService;
import com.mercedesbenz.sechub.domain.schedule.access.ScheduleAccess;
import com.mercedesbenz.sechub.domain.schedule.access.ScheduleAccess.ProjectAccessCompositeKey;
import com.mercedesbenz.sechub.domain.schedule.access.ScheduleAccessRepository;
import com.mercedesbenz.sechub.domain.schedule.job.ScheduleSecHubJob;
import com.mercedesbenz.sechub.domain.schedule.job.SecHubJobRepository;
import com.mercedesbenz.sechub.sharedkernel.Profiles;
import com.mercedesbenz.sechub.sharedkernel.configuration.AbstractAllowSecHubAPISecurityConfiguration;
import com.mercedesbenz.sechub.sharedkernel.configuration.SecHubConfiguration;
import com.mercedesbenz.sechub.sharedkernel.configuration.SecHubConfigurationValidator;
import com.mercedesbenz.sechub.sharedkernel.usecases.UseCaseRestDoc;
import com.mercedesbenz.sechub.sharedkernel.usecases.user.execute.UseCaseUserApprovesJob;
import com.mercedesbenz.sechub.sharedkernel.usecases.user.execute.UseCaseUserChecksJobStatus;
import com.mercedesbenz.sechub.sharedkernel.usecases.user.execute.UseCaseUserCreatesNewJob;
import com.mercedesbenz.sechub.sharedkernel.usecases.user.execute.UseCaseUserUploadsSourceCode;
import com.mercedesbenz.sechub.test.ExampleConstants;
import com.mercedesbenz.sechub.test.TestPortProvider;

@RunWith(SpringRunner.class)
@WebMvcTest(SchedulerRestController.class)
@ContextConfiguration(classes = { SchedulerRestController.class, SchedulerRestControllerRestDocTest.SimpleTestConfiguration.class })
@WithMockUser
@ActiveProfiles(Profiles.TEST)
@AutoConfigureRestDocs(uriScheme = "https", uriHost = ExampleConstants.URI_SECHUB_SERVER, uriPort = 443)
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
        String apiEndpoint = https(PORT_USED).buildAddJobUrl(PROJECT_ID.pathElement());
        Class<? extends Annotation> useCase = UseCaseUserCreatesNewJob.class;

        UUID randomUUID = UUID.randomUUID();
        SchedulerResult mockResult = new SchedulerResult(randomUUID);

        when(mockedScheduleCreateJobService.createJob(any(), any(SecHubConfiguration.class))).thenReturn(mockResult);

        /* execute + test @formatter:off */
	    this.mockMvc.perform(
	    		post(apiEndpoint,PROJECT1_ID).
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
	    			andDo(print()).
	    			andDo(defineRestService().
	    	                with().
	    	                    useCaseData(useCase, "Code Scan").
	    	                    tag(RestDocFactory.extractTag(apiEndpoint)).
	    	                    requestSchema(OpenApiSchema.SCAN_JOB.getSchema()).
	    	                    responseSchema(OpenApiSchema.JOB_ID.getSchema()).
	    	                and().
	    	                document(
	    			                            pathParameters(
	    			                                    parameterWithName(PROJECT_ID.paramName()).description("The unique id of the project id where a new sechub job shall be created")
	    			                            ),
	    			                            requestFields(
	    			                                    fieldWithPath(PROPERTY_API_VERSION).description("The api version, currently only 1.0 is supported"),
	    			                                    fieldWithPath(PROPERTY_CODE_SCAN).description("Code scan configuration block").optional(),
	    			                                    fieldWithPath(PROPERTY_CODE_SCAN+"."+SecHubDataConfigurationUsageByName.PROPERTY_USE).description("Referenced data configuration objects by their unique names").optional(),
	    			                                    fieldWithPath(PROPERTY_CODE_SCAN+"."+SecHubCodeScanConfiguration.PROPERTY_FILESYSTEM+"."+SecHubFileSystemConfiguration.PROPERTY_FOLDERS).description("Code scan sources from given file system folders").optional(),
	    			                                    fieldWithPath(PROPERTY_CODE_SCAN+"."+SecHubCodeScanConfiguration.PROPERTY_FILESYSTEM+"."+SecHubFileSystemConfiguration.PROPERTY_FILES).description("Code scan sources from given file system files").optional()

	    			                            ),
	    			                            responseFields(
	    			                                    fieldWithPath(SchedulerResult.PROPERTY_JOBID).description("A unique job id")
	    			                            )
   			                ));

	    /* @formatter:on */
    }

    @Test
    @UseCaseRestDoc(useCase = UseCaseUserCreatesNewJob.class, variant = "Infrastructure scan")
    public void restDoc_userCreatesNewJob_infrascan() throws Exception {
        /* prepare */
        String apiEndpoint = https(PORT_USED).buildAddJobUrl(PROJECT_ID.pathElement());
        Class<? extends Annotation> useCase = UseCaseUserCreatesNewJob.class;

        UUID randomUUID = UUID.randomUUID();
        SchedulerResult mockResult = new SchedulerResult(randomUUID);

        when(mockedScheduleCreateJobService.createJob(any(), any(SecHubConfiguration.class))).thenReturn(mockResult);

        /* execute + test @formatter:off */
	    this.mockMvc.perform(
	    		post(apiEndpoint,PROJECT1_ID).
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
	    			andDo(defineRestService().
	    	                with().
	    	                    useCaseData(useCase, "Infrastructure scan").
	    	                    tag(RestDocFactory.extractTag(apiEndpoint)).
	    	                    requestSchema(OpenApiSchema.SCAN_JOB.getSchema()).
	    	                    responseSchema(OpenApiSchema.JOB_ID.getSchema()).
	    	                and().
	    	                document(
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
                                                fieldWithPath(SchedulerResult.PROPERTY_JOBID).description("A unique job id")

                                    )
                            ));

	    /* @formatter:on */
    }

    @Test
    @UseCaseRestDoc(useCase = UseCaseUserCreatesNewJob.class, variant = "Web scan anonymous")
    public void restDoc_userCreatesNewJob_webscan_anonymous() throws Exception {
        /* prepare */
        String apiEndpoint = https(PORT_USED).buildAddJobUrl(PROJECT_ID.pathElement());
        Class<? extends Annotation> useCase = UseCaseUserCreatesNewJob.class;

        UUID randomUUID = UUID.randomUUID();
        SchedulerResult mockResult = new SchedulerResult(randomUUID);

        WebScanDurationConfiguration maxScanDuration = new WebScanDurationConfiguration();
        maxScanDuration.setDuration(1);
        maxScanDuration.setUnit(SecHubTimeUnit.HOUR);

        List<String> includes = Arrays.asList("/admin", "/hidden", "/admin.html");
        List<String> excludes = Arrays.asList("/public/media", "/static", "/contaxt.html");

        when(mockedScheduleCreateJobService.createJob(any(), any(SecHubConfiguration.class))).thenReturn(mockResult);

        /* execute + test @formatter:off */
	    this.mockMvc.perform(
	    		post(apiEndpoint,PROJECT1_ID).
	    			contentType(MediaType.APPLICATION_JSON_VALUE).
	    			content(configureSecHub().
	    					api("1.0").
	    					webConfig().
	    						addURI("https://localhost/mywebapp/login").
	    						maxScanDuration(maxScanDuration).
	    						addIncludes(includes).
	    						addExcludes(excludes).
	    					build().
	    					toJSON())
	    		).
	    			andExpect(status().isOk()).
	    			andExpect(content().json("{jobId:"+randomUUID.toString()+"}")).
	    			andDo(defineRestService().
                            with().
                                useCaseData(useCase, "Web Scan anonymous").
                                tag(RestDocFactory.extractTag(apiEndpoint)).
                                requestSchema(OpenApiSchema.SCAN_JOB.getSchema()).
                                responseSchema(OpenApiSchema.JOB_ID.getSchema()).
                            and().
                            document(
                                        pathParameters(
                                                parameterWithName(PROJECT_ID.paramName()).description("The unique id of the project id where a new sechub job shall be created")
                                        ),
                                        requestFields(
                                                fieldWithPath(PROPERTY_API_VERSION).description("The api version, currently only 1.0 is supported"),
                                                fieldWithPath(PROPERTY_WEB_SCAN).description("Webscan configuration block").optional(),
                                                fieldWithPath(PROPERTY_WEB_SCAN+"."+SecHubWebScanConfiguration.PROPERTY_URI).description("Webscan URI to scan for").optional(),
                                                fieldWithPath(PROPERTY_WEB_SCAN+"."+SecHubWebScanConfiguration.PROPERTY_MAX_SCAN_DURATION+"."+WebScanDurationConfiguration.PROPERTY_DURATION).description("Duration of the scan as integer").optional(),
                                                fieldWithPath(PROPERTY_WEB_SCAN+"."+SecHubWebScanConfiguration.PROPERTY_MAX_SCAN_DURATION+"."+WebScanDurationConfiguration.PROPERTY_UNIT).description("Unit of the duration. Possible values are: millisecond(s), second(s), minute(s), hour(s), day(s)").optional(),
                                                fieldWithPath(PROPERTY_WEB_SCAN+"."+SecHubWebScanConfiguration.PROPERTY_INCLUDES+"[]").description("Include URL sub-paths to scan. Example: /hidden").optional(),
                                                fieldWithPath(PROPERTY_WEB_SCAN+"."+SecHubWebScanConfiguration.PROPERTY_EXCLUDES+"[]").description("Exclude URL sub-paths to scan. Example: /admin").optional()

                                        ),
                                        responseFields(
                                                fieldWithPath(SchedulerResult.PROPERTY_JOBID).description("A unique job id")
                                        )
	    			      ));

	    /* @formatter:on */
    }

    @Test
    @UseCaseRestDoc(useCase = UseCaseUserCreatesNewJob.class, variant = "Web Scan login basic")
    public void restDoc_userCreatesNewJob_webscan_login_basic() throws Exception {
        /* prepare */
        String apiEndpoint = https(PORT_USED).buildAddJobUrl(PROJECT_ID.pathElement());
        Class<? extends Annotation> useCase = UseCaseUserCreatesNewJob.class;

        UUID randomUUID = UUID.randomUUID();
        SchedulerResult mockResult = new SchedulerResult(randomUUID);

        when(mockedScheduleCreateJobService.createJob(any(), any(SecHubConfiguration.class))).thenReturn(mockResult);

        /* execute + test @formatter:off */
	    this.mockMvc.perform(
	    		post(apiEndpoint, PROJECT1_ID).
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
	    			andDo(defineRestService().
                            with().
                                useCaseData(useCase, "Web Scan login basic").
                                tag(RestDocFactory.extractTag(apiEndpoint)).
                                requestSchema(OpenApiSchema.SCAN_JOB.getSchema()).
                                responseSchema(OpenApiSchema.JOB_ID.getSchema()).
                            and().
                            document(
                                        pathParameters(
                                                parameterWithName(PROJECT_ID.paramName()).description("The unique id of the project id where a new sechub job shall be created")
                                        ),
                                        requestFields(
                                                fieldWithPath(PROPERTY_API_VERSION).description("The api version, currently only 1.0 is supported"),
                                                fieldWithPath(PROPERTY_WEB_SCAN).description("Webscan configuration block").optional(),
                                                fieldWithPath(PROPERTY_WEB_SCAN+"."+SecHubWebScanConfiguration.PROPERTY_URI).description("Webscan URI to scan for").optional(),
                                                fieldWithPath(PROPERTY_WEB_SCAN+"."+SecHubWebScanConfiguration.PROPERTY_LOGIN).description("Webscan login definition").optional(),
                                                fieldWithPath(PROPERTY_WEB_SCAN+"."+SecHubWebScanConfiguration.PROPERTY_LOGIN+".url").description("Login URL").optional(),
                                                fieldWithPath(PROPERTY_WEB_SCAN+"."+SecHubWebScanConfiguration.PROPERTY_LOGIN+"."+WebLoginConfiguration.PROPERTY_BASIC).description("basic login definition").optional(),
                                                fieldWithPath(PROPERTY_WEB_SCAN+"."+SecHubWebScanConfiguration.PROPERTY_LOGIN+"."+WebLoginConfiguration.PROPERTY_BASIC+".user").description("username").optional(),
                                                fieldWithPath(PROPERTY_WEB_SCAN+"."+SecHubWebScanConfiguration.PROPERTY_LOGIN+"."+WebLoginConfiguration.PROPERTY_BASIC+".password").description("password").optional()

                                        ),
                                        responseFields(
                                                fieldWithPath(SchedulerResult.PROPERTY_JOBID).description("A unique job id")
                                        )
	    			    ));

	    /* @formatter:on */
    }

    @Test
    @UseCaseRestDoc(useCase = UseCaseUserCreatesNewJob.class, variant = "Web Scan login form scripted")
    public void restDoc_userCreatesNewJob_webScan_login_form_script() throws Exception {
        /* prepare */
        String apiEndpoint = https(PORT_USED).buildAddJobUrl(PROJECT_ID.pathElement());
        Class<? extends Annotation> useCase = UseCaseUserCreatesNewJob.class;

        UUID randomUUID = UUID.randomUUID();
        SchedulerResult mockResult = new SchedulerResult(randomUUID);

        when(mockedScheduleCreateJobService.createJob(any(), any(SecHubConfiguration.class))).thenReturn(mockResult);

        /* execute + test @formatter:off */
	    this.mockMvc.perform(
	    		post(apiEndpoint,PROJECT1_ID).
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
	    		andDo(defineRestService().
                        with().
                            useCaseData(useCase, "Web Scan login form scripted").
                            tag(RestDocFactory.extractTag(apiEndpoint)).
                            requestSchema(OpenApiSchema.SCAN_JOB.getSchema()).
                            responseSchema(OpenApiSchema.JOB_ID.getSchema()).
                        and().
                        document(
                                        pathParameters(
                                                parameterWithName(PROJECT_ID.paramName()).description("The unique id of the project id where a new sechub job shall be created")
                                        ),
                                        requestFields(
                                            fieldWithPath(PROPERTY_API_VERSION).description("The api version, currently only 1.0 is supported"),
    										fieldWithPath(PROPERTY_WEB_SCAN).description("Webscan configuration block").optional(),
    										fieldWithPath(PROPERTY_WEB_SCAN+"."+SecHubWebScanConfiguration.PROPERTY_URI).description("Webscan URI to scan for").optional(),
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
                                                fieldWithPath(SchedulerResult.PROPERTY_JOBID).description("A unique job id")
                                        )
	    		    ));
	    /* @formatter:on */
    }

    @Test
    @UseCaseRestDoc(useCase = UseCaseUserUploadsSourceCode.class)
    public void restDoc_userUploadsSourceCode() throws Exception {
        /* prepare */
        String apiEndpoint = https(PORT_USED).buildUploadSourceCodeUrl(PROJECT_ID.pathElement(), JOB_UUID.pathElement());
        Class<? extends Annotation> useCase = UseCaseUserUploadsSourceCode.class;

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

        InputStream inputStreamTo = RestDocTestFileSupport.getTestfileSupport().getInputStreamTo("upload/zipfile_contains_only_test1.txt.zip");
        MockMultipartFile file1 = new MockMultipartFile("file", inputStreamTo);
        /* execute + test @formatter:off */
        this.mockMvc.perform(
        		fileUpload(apiEndpoint, PROJECT1_ID,randomUUID).
        			file(file1).param("checkSum", "mychecksum")
        		).
        			andExpect(status().isOk()).
        					// https://docs.spring.io/spring-restdocs/docs/2.0.2.RELEASE/reference/html5/
        			andDo(defineRestService().
                            with().
                                useCaseData(useCase).
                                tag(RestDocFactory.extractTag(apiEndpoint)).
                            and().
                            document(
                                    pathParameters(
                                            parameterWithName("projectId").description("The id of the project where sourcecode shall be uploaded for"),
                                            parameterWithName("jobUUID").description("The jobUUID for sechub job")
                                    ),
                                    requestParameters(
                                            parameterWithName("checkSum").description("A sha256 checksum for file upload validation")
                                    ),
                                    // TODO jeeppler, 2020-12-07: It is not possible to document this part properly in OpenAPI.
                                    // See: https://github.com/ePages-de/restdocs-api-spec/issues/105
                					requestParts(partWithName("file").description("The sourcecode as zipfile to upload"))
        			));

        /* @formatter:on */
    }

    @Test
    @UseCaseRestDoc(useCase = UseCaseUserApprovesJob.class)
    public void restDoc_userApprovesJob() throws Exception {
        /* prepare */
        String apiEndpoint = https(PORT_USED).buildApproveJobUrl(PROJECT_ID.pathElement(), JOB_UUID.pathElement());
        Class<? extends Annotation> useCase = UseCaseUserApprovesJob.class;

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
	    		put(apiEndpoint, PROJECT1_ID,randomUUID).
	    			contentType(MediaType.APPLICATION_JSON_VALUE)
	    		).
	    			andExpect(status().isOk()).
	    			andDo(defineRestService().
                            with().
                                useCaseData(useCase).
                                tag(RestDocFactory.extractTag(apiEndpoint)).
                            and().
                            document(
	                                        pathParameters(
	                                                    parameterWithName("projectId").description("The id of the project where sechub job shall be approved"),
	                                                    parameterWithName("jobUUID").description("The jobUUID for sechub job")
	                                        )
	    				));

	    /* @formatter:on */
    }

    @Test
    @UseCaseRestDoc(useCase = UseCaseUserChecksJobStatus.class)
    public void restDoc_userChecksJobState() throws Exception {
        /* prepare */
        String apiEndpoint = https(PORT_USED).buildGetJobStatusUrl(PROJECT_ID.pathElement(), JOB_UUID.pathElement());
        Class<? extends Annotation> useCase = UseCaseUserChecksJobStatus.class;

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
        		get(apiEndpoint, PROJECT1_ID,randomUUID).
        			contentType(MediaType.APPLICATION_JSON_VALUE)
        		).
        			andExpect(status().isOk()).
        			andExpect(content().json("{jobUUID:"+randomUUID.toString()+", result:OK, state:ENDED, trafficLight:GREEN}")).
        			andDo(defineRestService().
                            with().
                                useCaseData(useCase).
                                tag(RestDocFactory.extractTag(apiEndpoint)).
                                responseSchema(OpenApiSchema.JOB_STATUS.getSchema()).
                            and().
                            document(
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
