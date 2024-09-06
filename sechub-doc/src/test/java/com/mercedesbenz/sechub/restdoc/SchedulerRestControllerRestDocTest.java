// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.restdoc;

import static com.mercedesbenz.sechub.commons.core.CommonConstants.*;
import static com.mercedesbenz.sechub.commons.model.SecHubConfigurationModel.*;
import static com.mercedesbenz.sechub.commons.model.TestSecHubConfigurationBuilder.*;
import static com.mercedesbenz.sechub.restdoc.RestDocumentation.*;
import static com.mercedesbenz.sechub.test.RestDocPathParameter.*;
import static com.mercedesbenz.sechub.test.SecHubTestURLBuilder.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.*;

import com.mercedesbenz.sechub.commons.model.ClientCertificateConfiguration;
import com.mercedesbenz.sechub.commons.model.HTTPHeaderConfiguration;
import com.mercedesbenz.sechub.commons.model.SecHubCodeScanConfiguration;
import com.mercedesbenz.sechub.commons.model.SecHubConfigurationMetaData;
import com.mercedesbenz.sechub.commons.model.SecHubDataConfiguration;
import com.mercedesbenz.sechub.commons.model.SecHubDataConfigurationUsageByName;
import com.mercedesbenz.sechub.commons.model.SecHubFileSystemConfiguration;
import com.mercedesbenz.sechub.commons.model.SecHubInfrastructureScanConfiguration;
import com.mercedesbenz.sechub.commons.model.SecHubSourceDataConfiguration;
import com.mercedesbenz.sechub.commons.model.SecHubTimeUnit;
import com.mercedesbenz.sechub.commons.model.SecHubWebScanApiConfiguration;
import com.mercedesbenz.sechub.commons.model.SecHubWebScanApiType;
import com.mercedesbenz.sechub.commons.model.SecHubWebScanConfiguration;
import com.mercedesbenz.sechub.commons.model.TrafficLight;
import com.mercedesbenz.sechub.commons.model.WebScanDurationConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.StringUtils;

import com.mercedesbenz.sechub.commons.core.CommonConstants;
import com.mercedesbenz.sechub.commons.model.job.ExecutionResult;
import com.mercedesbenz.sechub.commons.model.job.ExecutionState;
import com.mercedesbenz.sechub.commons.model.login.ActionType;
import com.mercedesbenz.sechub.commons.model.login.FormLoginConfiguration;
import com.mercedesbenz.sechub.commons.model.login.WebLoginConfiguration;
import com.mercedesbenz.sechub.docgen.util.RestDocFactory;
import com.mercedesbenz.sechub.docgen.util.RestDocTestFileSupport;
import com.mercedesbenz.sechub.domain.schedule.ScheduleJobStatus;
import com.mercedesbenz.sechub.domain.schedule.SchedulerApproveJobService;
import com.mercedesbenz.sechub.domain.schedule.SchedulerBinariesUploadService;
import com.mercedesbenz.sechub.domain.schedule.SchedulerCreateJobService;
import com.mercedesbenz.sechub.domain.schedule.SchedulerGetJobStatusService;
import com.mercedesbenz.sechub.domain.schedule.SchedulerRestController;
import com.mercedesbenz.sechub.domain.schedule.SchedulerResult;
import com.mercedesbenz.sechub.domain.schedule.SchedulerSourcecodeUploadService;
import com.mercedesbenz.sechub.domain.schedule.access.ScheduleAccess;
import com.mercedesbenz.sechub.domain.schedule.access.ScheduleAccess.ProjectAccessCompositeKey;
import com.mercedesbenz.sechub.domain.schedule.access.ScheduleAccessRepository;
import com.mercedesbenz.sechub.domain.schedule.job.ScheduleSecHubJob;
import com.mercedesbenz.sechub.domain.schedule.job.SecHubJobInfoForUser;
import com.mercedesbenz.sechub.domain.schedule.job.SecHubJobInfoForUserListPage;
import com.mercedesbenz.sechub.domain.schedule.job.SecHubJobInfoForUserService;
import com.mercedesbenz.sechub.domain.schedule.job.SecHubJobRepository;
import com.mercedesbenz.sechub.sharedkernel.Profiles;
import com.mercedesbenz.sechub.sharedkernel.configuration.AbstractSecHubAPISecurityConfiguration;
import com.mercedesbenz.sechub.sharedkernel.configuration.SecHubConfiguration;
import com.mercedesbenz.sechub.sharedkernel.configuration.SecHubConfigurationValidator;
import com.mercedesbenz.sechub.sharedkernel.usecases.UseCaseRestDoc;
import com.mercedesbenz.sechub.sharedkernel.usecases.job.UseCaseUserListsJobsForProject;
import com.mercedesbenz.sechub.sharedkernel.usecases.user.execute.UseCaseUserApprovesJob;
import com.mercedesbenz.sechub.sharedkernel.usecases.user.execute.UseCaseUserChecksJobStatus;
import com.mercedesbenz.sechub.sharedkernel.usecases.user.execute.UseCaseUserCreatesNewJob;
import com.mercedesbenz.sechub.sharedkernel.usecases.user.execute.UseCaseUserUploadsBinaries;
import com.mercedesbenz.sechub.sharedkernel.usecases.user.execute.UseCaseUserUploadsSourceCode;
import com.mercedesbenz.sechub.test.ExampleConstants;
import com.mercedesbenz.sechub.test.TestIsNecessaryForDocumentation;
import com.mercedesbenz.sechub.test.TestPortProvider;

