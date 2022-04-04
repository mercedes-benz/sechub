// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.restdoc;

import static com.mercedesbenz.sechub.domain.scan.project.FalsePositiveJobData.*;
import static com.mercedesbenz.sechub.domain.scan.project.FalsePositiveJobDataList.*;
import static com.mercedesbenz.sechub.domain.scan.project.FalsePositiveProjectConfiguration.*;
import static com.mercedesbenz.sechub.restdoc.RestDocumentation.*;
import static com.mercedesbenz.sechub.test.TestURLBuilder.*;
import static com.mercedesbenz.sechub.test.TestURLBuilder.RestDocPathParameter.*;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.lang.annotation.Annotation;
import java.util.Date;
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
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import com.mercedesbenz.sechub.commons.model.ScanType;
import com.mercedesbenz.sechub.commons.model.Severity;
import com.mercedesbenz.sechub.docgen.util.RestDocFactory;
import com.mercedesbenz.sechub.domain.scan.ScanAssertService;
import com.mercedesbenz.sechub.domain.scan.project.FalsePositiveCodeMetaData;
import com.mercedesbenz.sechub.domain.scan.project.FalsePositiveCodePartMetaData;
import com.mercedesbenz.sechub.domain.scan.project.FalsePositiveEntry;
import com.mercedesbenz.sechub.domain.scan.project.FalsePositiveJobData;
import com.mercedesbenz.sechub.domain.scan.project.FalsePositiveJobDataConfigMerger;
import com.mercedesbenz.sechub.domain.scan.project.FalsePositiveJobDataList;
import com.mercedesbenz.sechub.domain.scan.project.FalsePositiveJobDataListValidation;
import com.mercedesbenz.sechub.domain.scan.project.FalsePositiveJobDataService;
import com.mercedesbenz.sechub.domain.scan.project.FalsePositiveMetaData;
import com.mercedesbenz.sechub.domain.scan.project.FalsePositiveProjectConfiguration;
import com.mercedesbenz.sechub.domain.scan.project.FalsePositiveRestController;
import com.mercedesbenz.sechub.domain.scan.project.ScanProjectConfigService;
import com.mercedesbenz.sechub.domain.scan.report.ScanReportRepository;
import com.mercedesbenz.sechub.sharedkernel.Profiles;
import com.mercedesbenz.sechub.sharedkernel.RoleConstants;
import com.mercedesbenz.sechub.sharedkernel.UserContextService;
import com.mercedesbenz.sechub.sharedkernel.configuration.AbstractAllowSecHubAPISecurityConfiguration;
import com.mercedesbenz.sechub.sharedkernel.usecases.UseCaseRestDoc;
import com.mercedesbenz.sechub.sharedkernel.usecases.user.execute.UseCaseUserFetchesFalsePositiveConfigurationOfProject;
import com.mercedesbenz.sechub.sharedkernel.usecases.user.execute.UseCaseUserMarksFalsePositivesForJob;
import com.mercedesbenz.sechub.sharedkernel.usecases.user.execute.UseCaseUserUnmarksFalsePositives;
import com.mercedesbenz.sechub.sharedkernel.validation.UserInputAssertion;
import com.mercedesbenz.sechub.test.ExampleConstants;
import com.mercedesbenz.sechub.test.TestPortProvider;

@RunWith(SpringRunner.class)
@WebMvcTest(FalsePositiveRestController.class)
@ContextConfiguration(classes = { FalsePositiveRestController.class, FalsePositiveRestControllerRestDocTest.SimpleTestConfiguration.class })
@WithMockUser(authorities = RoleConstants.ROLE_SUPERADMIN)
@ActiveProfiles({ Profiles.TEST, Profiles.ADMIN_ACCESS })
@AutoConfigureRestDocs(uriScheme = "https", uriHost = ExampleConstants.URI_SECHUB_SERVER, uriPort = 443)
public class FalsePositiveRestControllerRestDocTest {

    private static final int PORT_USED = TestPortProvider.DEFAULT_INSTANCE.getRestDocTestPort();
    private static final String PROJECT1_ID = "project1";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ScanReportRepository scanReportRepository;

    @MockBean
    private UserInputAssertion userInputAssertion;

    @MockBean
    private ScanProjectConfigService configService;

    @MockBean
    private FalsePositiveJobDataService falsePositiveJobDataService;

