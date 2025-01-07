// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.restdoc;

import static com.mercedesbenz.sechub.restdoc.RestDocumentation.*;
import static com.mercedesbenz.sechub.test.RestDocPathParameter.*;
import static com.mercedesbenz.sechub.test.SecHubTestURLBuilder.*;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.lang.annotation.Annotation;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import com.mercedesbenz.sechub.docgen.util.RestDocFactory;
import com.mercedesbenz.sechub.domain.scan.admin.FullScanData;
import com.mercedesbenz.sechub.domain.scan.admin.FullScanDataRestController;
import com.mercedesbenz.sechub.domain.scan.admin.FullScanDataService;
import com.mercedesbenz.sechub.domain.scan.admin.ScanData;
import com.mercedesbenz.sechub.domain.scan.log.ProjectScanLog;
import com.mercedesbenz.sechub.sharedkernel.Profiles;
import com.mercedesbenz.sechub.sharedkernel.logging.AuditLogService;
import com.mercedesbenz.sechub.sharedkernel.logging.LogSanitizer;
import com.mercedesbenz.sechub.sharedkernel.security.RoleConstants;
import com.mercedesbenz.sechub.sharedkernel.usecases.UseCaseRestDoc;
import com.mercedesbenz.sechub.sharedkernel.usecases.UseCaseRestDoc.SpringRestDocOutput;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.project.UseCaseAdminDownloadsFullScanDataForJob;
import com.mercedesbenz.sechub.test.ExampleConstants;
import com.mercedesbenz.sechub.test.TestIsNecessaryForDocumentation;
import com.mercedesbenz.sechub.test.TestPortProvider;

@RunWith(SpringRunner.class)
@WebMvcTest
@ContextConfiguration(classes = { FullScanDataRestController.class, LogSanitizer.class })
@Import(TestRestDocSecurityConfiguration.class)
@WithMockUser(roles = RoleConstants.ROLE_SUPERADMIN)
@ActiveProfiles(Profiles.TEST)
@AutoConfigureRestDocs(uriScheme = "https", uriHost = ExampleConstants.URI_SECHUB_SERVER, uriPort = 443)
public class DownloadsFullScanDataForJobRestDocTest implements TestIsNecessaryForDocumentation {

    private static final int PORT_USED = TestPortProvider.DEFAULT_INSTANCE.getRestDocTestPort();

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    FullScanDataService fullScanDataService;

    @MockBean
    AuditLogService auditLogService;

    private UUID jobUUID;

    @Before
    public void before() {
        jobUUID = UUID.randomUUID();
        FullScanData data = new FullScanData();
        ScanData d = new ScanData();
        d.productId = "productX";
        d.result = "{ 'result':'OK'}";
        d.metaData = "{}";
        data.allScanData.add(d);

        ProjectScanLog log = new ProjectScanLog("theProject", jobUUID, "spartakus");
        data.allScanLogs.add(log);

        when(fullScanDataService.getFullScanData(jobUUID)).thenReturn(data);
    }

    @Test
    @UseCaseRestDoc(useCase = UseCaseAdminDownloadsFullScanDataForJob.class, wanted = {

            SpringRestDocOutput.PATH_PARAMETERS,

            SpringRestDocOutput.REQUEST_FIELDS,

            SpringRestDocOutput.CURL_REQUEST })
    public void restdoc_admin_downloads_fullscan_data_for_job() throws Exception {
        /* prepare */
        String apiEndpoint = https(PORT_USED).buildAdminDownloadsZipFileContainingFullScanDataFor(JOB_UUID.pathElement());
        Class<? extends Annotation> useCase = UseCaseAdminDownloadsFullScanDataForJob.class;

        /* execute + test @formatter:off */
		this.mockMvc.perform(
					get(apiEndpoint,jobUUID).
					contentType(MediaType.APPLICATION_JSON_VALUE).
					header(AuthenticationHelper.HEADER_NAME, AuthenticationHelper.getHeaderValue())
				).
		andExpect(status().isOk()).
		andDo(defineRestService().
                with().
                    useCaseData(useCase).
                    tag(RestDocFactory.extractTag(apiEndpoint)).
                    responseSchema(OpenApiSchema.FULL_SCAN_DATA_ZIP.getSchema()).
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

}