@WebMvcTest(SchedulerRestController.class)
@ContextConfiguration(classes = { SchedulerRestController.class, SchedulerRestControllerRestDocTest.SimpleTestConfiguration.class })
@WithMockUser
@ExtendWith(RestDocumentationExtension.class)
@ActiveProfiles(Profiles.TEST)
@AutoConfigureRestDocs(uriScheme = "https", uriHost = ExampleConstants.URI_SECHUB_SERVER, uriPort = 443)
public class SchedulerRestControllerRestDocTest implements TestIsNecessaryForDocumentation {

    private static final String PROJECT1_ID = "project1";

    private static final String VARIANT_CODE_SCAN = "Code Scan";
    private static final String VARIANT_CODES_SCAN_WITH_FULL_DATA_SECTION = "Code Scan using data section";

    private static final String VARIANT_WEB_SCAN_HEADERS = "Web Scan headers";
    private static final String VARIANT_WEB_SCAN_LOGIN_FORM_SCRIPTED = "Web Scan login form scripted";
    private static final String VARIANT_WEB_SCAN_LOGIN_BASIC = "Web Scan login basic";
    private static final String VARIANT_WEB_SCAN_WITH_CLIENT_CERTIFICATE_DEFINITION = "Web scan with client certificate definition";
    private static final String VARIANT_WEB_SCAN_WITH_API_DEFINITION = "Web scan with api definition";
    private static final String VARIANT_WEB_SCAN_ANONYMOUS = "Web scan anonymous";

    private static final String VARIANT_INFRASTRUCTURE_SCAN = "Infrastructure scan";
    private static final String VARIANT_SECRET_SCAN = "Secret scan";
    private static final String VARIANT_LICENSE_SCAN = "License scan";

    private static final int PORT_USED = TestPortProvider.DEFAULT_INSTANCE.getRestDocTestPort();

    private static final String FORM = WebLoginConfiguration.PROPERTY_FORM;
    private static final String SCRIPT = FormLoginConfiguration.PROPERTY_SCRIPT;

    private static final String DESCRIPTION_JOB_UUID = "The SecHub jobUUID. During the job creation this unique job identifier is automatically generated by SecHub.";

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
    private SchedulerSourcecodeUploadService mockedSourcecodeUploadService;

    @MockBean
    private SchedulerBinariesUploadService mockedBinariesUploadService;

    @MockBean
    private SecHubJobRepository mockedJobRepository;

    @MockBean
    private ScheduleAccessRepository mockedProjectRepository;

    @MockBean
    private SecHubJobInfoForUserService mockedJobInfoForUserService;

    private ScheduleAccess project1;

    private UUID randomUUID;

    @Test
    @UseCaseRestDoc(useCase = UseCaseUserCreatesNewJob.class, variant = VARIANT_CODE_SCAN)
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
	    	                    useCaseData(useCase, VARIANT_CODE_SCAN).
	    	                    tag(RestDocFactory.extractTag(apiEndpoint)).
	    	                    requestSchema(OpenApiSchema.SCAN_JOB.getSchema()).
	    	                    responseSchema(OpenApiSchema.JOB_ID.getSchema()).
	    	                and().
	    	                document(
				    	                		requestHeaders(

				    	                		),
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
    @UseCaseRestDoc(useCase = UseCaseUserCreatesNewJob.class, variant = VARIANT_CODES_SCAN_WITH_FULL_DATA_SECTION)
    public void restDoc_userCreatesNewJob_codescan_with_data_section() throws Exception {
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
                                useDataReferences("source-ref-name","bin-ref-name").
                            and().
                            data().
                                withSource().
                                    uniqueName("source-ref-name").
                                    fileSystemFolders("testproject1/src/main/java","testproject2/src/main/java").
                                    fileSystemFiles("testproject1/src/other/example/php-example.php").
                                end().
                                withBinary().
                                    uniqueName("bin-ref-name").
                                    fileSystemFolders("testproject1/build/kotlin").
                                    fileSystemFolders("testproject1/build/kotlin").
                                    fileSystemFiles("testproject1/build/other/native.dll").
                                end().
                            and().
                            build().
                            toJSON())
                ).
                    andExpect(status().isOk()).
                    andExpect(content().json("{jobId:"+randomUUID.toString()+"}")).
                    andDo(print()).
                    andDo(defineRestService().
                            with().
                                useCaseData(useCase, VARIANT_CODES_SCAN_WITH_FULL_DATA_SECTION).
                                tag(RestDocFactory.extractTag(apiEndpoint)).
                                requestSchema(OpenApiSchema.SCAN_JOB.getSchema()).
                                responseSchema(OpenApiSchema.JOB_ID.getSchema()).
                            and().
                            document(
                                                requestHeaders(

                                                ),
                                                pathParameters(
                                                        parameterWithName(PROJECT_ID.paramName()).description("The unique id of the project id where a new sechub job shall be created")
                                                ),
                                                requestFields(
                                                        fieldWithPath(PROPERTY_API_VERSION).description("The api version, currently only 1.0 is supported"),
                                                        fieldWithPath(PROPERTY_CODE_SCAN).description("Code scan configuration block").optional(),
                                                        fieldWithPath(PROPERTY_CODE_SCAN+"."+SecHubDataConfigurationUsageByName.PROPERTY_USE).description("Referenced data configuration objects by their unique names").optional(),

                                                        fieldWithPath(PROPERTY_DATA+"."+SecHubDataConfiguration.PROPERTY_SOURCES +"[]."+SecHubSourceDataConfiguration.PROPERTY_UNIQUENAME_AS_NAME).description("Unique reference name").optional(),
                                                        fieldWithPath(PROPERTY_DATA+"."+SecHubDataConfiguration.PROPERTY_SOURCES +"[]."+SecHubSourceDataConfiguration.PROPERTY_FILESYSTEM+"."+SecHubFileSystemConfiguration.PROPERTY_FOLDERS+"[]").description("Sources from given file system folders").optional(),
                                                        fieldWithPath(PROPERTY_DATA+"."+SecHubDataConfiguration.PROPERTY_SOURCES +"[]."+SecHubSourceDataConfiguration.PROPERTY_FILESYSTEM+"."+SecHubFileSystemConfiguration.PROPERTY_FILES+"[]").description("Sources from given file system files").optional(),

                                                        fieldWithPath(PROPERTY_DATA+"."+SecHubDataConfiguration.PROPERTY_BINARIES+"[]."+SecHubSourceDataConfiguration.PROPERTY_UNIQUENAME_AS_NAME).description("Unique reference name").optional(),
                                                        fieldWithPath(PROPERTY_DATA+"."+SecHubDataConfiguration.PROPERTY_BINARIES+"[]."+SecHubSourceDataConfiguration.PROPERTY_FILESYSTEM+"."+SecHubFileSystemConfiguration.PROPERTY_FOLDERS+"[]").description("Binaries from given file system folders").optional(),
                                                        fieldWithPath(PROPERTY_DATA+"."+SecHubDataConfiguration.PROPERTY_BINARIES+"[]."+SecHubSourceDataConfiguration.PROPERTY_FILESYSTEM+"."+SecHubFileSystemConfiguration.PROPERTY_FILES+"[]").description("Binaries from given file system files").optional()


                                                ),
                                                responseFields(
                                                        fieldWithPath(SchedulerResult.PROPERTY_JOBID).description("A unique job id")
                                                )
                            ));