    @MockBean
    private FalsePositiveJobDataListValidation falsePositiveJobDataListValidation;

    @MockBean
    private FalsePositiveJobDataConfigMerger merger;

    @MockBean
    private UserContextService userContextService;

    @MockBean
    private ScanAssertService scanAssertService;

    @Before
    public void before() {
    }

    @Test
    @UseCaseRestDoc(useCase = UseCaseUserMarksFalsePositivesForJob.class)
    public void restdoc_mark_false_positives_for_job() throws Exception {
        /* prepare */
        String apiEndpoint = https(PORT_USED).buildUserAddsFalsePositiveJobDataListForProject(PROJECT_ID.pathElement());
        Class<? extends Annotation> useCase = UseCaseUserMarksFalsePositivesForJob.class;

        FalsePositiveJobDataList jobDataList = new FalsePositiveJobDataList();
        jobDataList.setApiVersion("1.0");
        List<FalsePositiveJobData> list = jobDataList.getJobData();
        FalsePositiveJobData data = new FalsePositiveJobData();
        data.setComment("an optional comment why this is a false positive...");
        data.setFindingId(42);
        data.setJobUUID(UUID.fromString("f1d02a9d-5e1b-4f52-99e5-401854ccf936"));
        list.add(data);
        String content = jobDataList.toJSON();

        /* execute + test @formatter:off */
		this.mockMvc.perform(
				put(apiEndpoint, PROJECT1_ID).
		contentType(MediaType.APPLICATION_JSON_VALUE).
        content(content)).
		andExpect(status().isOk()).
		/*andDo(print()).*/
		andDo(defineRestService().
                with().
                    useCaseData(useCase).
                    tag(RestDocFactory.extractTag(apiEndpoint)).
                    requestSchema(OpenApiSchema.FALSE_POSITVES_FOR_JOB.getSchema()).
                and().
                document(
                            requestFields(
                                    fieldWithPath(PROPERTY_API_VERSION).description("The api version, currently only 1.0 is supported"),
                                    fieldWithPath(PROPERTY_TYPE).description("The type of the json content. Currently only accepted value is '"+FalsePositiveJobDataList.ACCEPTED_TYPE+"'."),

                                    fieldWithPath(PROPERTY_JOBDATA).description("Job data list containing false positive setup based on former jobs"),
                                    fieldWithPath(PROPERTY_JOBDATA+"[]."+ PROPERTY_JOBUUID).description("SecHub job uuid where finding was"),
                                    fieldWithPath(PROPERTY_JOBDATA+"[]."+ PROPERTY_FINDINGID).description("SecHub finding identifier - identifies problem inside the job which shall be markeda as a false positive. *ATTENTION*: at the moment only code scan false positive handling is supported. Infra and web scan findings will lead to a non accepted error!"),
                                    fieldWithPath(PROPERTY_JOBDATA+"[]."+ PROPERTY_COMMENT).optional().description("A comment describing why this is a false positive")
                            ),
                            pathParameters(
                                    parameterWithName(PROJECT_ID.paramName()).description("The projectId of the project where users adds false positives for")
                         )
				));

		/* @formatter:on */
    }

    @Test
    @UseCaseRestDoc(useCase = UseCaseUserUnmarksFalsePositives.class)
    public void restdoc_unmark_false_positives() throws Exception {
        /* prepare */
        String apiEndpoint = https(PORT_USED).buildUserRemovesFalsePositiveEntryFromProject(PROJECT_ID.pathElement(), JOB_UUID.pathElement(),
                FINDING_ID.pathElement());
        Class<? extends Annotation> useCase = UseCaseUserUnmarksFalsePositives.class;

        int findingId = 42;
        UUID jobUUID = UUID.fromString("f1d02a9d-5e1b-4f52-99e5-401854ccf936");

        /* execute + test @formatter:off */
        this.mockMvc.perform(
                delete(apiEndpoint,PROJECT1_ID,jobUUID,findingId)
        ).
        andExpect(status().isOk()).
        /*andDo(print()).*/
        andDo(defineRestService().
                with().
                    useCaseData(useCase).
                    tag(RestDocFactory.extractTag(apiEndpoint)).
                and().
                document(
                            pathParameters(
                                    parameterWithName(PROJECT_ID.paramName()).description("The project id"),
                                    parameterWithName(JOB_UUID.paramName()).description("Job uuid"),
                                    parameterWithName(FINDING_ID.paramName()).description("Finding id - in combination with job UUID this defines the false positive to remove")
                         )
                ));
        /* @formatter:on */
    }

