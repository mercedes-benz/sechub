// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.restdoc;

import static com.mercedesbenz.sechub.restdoc.RestDocumentationTest.*;
import static com.mercedesbenz.sechub.test.RestDocPathParameter.*;
import static com.mercedesbenz.sechub.test.SecHubTestURLBuilder.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
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
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import com.mercedesbenz.sechub.commons.model.TrafficLight;
import com.mercedesbenz.sechub.docgen.util.RestDocFactory;
import com.mercedesbenz.sechub.domain.scan.report.DownloadScanReportService;
import com.mercedesbenz.sechub.domain.scan.report.DownloadSpdxScanReportService;
import com.mercedesbenz.sechub.domain.scan.report.HTMLReportHelper;
import com.mercedesbenz.sechub.domain.scan.report.HTMLScanResultReportModelBuilder;
import com.mercedesbenz.sechub.domain.scan.report.ScanReport;
import com.mercedesbenz.sechub.domain.scan.report.ScanReportRestController;
import com.mercedesbenz.sechub.domain.scan.report.ScanSecHubReport;
import com.mercedesbenz.sechub.sharedkernel.usecases.UseCaseRestDoc;
import com.mercedesbenz.sechub.sharedkernel.usecases.UseCaseRestDoc.SpringRestDocOutput;
import com.mercedesbenz.sechub.sharedkernel.usecases.user.execute.UseCaseUserDownloadsJobReport;
import com.mercedesbenz.sechub.sharedkernel.usecases.user.execute.UseCaseUserDownloadsSpdxJobReport;
import com.mercedesbenz.sechub.test.ExampleConstants;
import com.mercedesbenz.sechub.test.TestIsNecessaryForDocumentation;
import com.mercedesbenz.sechub.test.TestPortProvider;

@RunWith(SpringRunner.class)
@WebMvcTest(ScanReportRestController.class)
@ContextConfiguration(classes = { ScanReportRestController.class, ScanReportRestControllerRestDocTest.SimpleTestConfiguration.class })
@AutoConfigureRestDocs(uriScheme = "https", uriHost = ExampleConstants.URI_SECHUB_SERVER, uriPort = 443)
public class ScanReportRestControllerRestDocTest implements TestIsNecessaryForDocumentation {

    private static final String PROJECT1_ID = "project1";

    private static final int PORT_USED = TestPortProvider.DEFAULT_INSTANCE.getRestDocTestPort();

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DownloadScanReportService downloadReportService;

    @MockBean
    private DownloadSpdxScanReportService downloadSpdxReportService;

    @MockBean
    HTMLScanResultReportModelBuilder modelBuilder;

    private UUID jobUUID;

    @UseCaseRestDoc(useCase = UseCaseUserDownloadsJobReport.class, variant = "JSON", wanted = {

            SpringRestDocOutput.PATH_PARAMETERS,

            SpringRestDocOutput.REQUEST_FIELDS,

            SpringRestDocOutput.CURL_REQUEST })
    @Test
    @WithMockUser
    public void get_report_from_existing_job_returns_information_as_json_when_type_is_APPLICATION_JSON_UTF8() throws Exception {
        /* prepare */
        String apiEndpoint = https(PORT_USED).buildGetJobReportUrl(PROJECT_ID.pathElement(), JOB_UUID.pathElement());
        Class<? extends Annotation> useCase = UseCaseUserDownloadsJobReport.class;

        ScanReport report = new ScanReport(jobUUID, PROJECT1_ID);
        report.setResult("{'count':'1'}");
        report.setTrafficLight(TrafficLight.YELLOW);

        ScanSecHubReport scanSecHubReport = new ScanSecHubReport(report);
        when(downloadReportService.getObfuscatedScanSecHubReport(PROJECT1_ID, jobUUID)).thenReturn(scanSecHubReport);

        /* execute + test @formatter:off */
	    this.mockMvc.perform(
	    		get(apiEndpoint,PROJECT1_ID,jobUUID).
	    		    accept(MediaType.APPLICATION_JSON_VALUE).
	    			contentType(MediaType.APPLICATION_JSON_VALUE).
	    			header(TestAuthenticationHelper.HEADER_NAME, TestAuthenticationHelper.getHeaderValue())
	    		).
	    			andExpect(status().isOk()).
	    			andExpect(content().json("{\"jobUUID\":\""+jobUUID.toString()+"\",\"result\":{\"count\":0,\"findings\":[]},\"trafficLight\":\"YELLOW\"}")).
	    			andDo(defineRestService().
                            with().
                                useCaseData(useCase, "JSON").
                                tag(RestDocFactory.extractTag(apiEndpoint)).
                                responseSchema(TestOpenApiSchema.SECHUB_REPORT.getSchema()).
                            and().
                            document(
	                            		requestHeaders(

	                            		),
                                    	pathParameters(
                                                parameterWithName(PROJECT_ID.paramName()).description("The project Id"),
                                                parameterWithName(JOB_UUID.paramName()).description("The job UUID")
                                    	)
	    			     ));

	    /* @formatter:on */
    }

