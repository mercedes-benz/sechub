// SPDX-License-Identifier: MIT
package com.daimler.sechub.restdoc;

import static com.daimler.sechub.domain.scan.project.FalsePositiveJobData.*;
import static com.daimler.sechub.domain.scan.project.FalsePositiveJobDataList.*;
import static com.daimler.sechub.test.TestURLBuilder.*;
import static com.daimler.sechub.test.TestURLBuilder.RestDocPathParameter.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import com.daimler.sechub.docgen.util.RestDocPathFactory;
import com.daimler.sechub.domain.scan.ScanAssertService;
import com.daimler.sechub.domain.scan.project.FalsePositiveJobData;
import com.daimler.sechub.domain.scan.project.FalsePositiveJobDataConfigMerger;
import com.daimler.sechub.domain.scan.project.FalsePositiveJobDataList;
import com.daimler.sechub.domain.scan.project.FalsePositiveJobDataListValidation;
import com.daimler.sechub.domain.scan.project.FalsePositiveJobDataService;
import com.daimler.sechub.domain.scan.project.FalsePositiveRestController;
import com.daimler.sechub.domain.scan.project.ScanProjectConfigService;
import com.daimler.sechub.domain.scan.report.ScanReportRepository;
import com.daimler.sechub.sharedkernel.Profiles;
import com.daimler.sechub.sharedkernel.RoleConstants;
import com.daimler.sechub.sharedkernel.UserContextService;
import com.daimler.sechub.sharedkernel.configuration.AbstractAllowSecHubAPISecurityConfiguration;
import com.daimler.sechub.sharedkernel.usecases.UseCaseRestDoc;
import com.daimler.sechub.sharedkernel.usecases.user.execute.UseCaseUserMarksFalsePositivesForJob;
import com.daimler.sechub.sharedkernel.usecases.user.execute.UseCaseUserUnmarksFalsePositives;
import com.daimler.sechub.sharedkernel.validation.UserInputAssertion;
import com.daimler.sechub.test.ExampleConstants;
import com.daimler.sechub.test.TestPortProvider;
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
				put(https(PORT_USED).buildUserAddsFalsePositiveJobDataListForProject(PROJECT_ID.pathElement()),PROJECT1_ID).
		contentType(MediaType.APPLICATION_JSON_VALUE).
        content(content)).
		andExpect(status().isOk()).
		/*andDo(print()).*/
		andDo(document(RestDocPathFactory.createPath(UseCaseUserMarksFalsePositivesForJob.class),
				pathParameters(
					parameterWithName(PROJECT_ID.paramName()).description("The projectId of the project where users adds false positives for")
				),
		        requestFields(
                        fieldWithPath(PROPERTY_API_VERSION).description("The api version, currently only 1.0 is supported"),
                        fieldWithPath(PROPERTY_TYPE).description("The type of the json content. Currently only accepted value is '"+FalsePositiveJobDataList.ACCEPTED_TYPE+"'."),
                        
                        fieldWithPath(PROPERTY_JOBDATA).description("Job data list containing false positive setup based on former jobs"),
                        fieldWithPath(PROPERTY_JOBDATA+"[]."+ PROPERTY_JOBUUID).description("SecHub job uuid where finding was"),
                        fieldWithPath(PROPERTY_JOBDATA+"[]."+ PROPERTY_FINDINGID).description("SecHub finding identifier - identifies problem inside the job which shall be markeda as a false positive. *ATTENTION*: at the moment only code scan false positive handling is supported. Infra and web scan findings will lead to a non accepted error!"),
                        fieldWithPath(PROPERTY_JOBDATA+"[]."+ PROPERTY_COMMENT).optional().description("A comment describing why this is a false positive")
                        )

				));

		/* @formatter:on */
    }
    
    @Test
    @UseCaseRestDoc(useCase = UseCaseUserUnmarksFalsePositives.class)
    public void restdoc_unmark_false_positives() throws Exception {
        /* prepare */
        int findingId=42;
        UUID jobUUID = UUID.fromString("f1d02a9d-5e1b-4f52-99e5-401854ccf936");
        
        /* execute + test @formatter:off */
        this.mockMvc.perform(
                delete(https(PORT_USED).buildUserRemovesFalsePositiveEntryFromProject(PROJECT_ID.pathElement(),JOB_UUID.pathElement(),FINDING_ID.pathElement()),PROJECT1_ID,jobUUID,findingId)
        ).
        andExpect(status().isOk()).
        /*andDo(print()).*/
        andDo(document(RestDocPathFactory.createPath(UseCaseUserUnmarksFalsePositives.class),
                pathParameters(
                    parameterWithName(PROJECT_ID.paramName()).description("The project id"),
                    parameterWithName(JOB_UUID.paramName()).description("Job uuid"),
                    parameterWithName(FINDING_ID.paramName()).description("Finding id - in combination with job UUID this defines the false positive to remove")
                )

                ));

        /* @formatter:on */
    }

    @Profile(Profiles.TEST)
    @EnableAutoConfiguration
    public static class SimpleTestConfiguration extends AbstractAllowSecHubAPISecurityConfiguration {

    }

}