    @Test
    @UseCaseRestDoc(useCase = UseCaseUserFetchesFalsePositiveConfigurationOfProject.class)
    public void user_fetches_false_positive_configuration() throws Exception {
        /* prepare */
        String apiEndpoint = https(PORT_USED).buildUserFetchesFalsePositiveConfigurationOfProject(PROJECT_ID.pathElement());
        Class<? extends Annotation> useCase = UseCaseUserFetchesFalsePositiveConfigurationOfProject.class;

        int findingId = 42;
        UUID jobUUID = UUID.fromString("f1d02a9d-5e1b-4f52-99e5-401854ccf936");
        FalsePositiveProjectConfiguration config = new FalsePositiveProjectConfiguration();
        List<FalsePositiveEntry> fpList = config.getFalsePositives();
        FalsePositiveEntry entry = new FalsePositiveEntry();
        entry.setAuthor("developer1");
        entry.setCreated(new Date(1591962795187L));

        FalsePositiveJobData jobData1 = new FalsePositiveJobData();
        jobData1.setComment("Only used in documentation build not in deployment");
        jobData1.setJobUUID(jobUUID);
        jobData1.setFindingId(findingId);

        entry.setJobData(jobData1);

        FalsePositiveMetaData metaData = new FalsePositiveMetaData();
        metaData.setCweId(Integer.valueOf(36));
        FalsePositiveCodeMetaData code = new FalsePositiveCodeMetaData();
        FalsePositiveCodePartMetaData start = new FalsePositiveCodePartMetaData();
        start.setLocation("java/com/mercedesbenz/sechub/docgen/AsciidocGenerator.java");
        start.setRelevantPart("args");
        start.setSourceCode("\tpublic static void main(String[] args) throws Exception {");

        code.setStart(start);
        FalsePositiveCodePartMetaData end = new FalsePositiveCodePartMetaData();
        end.setLocation("java/com/mercedesbenz/sechub/docgen/AsciidocGenerator.java");
        end.setRelevantPart("File");
        end.setSourceCode("\t\tFile documentsGenFolder = new File(path);");
        code.setEnd(end);

        metaData.setCode(code);
        metaData.setScanType(ScanType.CODE_SCAN);
        metaData.setSeverity(Severity.MEDIUM);
        metaData.setName("Absolute Path Traversal");

        entry.setMetaData(metaData);

        fpList.add(entry);
        when(falsePositiveJobDataService.fetchFalsePositivesProjectConfiguration(PROJECT1_ID)).thenReturn(config);

        /* execute + test @formatter:off */
        String metaDataPath = PROPERTY_FALSE_POSITIVES+"[]."+FalsePositiveEntry.PROPERTY_METADATA;
        String codeMetaDataPath = metaDataPath+"."+FalsePositiveMetaData.PROPERTY_CODE;

        this.mockMvc.perform(
                get(apiEndpoint,PROJECT1_ID)
        ).
        andExpect(status().isOk()).
        /*andDo(print()).*/
        andDo(defineRestService().
                with().
                    useCaseData(useCase).
                    tag(RestDocFactory.extractTag(apiEndpoint)).
                    responseSchema(OpenApiSchema.FALSE_POSITVES.getSchema()).
                and().
                document(
                            responseFields(
                                    fieldWithPath(PROPERTY_FALSE_POSITIVES).description("Job data list containing false positive setup based on former jobs"),
                                    fieldWithPath(PROPERTY_FALSE_POSITIVES+"[]."+FalsePositiveEntry.PROPERTY_AUTHOR).description("User id of author who created false positive"),
                                    fieldWithPath(PROPERTY_FALSE_POSITIVES+"[]."+FalsePositiveEntry.PROPERTY_CREATED).description("Creation timestamp"),

                                    fieldWithPath(metaDataPath).description("Meta data for this false positive"),
                                    fieldWithPath(metaDataPath+"."+FalsePositiveMetaData.PROPERTY_SCANTYPE).description("Scan type - e.g. codeScan"),
                                    fieldWithPath(metaDataPath+"."+FalsePositiveMetaData.PROPERTY_NAME).description("Name of origin finding marked as false positive"),
                                    fieldWithPath(metaDataPath+"."+FalsePositiveMetaData.PROPERTY_CWE_ID).type(JsonFieldType.NUMBER).optional().description("CWE (common weakness enumeration). For code scans this is always set."),
                                    fieldWithPath(metaDataPath+"."+FalsePositiveMetaData.PROPERTY_CVE_ID).type(JsonFieldType.STRING).optional().description("CVE (common vulnerability and exposures). For infra scans this is always set."),
                                    fieldWithPath(metaDataPath+"."+FalsePositiveMetaData.PROPERTY_OWASP).type(JsonFieldType.STRING).optional().description("OWASP At least this field must be set for web scans when no cwe identifier is defined."),
                                    fieldWithPath(metaDataPath+"."+FalsePositiveMetaData.PROPERTY_SEVERITY).description("Severity of origin report entry marked as false positive"),
                                    fieldWithPath(codeMetaDataPath).optional().description("Code part. Only available for scan type 'codeScan'"),

                                    fieldWithPath(codeMetaDataPath+"."+FalsePositiveCodeMetaData.PROPERTY_START).description("entry point"),
                                    fieldWithPath(codeMetaDataPath+"."+FalsePositiveCodeMetaData.PROPERTY_START+"."+FalsePositiveCodePartMetaData.PROPERTY_LOCATION).description("location of code"),
                                    fieldWithPath(codeMetaDataPath+"."+FalsePositiveCodeMetaData.PROPERTY_START+"."+FalsePositiveCodePartMetaData.PROPERTY_RELEVANT_PART).description("relevant part of source vulnerability"),
                                    fieldWithPath(codeMetaDataPath+"."+FalsePositiveCodeMetaData.PROPERTY_START+"."+FalsePositiveCodePartMetaData.PROPERTY_SOURCE_CODE).description("source code"),

                                    fieldWithPath(codeMetaDataPath+"."+FalsePositiveCodeMetaData.PROPERTY_END).optional().description("end point (sink)"),
                                    fieldWithPath(codeMetaDataPath+"."+FalsePositiveCodeMetaData.PROPERTY_END+"."+FalsePositiveCodePartMetaData.PROPERTY_LOCATION).description("location of code"),
                                    fieldWithPath(codeMetaDataPath+"."+FalsePositiveCodeMetaData.PROPERTY_END+"."+FalsePositiveCodePartMetaData.PROPERTY_RELEVANT_PART).description("relevant part of source vulnerability"),
                                    fieldWithPath(codeMetaDataPath+"."+FalsePositiveCodeMetaData.PROPERTY_END+"."+FalsePositiveCodePartMetaData.PROPERTY_SOURCE_CODE).description("source code"),

                                    fieldWithPath(PROPERTY_FALSE_POSITIVES+"[]."+FalsePositiveEntry.PROPERTY_JOBDATA).description("Job data parts, can be used as key to identify false positives"),
                                    fieldWithPath(PROPERTY_FALSE_POSITIVES+"[]."+FalsePositiveEntry.PROPERTY_JOBDATA+"."+PROPERTY_JOBUUID).description("SecHub job uuid where finding was"),
                                    fieldWithPath(PROPERTY_FALSE_POSITIVES+"[]."+FalsePositiveEntry.PROPERTY_JOBDATA+"."+PROPERTY_FINDINGID).description("SecHub finding identifier - identifies problem inside the job which shall be markeda as a false positive. *ATTENTION*: at the moment only code scan false positive handling is supported. Infra and web scan findings will lead to a non accepted error!"),
                                    fieldWithPath(PROPERTY_FALSE_POSITIVES+"[]."+FalsePositiveEntry.PROPERTY_JOBDATA+"."+PROPERTY_COMMENT).optional().description("A comment from author describing why this was marked as a false positive")
                            ),
                            pathParameters(
                                    parameterWithName(PROJECT_ID.paramName()).description("The project id")
                         )
                ));

        /* @formatter:on */
    }

    @Profile(Profiles.TEST)
    @EnableAutoConfiguration
    public static class SimpleTestConfiguration extends AbstractAllowSecHubAPISecurityConfiguration {

    }

}