        /* @formatter:on */
    }

    @Test
    @UseCaseRestDoc(useCase = UseCaseUserCreatesNewJob.class, variant = VARIANT_SECRET_SCAN)
    public void restDoc_userCreatesNewJob_secretscan_with_data_section() throws Exception {
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
                            secretScanConfig().
                                useDataReferences("source-ref-name","bin-ref-name").
                            and().
                            data().
                                withSource().
                                    uniqueName("source-ref-name").
                                end().
                                withBinary().
                                    uniqueName("bin-ref-name").
                                end().
                            and().
                            build().
                            toJSON())
                ).
                    andExpect(status().isOk()).
                    andExpect(content().json("{jobId:"+randomUUID.toString()+"}")).
                    andDo(print()).
                    andDo(defineRestService().
                            with().
                                useCaseData(useCase, VARIANT_SECRET_SCAN).
                                tag(RestDocFactory.extractTag(apiEndpoint)).
                                requestSchema(OpenApiSchema.SCAN_JOB.getSchema()).
                                responseSchema(OpenApiSchema.JOB_ID.getSchema()).
                            and().
                            document(
                                                requestHeaders(

                                                ),
                                                pathParameters(
                                                        parameterWithName(PROJECT_ID.paramName()).description("The unique id of the project id where a new sechub job shall be created")
                                                ),
                                                requestFields(
                                                        fieldWithPath(PROPERTY_API_VERSION).description("The api version, currently only 1.0 is supported"),
                                                        fieldWithPath(PROPERTY_SECRET_SCAN).description("Secret scan configuration block").optional(),
                                                        fieldWithPath(PROPERTY_SECRET_SCAN+"."+SecHubDataConfigurationUsageByName.PROPERTY_USE).description("Referenced data configuration objects by their unique names").optional(),

                                                        fieldWithPath(PROPERTY_DATA+"."+SecHubDataConfiguration.PROPERTY_SOURCES +"[]."+SecHubSourceDataConfiguration.PROPERTY_UNIQUENAME_AS_NAME).description("Unique reference name").optional(),

                                                        fieldWithPath(PROPERTY_DATA+"."+SecHubDataConfiguration.PROPERTY_BINARIES+"[]."+SecHubSourceDataConfiguration.PROPERTY_UNIQUENAME_AS_NAME).description("Unique reference name").optional()


                                                ),
                                                responseFields(
                                                        fieldWithPath(SchedulerResult.PROPERTY_JOBID).description("A unique job id")
                                                )
                            ));

        /* @formatter:on */
    }

    @Test
    @UseCaseRestDoc(useCase = UseCaseUserCreatesNewJob.class, variant = VARIANT_LICENSE_SCAN)
    public void restDoc_userCreatesNewJob_licensescan_with_data_section() throws Exception {
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
                        licenseScanConfig().
                            useDataReferences("source-ref-name","bin-ref-name").
                        and().
                            data().
                                withSource().
                                    uniqueName("source-ref-name").
                                end().
                                withBinary().
                                    uniqueName("bin-ref-name").
                                end().
                            and().
                        build().
                        toJSON())
                ).
        andExpect(status().isOk()).
        andExpect(content().json("{jobId:"+randomUUID.toString()+"}")).
        andDo(print()).
        andDo(defineRestService().
                with().
                useCaseData(useCase, VARIANT_LICENSE_SCAN).
                tag(RestDocFactory.extractTag(apiEndpoint)).
                requestSchema(OpenApiSchema.SCAN_JOB.getSchema()).
                responseSchema(OpenApiSchema.JOB_ID.getSchema()).
                and().
                document(
                        requestHeaders(

                                ),
                        pathParameters(
                                parameterWithName(PROJECT_ID.paramName()).description("The unique id of the project id where a new sechub job shall be created")
                                ),
                        requestFields(
                                fieldWithPath(PROPERTY_API_VERSION).description("The api version, currently only 1.0 is supported"),
                                fieldWithPath(PROPERTY_LICENSE_SCAN).description("License scan configuration block").optional(),
                                fieldWithPath(PROPERTY_LICENSE_SCAN+"."+SecHubDataConfigurationUsageByName.PROPERTY_USE).description("Referenced data configuration objects by their unique names").optional(),

                                fieldWithPath(PROPERTY_DATA+"."+SecHubDataConfiguration.PROPERTY_SOURCES +"[]."+SecHubSourceDataConfiguration.PROPERTY_UNIQUENAME_AS_NAME).description("Unique reference name").optional(),

                                fieldWithPath(PROPERTY_DATA+"."+SecHubDataConfiguration.PROPERTY_BINARIES+"[]."+SecHubSourceDataConfiguration.PROPERTY_UNIQUENAME_AS_NAME).description("Unique reference name").optional()


                                ),
                        responseFields(
                                fieldWithPath(SchedulerResult.PROPERTY_JOBID).description("A unique job id")
                                )
                        ));

        /* @formatter:on */
    }

    @Test
    @UseCaseRestDoc(useCase = UseCaseUserCreatesNewJob.class, variant = VARIANT_INFRASTRUCTURE_SCAN)
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
	    	                    useCaseData(useCase, VARIANT_INFRASTRUCTURE_SCAN).
	    	                    tag(RestDocFactory.extractTag(apiEndpoint)).
	    	                    requestSchema(OpenApiSchema.SCAN_JOB.getSchema()).
	    	                    responseSchema(OpenApiSchema.JOB_ID.getSchema()).
	    	                and().
	    	                document(
		    	                		requestHeaders(

		    	                		),
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
    @UseCaseRestDoc(useCase = UseCaseUserCreatesNewJob.class, variant = VARIANT_WEB_SCAN_ANONYMOUS)
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
	                            		requestHeaders(

	                            		),
                                        pathParameters(
                                                parameterWithName(PROJECT_ID.paramName()).description("The unique id of the project id where a new sechub job shall be created")
                                        ),
                                        requestFields(
                                                fieldWithPath(PROPERTY_API_VERSION).description("The api version, currently only 1.0 is supported"),
                                                fieldWithPath(PROPERTY_WEB_SCAN).description("Webscan configuration block").optional(),
                                                fieldWithPath(PROPERTY_WEB_SCAN+"."+SecHubWebScanConfiguration.PROPERTY_URL).description("Webscan URI to scan for").optional(),
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
    @UseCaseRestDoc(useCase = UseCaseUserCreatesNewJob.class, variant = VARIANT_WEB_SCAN_WITH_API_DEFINITION)
    public void restDoc_userCreatesNewJob_webscan_with_api_definition() throws Exception {
        /* prepare */
        String apiEndpoint = https(PORT_USED).buildAddJobUrl(PROJECT_ID.pathElement());
        Class<? extends Annotation> useCase = UseCaseUserCreatesNewJob.class;

        UUID randomUUID = UUID.randomUUID();
        SchedulerResult mockResult = new SchedulerResult(randomUUID);

        SecHubWebScanApiConfiguration apiConfig = new SecHubWebScanApiConfiguration();
        apiConfig.setType(SecHubWebScanApiType.OPEN_API);
        apiConfig.getNamesOfUsedDataConfigurationObjects().add("openApi-file-reference");
        URL apiDefinitionUrl = new URL("https://www.example.org/api/v1/swagger/");
        apiConfig.setApiDefinitionUrl(apiDefinitionUrl);

        when(mockedScheduleCreateJobService.createJob(any(), any(SecHubConfiguration.class))).thenReturn(mockResult);

        /* execute + test @formatter:off */
        this.mockMvc.perform(
                post(apiEndpoint,PROJECT1_ID).
                    contentType(MediaType.APPLICATION_JSON_VALUE).
                    content(configureSecHub().
                            api("1.0").
                            webConfig().
                                addURI("https://www.example.org/").
                                addApiConfig(apiConfig).
                            build().
                            toJSON())
                ).
                    andExpect(status().isOk()).
                    andExpect(content().json("{jobId:"+randomUUID.toString()+"}")).
                    andDo(defineRestService().
                            with().
                                useCaseData(useCase, VARIANT_WEB_SCAN_WITH_API_DEFINITION).
                                tag(RestDocFactory.extractTag(apiEndpoint)).
                                requestSchema(OpenApiSchema.SCAN_JOB.getSchema()).
                                responseSchema(OpenApiSchema.JOB_ID.getSchema()).
                            and().
                            document(
                                        requestHeaders(

                                        ),
                                        pathParameters(
                                                parameterWithName(PROJECT_ID.paramName()).description("The unique id of the project id where a new sechub job shall be created")
                                        ),
                                        requestFields(
                                                fieldWithPath(PROPERTY_API_VERSION).description("The api version, currently only 1.0 is supported"),
                                                fieldWithPath(PROPERTY_WEB_SCAN).description("Webscan configuration block").optional(),
                                                fieldWithPath(PROPERTY_WEB_SCAN+"."+SecHubWebScanConfiguration.PROPERTY_URL).description("Webscan URI to scan for").optional(),
                                                fieldWithPath(PROPERTY_WEB_SCAN+"."+SecHubWebScanConfiguration.PROPERTY_API+"."+SecHubWebScanApiConfiguration.PROPERTY_TYPE).description("Type of the API definition files that will be provided").optional(),
                                                fieldWithPath(PROPERTY_WEB_SCAN+"."+SecHubWebScanConfiguration.PROPERTY_API+"."+SecHubDataConfigurationUsageByName.PROPERTY_USE).description("Reference to the data section containing the API definition files. Always use 'sources' with 'files' instead 'folders'.").optional(),
                                                fieldWithPath(PROPERTY_WEB_SCAN+"."+SecHubWebScanConfiguration.PROPERTY_API+"."+SecHubWebScanApiConfiguration.PROPERTY_API_DEFINITION_URL).description("Specifies an URL to read the API definition from.").optional()
                                        ),
                                        responseFields(
                                                fieldWithPath(SchedulerResult.PROPERTY_JOBID).description("A unique job id")
                                        )
                          ));

        /* @formatter:on */
    }

    @Test
    @UseCaseRestDoc(useCase = UseCaseUserCreatesNewJob.class, variant = VARIANT_WEB_SCAN_WITH_CLIENT_CERTIFICATE_DEFINITION)
    public void restDoc_userCreatesNewJob_webscan_with_client_certificate_definition() throws Exception {
        /* prepare */
        String apiEndpoint = https(PORT_USED).buildAddJobUrl(PROJECT_ID.pathElement());
        Class<? extends Annotation> useCase = UseCaseUserCreatesNewJob.class;

        UUID randomUUID = UUID.randomUUID();
        SchedulerResult mockResult = new SchedulerResult(randomUUID);

        ClientCertificateConfiguration clientCertificateConfig = new ClientCertificateConfiguration();
        clientCertificateConfig.setPassword("example-cert-password".toCharArray());
        clientCertificateConfig.getNamesOfUsedDataConfigurationObjects().add("client-certificate-file-reference");

        when(mockedScheduleCreateJobService.createJob(any(), any(SecHubConfiguration.class))).thenReturn(mockResult);

        /* execute + test @formatter:off */
        this.mockMvc.perform(
                post(apiEndpoint,PROJECT1_ID).
                    contentType(MediaType.APPLICATION_JSON_VALUE).
                    content(configureSecHub().
                            api("1.0").
                            webConfig().
                                addURI("https://localhost/mywebapp").
                                addClientCertificateConfig(clientCertificateConfig).
                            build().
                            toJSON())
                ).
                    andExpect(status().isOk()).
                    andExpect(content().json("{jobId:"+randomUUID.toString()+"}")).
                    andDo(defineRestService().
                            with().
                                useCaseData(useCase, VARIANT_WEB_SCAN_WITH_CLIENT_CERTIFICATE_DEFINITION).
                                tag(RestDocFactory.extractTag(apiEndpoint)).
                                requestSchema(OpenApiSchema.SCAN_JOB.getSchema()).
                                responseSchema(OpenApiSchema.JOB_ID.getSchema()).
                            and().
                            document(
                                        requestHeaders(

                                        ),
                                        pathParameters(
                                                parameterWithName(PROJECT_ID.paramName()).description("The unique id of the project id where a new sechub job shall be created")
                                        ),
                                        requestFields(
                                                fieldWithPath(PROPERTY_API_VERSION).description("The api version, currently only 1.0 is supported"),
                                                fieldWithPath(PROPERTY_WEB_SCAN).description("Webscan configuration block").optional(),
                                                fieldWithPath(PROPERTY_WEB_SCAN+"."+SecHubWebScanConfiguration.PROPERTY_URL).description("Webscan URI to scan for").optional(),
                                                fieldWithPath(PROPERTY_WEB_SCAN+"."+SecHubWebScanConfiguration.PROPERTY_CLIENT_CERTIFICATE+"."+ClientCertificateConfiguration.PROPERTY_PASSWORD).description("Password the client certificate file is protected with").optional(),
                                                fieldWithPath(PROPERTY_WEB_SCAN+"."+SecHubWebScanConfiguration.PROPERTY_CLIENT_CERTIFICATE+"."+SecHubDataConfigurationUsageByName.PROPERTY_USE).description("Reference to the data section containing the client certificate definition file. Always use 'sources' with a single 'file' instead 'folders'.").optional()
                                        ),
                                        responseFields(
                                                fieldWithPath(SchedulerResult.PROPERTY_JOBID).description("A unique job id")
                                        )
                          ));

        /* @formatter:on */
    }

    @Test
    @UseCaseRestDoc(useCase = UseCaseUserCreatesNewJob.class, variant = VARIANT_WEB_SCAN_LOGIN_BASIC)
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
                                useCaseData(useCase, VARIANT_WEB_SCAN_LOGIN_BASIC).
                                tag(RestDocFactory.extractTag(apiEndpoint)).
                                requestSchema(OpenApiSchema.SCAN_JOB.getSchema()).
                                responseSchema(OpenApiSchema.JOB_ID.getSchema()).
                            and().
                            document(
	                            		requestHeaders(

	                            		),
                                        pathParameters(
                                                parameterWithName(PROJECT_ID.paramName()).description("The unique id of the project id where a new sechub job shall be created")
                                        ),
                                        requestFields(
                                                fieldWithPath(PROPERTY_API_VERSION).description("The api version, currently only 1.0 is supported"),
                                                fieldWithPath(PROPERTY_WEB_SCAN).description("Webscan configuration block").optional(),
                                                fieldWithPath(PROPERTY_WEB_SCAN+"."+SecHubWebScanConfiguration.PROPERTY_URL).description("Webscan URI to scan for").optional(),
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
    @UseCaseRestDoc(useCase = UseCaseUserCreatesNewJob.class, variant = VARIANT_WEB_SCAN_LOGIN_FORM_SCRIPTED)
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
                            useCaseData(useCase, VARIANT_WEB_SCAN_LOGIN_FORM_SCRIPTED).
                            tag(RestDocFactory.extractTag(apiEndpoint)).
                            requestSchema(OpenApiSchema.SCAN_JOB.getSchema()).
                            responseSchema(OpenApiSchema.JOB_ID.getSchema()).
                        and().
                        document(
		                        		requestHeaders(

		                        		),
                                        pathParameters(
                                                parameterWithName(PROJECT_ID.paramName()).description("The unique id of the project id where a new sechub job shall be created")
                                        ),
                                        requestFields(
                                            fieldWithPath(PROPERTY_API_VERSION).description("The api version, currently only 1.0 is supported"),
    										fieldWithPath(PROPERTY_WEB_SCAN).description("Webscan configuration block").optional(),
    										fieldWithPath(PROPERTY_WEB_SCAN+"."+SecHubWebScanConfiguration.PROPERTY_URL).description("Webscan URI to scan for").optional(),
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
    @UseCaseRestDoc(useCase = UseCaseUserCreatesNewJob.class, variant = VARIANT_WEB_SCAN_HEADERS)
    public void restDoc_userCreatesNewJob_webscan_with_headers() throws Exception {
        /* prepare */
        String apiEndpoint = https(PORT_USED).buildAddJobUrl(PROJECT_ID.pathElement());
        Class<? extends Annotation> useCase = UseCaseUserCreatesNewJob.class;

        HTTPHeaderConfiguration header = new HTTPHeaderConfiguration();
        header.setName("api-token");
        header.setValue("secret");
        List<String> onlyForUrls = Arrays.asList("https://localhost/mywebapp/admin", "https://localhost/mywebapp/<*>/profile",
                "https://localhost/mywebapp/blog/<*>");
        header.setOnlyForUrls(Optional.ofNullable(onlyForUrls));
        header.getNamesOfUsedDataConfigurationObjects().add("header-value-file-ref-for-big-tokens");

        List<HTTPHeaderConfiguration> httpHeaders = new ArrayList<>();
        httpHeaders.add(header);

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
	    						addHeaders(httpHeaders).
	    					build().
	    					toJSON())
	    		).
	    			andExpect(status().isOk()).
	    			andExpect(content().json("{jobId:"+randomUUID.toString()+"}")).
	    			andDo(defineRestService().
                            with().
                                useCaseData(useCase, VARIANT_WEB_SCAN_HEADERS).
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
                                                fieldWithPath(PROPERTY_WEB_SCAN+"."+SecHubWebScanConfiguration.PROPERTY_URL).description("Webscan URI to scan for").optional(),
                                                fieldWithPath(PROPERTY_WEB_SCAN+"."+SecHubWebScanConfiguration.PROPERTY_HEADERS).description("List of HTTP headers. Can be used for authentication or anything else.").optional(),
                                                fieldWithPath(PROPERTY_WEB_SCAN+"."+SecHubWebScanConfiguration.PROPERTY_HEADERS+"[]."+HTTPHeaderConfiguration.PROPERTY_NAME).description("Name of the defined HTTP header.").optional(),
                                                fieldWithPath(PROPERTY_WEB_SCAN+"."+SecHubWebScanConfiguration.PROPERTY_HEADERS+"[]."+HTTPHeaderConfiguration.PROPERTY_VALUE).description("Value of the defined HTTP header. Either specify the header value directly here or reference a data section with 'use' e.g. if the value is to big, but never specify both.").optional(),
                                                fieldWithPath(PROPERTY_WEB_SCAN+"."+SecHubWebScanConfiguration.PROPERTY_HEADERS+"[]."+SecHubDataConfigurationUsageByName.PROPERTY_USE).description("Reference to the data section containing a file with the value for this header, e.g if the value is to big for the sechub configuration. Always use 'sources' with a single 'file' instead 'folders'.").optional(),
                                                fieldWithPath(PROPERTY_WEB_SCAN+"."+SecHubWebScanConfiguration.PROPERTY_HEADERS+"[]."+HTTPHeaderConfiguration.PROPERTY_ONLY_FOR_URLS+"[]").description("Optional list of URLs this header shall be used for like: https://mywebapp.com/path/. Can contain wildcards like: https://mywebapp.com/path/<*>/with/wildcard").optional(),
                                                fieldWithPath(PROPERTY_WEB_SCAN+"."+SecHubWebScanConfiguration.PROPERTY_HEADERS+"[]."+HTTPHeaderConfiguration.PROPERTY_SENSITIVE).description("Defines header masking. If 'true' the header value will be replaced with '********' inside the report, 'false' will show the value as is. Default is set to 'true'.").optional()
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
                multipart(apiEndpoint, PROJECT1_ID, randomUUID).
                    file(file1).
                    queryParam(MULTIPART_CHECKSUM, "checkSumValue")
                ).
                    andExpect(status().isOk()).
                            // https://docs.spring.io/spring-restdocs/docs/2.0.2.RELEASE/reference/html5/
                    andDo(defineRestService().
                            with().
                                useCaseData(useCase).
                                tag(RestDocFactory.extractTag(apiEndpoint)).
                            and().
                            document(
                                    requestHeaders(
                                    ),
                                    pathParameters(
                                            parameterWithName("projectId").description("The id of the project where sourcecode shall be uploaded for"),
                                            parameterWithName("jobUUID").description(DESCRIPTION_JOB_UUID)
                                    ),
                                    queryParameters(
                                            parameterWithName(MULTIPART_CHECKSUM).description("A sha256 checksum for file upload validation")
                                    ),
                                    // TODO Jeremias Eppler, 2020-12-07: It is not possible to document this part properly in OpenAPI.
                                    // See: https://github.com/ePages-de/restdocs-api-spec/issues/105
                                    requestParts(
                                            partWithName(MULTIPART_FILE).description("The sourcecode as zipfile to upload")
                                    )
                    ));

        /* @formatter:on */
    }

    @Test
    @UseCaseRestDoc(useCase = UseCaseUserUploadsBinaries.class)
    public void restDoc_userUploadsBinaries() throws Exception {
        /* prepare */
        String apiEndpoint = https(PORT_USED).buildUploadBinariesUrl(PROJECT_ID.pathElement(), JOB_UUID.pathElement());
        Class<? extends Annotation> useCase = UseCaseUserUploadsBinaries.class;

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

        InputStream inputStreamTo = RestDocTestFileSupport.getTestfileSupport().getInputStreamTo("upload/tarfile_contains_only_test1.txt.tar");
        MockMultipartFile file = new MockMultipartFile("file", inputStreamTo);
        MockMultipartFile checkSum = new MockMultipartFile("checkSum", "", "", "checkSumValue".getBytes());

        /* execute + test */
        /* @formatter:off */
        this.mockMvc.perform(

                multipart(apiEndpoint, PROJECT1_ID, randomUUID).
                    file(file).
                    file(checkSum).
                    header(CommonConstants.FILE_SIZE_HEADER_FIELD_NAME, file.getBytes().length)
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
                                            parameterWithName("projectId").description("The id of the project for which the binaries are uploaded for"),
                                            parameterWithName("jobUUID").description(DESCRIPTION_JOB_UUID)
                                    ),
                                    requestHeaders(
                                    		headerWithName(CommonConstants.FILE_SIZE_HEADER_FIELD_NAME).description("The file size of the tar-archive to upload in bytes. Needs to be a positive integer value.")
                                    ),
                                    // TODO de-jcup, 2022-04-14: It is not possible to document this part properly in OpenAPI.
                                    // See: https://github.com/ePages-de/restdocs-api-spec/issues/105
                                    requestParts(
                                    		partWithName("file").description("The binaries as tarfile to upload"),
                                    		partWithName(CommonConstants.MULTIPART_CHECKSUM).description("A sha256 checksum for file upload validation")
                                    )
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
                            		requestHeaders(

                            		),
	                                pathParameters(
	                                         parameterWithName("projectId").description("The id of the project where sechub job shall be approved"),
	                                         parameterWithName("jobUUID").description(DESCRIPTION_JOB_UUID)
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
	                            		 requestHeaders(

	                            		 ),
                                          pathParameters(
                                            parameterWithName("projectId").description("The id of the project where sechub job was started for"),
                                            parameterWithName("jobUUID").description(DESCRIPTION_JOB_UUID)
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

    @Test
    @UseCaseRestDoc(useCase = UseCaseUserListsJobsForProject.class)
    public void restDoc_userListsJobsForProject() throws Exception {

        /* prepare */
        Map<String, String> labels = new TreeMap<>();
        labels.put("metadata.labels.stage", "testing");

        String apiEndpoint = https(PORT_USED).buildUserFetchesListOfJobsForProject(PROJECT_ID.pathElement(), SIZE.pathElement(), PAGE.pathElement(),
                WITH_META_DATA.pathElement(), labels);

        Class<? extends Annotation> useCase = UseCaseUserListsJobsForProject.class;

        SecHubJobInfoForUser job1 = new SecHubJobInfoForUser();
        job1.setExecutionResult(ExecutionResult.OK);
        job1.setExecutionState(ExecutionState.ENDED);

        job1.setJobUUID(randomUUID);
        job1.setCreated(LocalDateTime.now().minusMinutes(17));
        job1.setStarted(LocalDateTime.now().minusMinutes(15));
        job1.setEnded(LocalDateTime.now());
        job1.setExecutedBy("User1");
        job1.setTrafficLight(TrafficLight.GREEN);

        SecHubConfigurationMetaData metaData = new SecHubConfigurationMetaData();
        metaData.getLabels().put("stage", "test");
        job1.setMetaData(metaData);

        SecHubJobInfoForUserListPage listPage = new SecHubJobInfoForUserListPage();
        listPage.setPage(0);
        listPage.setTotalPages(1);
        List<SecHubJobInfoForUser> list = listPage.getContent();
        list.add(job1);

        when(mockedJobInfoForUserService.listJobsForProject(eq(PROJECT1_ID), eq(1), eq(0), eq(true), any())).thenReturn(listPage);

        /* execute + test @formatter:off */
        this.mockMvc.perform(
                get(apiEndpoint, PROJECT1_ID, 1, 0, true, labels).
                    contentType(MediaType.APPLICATION_JSON_VALUE)
                ).
                    andExpect(status().isOk()).
                    andExpect(content().json("{page:0, totalPages:1, content:[{jobUUID:"+randomUUID.toString()+", executionState:ENDED, executionResult:OK, trafficLight:GREEN, executedBy:User1, executionState:ENDED}]}")).
                    andDo(defineRestService().
                            with().
                                useCaseData(useCase).
                                tag(RestDocFactory.extractTag(apiEndpoint)).
                                responseSchema(OpenApiSchema.PROJECT_JOB_LIST.getSchema()).
                            and().
                            document(
                                         requestHeaders(

                                         ),
                                          pathParameters(
                                            parameterWithName(PROJECT_ID.paramName()).description("The id of the project where job information shall be fetched for")
                                          ),
                                          queryParameters(
                                              parameterWithName(SIZE.paramName()).optional().description("The wanted (maximum) size for the result set. When not defined, the default will be "+SchedulerRestController.DEFAULT_JOB_INFORMATION_SIZE+"."),
                                              parameterWithName(PAGE.paramName()).optional().description("The wanted page number. When not defined, the default will be "+SchedulerRestController.DEFAULT_JOB_INFORMATION_PAGE+"."),
                                              parameterWithName("metadata.labels.*").optional().
                                                  description("An optional dynamic query parameter to filter jobs by labels. The syntax is 'metadata.labels.${labelKey}=${labelValue}'.\n\n"
                                                            + "It is possible to query for multiple labels (up to "+ SecHubJobInfoForUserService.MAXIMUM_ALLOWED_LABEL_PARAMETERS + " ).\n"
                                                            + "The filter works as an AND combination: Only jobs having all wanted label key value combinations are returned."),
                                              parameterWithName("metadata.labels.stage").ignored(), // we we do not want the label query example to be documented - we document only the generic way
                                              parameterWithName(WITH_META_DATA.paramName()).optional().description("An optional parameter to define if meta data shall be fetched as well. When not defined, the default will be "+SchedulerRestController.DEFAULT_WITH_METADATA+".")
                                          ),
                                          responseFields(
                                            fieldWithPath(SecHubJobInfoForUserListPage.PROPERTY_PAGE).description("The page number"),
                                            fieldWithPath(SecHubJobInfoForUserListPage.PROPERTY_TOTAL_PAGES).description("The total pages available"),
                                            fieldWithPath("content[]."+SecHubJobInfoForUser.PROPERTY_JOBUUID).description("The job uuid"),
                                            fieldWithPath("content[]."+SecHubJobInfoForUser.PROPERTY_CREATED).description("Creation timestamp of job"),
                                            fieldWithPath("content[]."+SecHubJobInfoForUser.PROPERTY_STARTED).description("Start timestamp of job execution"),
                                            fieldWithPath("content[]."+SecHubJobInfoForUser.PROPERTY_ENDED).description("End timestamp of job execution"),
                                            fieldWithPath("content[]."+SecHubJobInfoForUser.PROPERTY_EXECUTED_BY).description("User who initiated the job"),
                                            fieldWithPath("content[]."+SecHubJobInfoForUser.PROPERTY_EXECUTION_STATE).description("Execution state of job"),
                                            fieldWithPath("content[]."+SecHubJobInfoForUser.PROPERTY_EXECUTION_RESULT).description("Execution result of job"),
                                            fieldWithPath("content[]."+SecHubJobInfoForUser.PROPERTY_TRAFFIC_LIGHT).description("Trafficlight of job - but only available when job has been done. Possible states are "+StringUtils.arrayToDelimitedString(TrafficLight.values(),", ")),
                                            fieldWithPath("content[]."+SecHubJobInfoForUser.PROPERTY_METADATA+".*").optional().description("Meta data of job - but only contained in result, when query parameter `"+WITH_META_DATA.paramName()+"` is defined as 'true'."),
                                            fieldWithPath("content[]."+SecHubJobInfoForUser.PROPERTY_METADATA+".labels.stage").ignored()// we do not want the label example to be documented - we document only the generic way
                                          )
                            )
                );

        /* @formatter:on */
    }

    @BeforeEach
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
    public static class SimpleTestConfiguration extends AbstractSecHubAPISecurityConfiguration {

    }
}