    @UseCaseRestDoc(useCase = UseCaseUserDownloadsJobReport.class, variant = "HTML", wanted = {

            SpringRestDocOutput.PATH_PARAMETERS,

            SpringRestDocOutput.REQUEST_FIELDS,

            SpringRestDocOutput.CURL_REQUEST })
    @Test
    @WithMockUser
    public void get_report_from_existing_job_returns_information_as_html_when_type_is_APPLICATION_XHTML_XML() throws Exception {
        /* prepare */
        String apiEndpoint = https(PORT_USED).buildGetJobReportUrl(PROJECT_ID.pathElement(), JOB_UUID.pathElement());
        Class<? extends Annotation> useCase = UseCaseUserDownloadsJobReport.class;

        ScanReport report = new ScanReport(jobUUID, PROJECT1_ID);
        report.setResult("{'count':'1'}");
        report.setTrafficLight(TrafficLight.YELLOW);

        ScanSecHubReport scanSecHubReport = new ScanSecHubReport(report);
        assertNotNull(scanSecHubReport.getMetaData());
        when(downloadReportService.getObfuscatedScanSecHubReport(PROJECT1_ID, jobUUID)).thenReturn(scanSecHubReport);

        /* execute + test @formatter:off */
        this.mockMvc.perform(
        		get(apiEndpoint,PROJECT1_ID,jobUUID).
        		    accept(MediaType.APPLICATION_XHTML_XML).
        			contentType(MediaType.APPLICATION_JSON_VALUE).
        			header(TestAuthenticationHelper.HEADER_NAME, TestAuthenticationHelper.getHeaderValue())
        		).
        			andExpect(status().isOk()).
        			andExpect(content().contentType("text/html;charset=UTF-8")).
        			andExpect(content().encoding("UTF-8")).
        			andExpect(content().string(containsString(jobUUID.toString()))).
        			andExpect(content().string(containsString("theRedStyle"))).
        			andDo(defineRestService().
                            with().
                                useCaseData(useCase, "HTML").
                                tag(RestDocFactory.extractTag(apiEndpoint)).
                                responseSchema(TestOpenApiSchema.SECHUB_REPORT.getSchema()).
                            and().
                            document(
	                            		requestHeaders(

	                            		),
                                        pathParameters(
                                                parameterWithName(PROJECT_ID.paramName()).description("The project Id"),
                                                parameterWithName(JOB_UUID.paramName()).description("The job UUID")
                                        )
        			      ));

        /* @formatter:on */
    }

    @UseCaseRestDoc(useCase = UseCaseUserDownloadsSpdxJobReport.class, variant = "JSON", wanted = {

            SpringRestDocOutput.PATH_PARAMETERS,

            SpringRestDocOutput.REQUEST_FIELDS,

            SpringRestDocOutput.CURL_REQUEST })
    @Test
    @WithMockUser
    public void get_report_from_existing_job_returns_information_as_spdx_json_when_type_is_APPLICATION_JSON_UTF8() throws Exception {
        /* prepare */
        String apiEndpoint = https(PORT_USED).buildGetJobReportUrlSpdx(PROJECT_ID.pathElement(), JOB_UUID.pathElement());
        Class<? extends Annotation> useCase = UseCaseUserDownloadsSpdxJobReport.class;

        String spdxReport = "{\"spdxVersion\": \"SPDX-2.2\"}";

        when(downloadSpdxReportService.getScanSpdxJsonReport(PROJECT1_ID, jobUUID)).thenReturn(spdxReport);

        /* execute + test @formatter:off */
	    this.mockMvc.perform(
	    		get(apiEndpoint,PROJECT1_ID,jobUUID).
	    		    accept(MediaType.APPLICATION_JSON_VALUE).
	    			contentType(MediaType.APPLICATION_JSON_VALUE).
	    			header(TestAuthenticationHelper.HEADER_NAME, TestAuthenticationHelper.getHeaderValue())
	    		).
	    			andExpect(status().isOk()).
	    			andExpect(content().json(spdxReport)).
	    			andDo(defineRestService().
                            with().
                                useCaseData(useCase, "JSON").
                                tag(RestDocFactory.extractTag(apiEndpoint)).
                                responseSchema(TestOpenApiSchema.SECHUB_REPORT.getSchema()).
                            and().
                            document(
	                            		requestHeaders(

	                            		),
                                        pathParameters(
                                                parameterWithName(PROJECT_ID.paramName()).description("The project Id"),
                                                parameterWithName(JOB_UUID.paramName()).description("The job UUID")
                                        )
	    			     ));

	    /* @formatter:on */
    }

    @TestConfiguration
    @EnableAutoConfiguration
    public static class SimpleTestConfiguration {

    }

    @Before
    public void before() throws Exception {
        jobUUID = UUID.randomUUID();
        Map<String, Object> map = new HashMap<>();
        map.put("jobuuid", jobUUID);
        map.put("styleRed", "theRedStyle");
        map.put("styleGreen", "display:none");
        map.put("styleYellow", "display:none");
        map.put("isWebDesignMode", false);
        map.put("metaData", null);
        map.put("reportHelper", new HTMLReportHelper());
        map.put("scanTypeSummaries", new ArrayList<>());
        when(modelBuilder.build(any())).thenReturn(map);
    }

}
