// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan;

import static com.mercedesbenz.sechub.test.SecHubTestURLBuilder.*;
import static org.hamcrest.CoreMatchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import com.mercedesbenz.sechub.commons.model.*;
import com.mercedesbenz.sechub.domain.scan.product.ReportProductExecutionService;
import com.mercedesbenz.sechub.domain.scan.report.*;
import com.mercedesbenz.sechub.test.TestPortProvider;

@ExtendWith(SpringExtension.class)
@WebMvcTest(ScanReportRestController.class)
@ContextConfiguration(classes = { ScanReportRestController.class, ScanReportRestControllerMockTest.SimpleTestConfiguration.class })
class ScanReportRestControllerMockTest {

    private static final String PROJECT1_ID = "project1";

    private static final int PORT_USED = TestPortProvider.DEFAULT_INSTANCE.getWebMVCTestHTTPSPort();

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CreateScanReportService createReportService;

    @MockBean
    private DownloadScanReportService downloadReportService;

    @MockBean
    private DownloadSpdxScanReportService serecoSpdaxDownloadService;

    @MockBean
    private SecHubReportProductTransformerService secHubResultService;

    @MockBean
    private ReportProductExecutionService reportProductExecutionService;

    @MockBean
    private TrafficLightCalculator trafficLightCalculator;

    @MockBean
    private ScanReportRepository reportRepository;

    @MockBean
    private HTMLScanResultReportModelBuilder modelBuilder;

    private UUID randomUUID;

    private Map<String, Object> reportModelBuilderResult;

    @Test
    @WithMockUser
    void get_report_from_existing_job_returns_information_as_json_when_type_is_APPLICATION_JSON_UTF8() throws Exception {
        internalTestAcceptedAndReturnsJSON(MediaType.APPLICATION_JSON);
    }

    @Test
    @WithMockUser
    void get_report_from_existing_job_returns_406_NOT_ACCEPTABLE__when_type_is_APPLICATION_PDF() throws Exception {
        /* prepare */
        ScanReport scanReport = new ScanReport(randomUUID, PROJECT1_ID);
        scanReport.setResult("{'count':'1'}");
        scanReport.setTrafficLight(TrafficLight.YELLOW);

        ScanSecHubReport scanSecHubReport = new ScanSecHubReport(scanReport);
        when(downloadReportService.getScanSecHubReport(PROJECT1_ID, randomUUID)).thenReturn(scanSecHubReport);

        /* execute + test @formatter:off */
        this.mockMvc.perform(
        		get(https(PORT_USED).buildGetJobReportUrl(PROJECT1_ID,randomUUID)).accept(MediaType.APPLICATION_PDF).
        			contentType(MediaType.APPLICATION_JSON_VALUE)
        		).
        			andExpect(status().isNotAcceptable()
        		);

        /* @formatter:on */
    }

    @Test
    @WithMockUser
    void get_report_from_existing_job_returns_information_as_html_when_type_is_APPLICATION_XHTML_XML() throws Exception {
        internalTestAcceptedAndReturnsHTML(MediaType.APPLICATION_XHTML_XML);
    }

    @Test
    @WithMockUser
    void get_report_from_existing_job_returns_information_as_html_when_type_is_TEXT_HTML() throws Exception {
        internalTestAcceptedAndReturnsHTML(MediaType.TEXT_HTML);
    }

    @Test
    @WithMockUser
    void get_html_report_with_cwe_id() throws Exception {
        /* prepare */

        Integer cweId = Integer.valueOf(77);

        HTMLCodeScanEntriesSecHubFindingData finding = new HTMLCodeScanEntriesSecHubFindingData();
        finding.setCweId(cweId);
        finding.setSeverity(Severity.HIGH);
        finding.setType(ScanType.CODE_SCAN);
        finding.setDescription("Potential file inclusion via variable");

        reportModelBuilderResult.put("reportHelper", HTMLReportHelper.DEFAULT);
        reportModelBuilderResult.put("redHTMLSecHubFindingList", Arrays.asList(finding));

        when(modelBuilder.build(any())).thenReturn(reportModelBuilderResult);

        /* execute + test @formatter:off */
        this.mockMvc.perform(
                get(https(PORT_USED).buildGetJobReportUrl(PROJECT1_ID,randomUUID)).accept(MediaType.TEXT_HTML).
                    contentType(MediaType.APPLICATION_JSON_VALUE)
                ).  andDo(print()).
                    andExpect(status().isOk()).
                    andExpect(content().contentType("text/html;charset=UTF-8")).
                    andExpect(content().encoding("UTF-8")).
                    andExpect(content().string(containsString(randomUUID.toString()))).
                    andExpect(content().string(containsString("CWE-" + cweId.toString()))).
                    andExpect(content().string(containsString("href=\"https://cwe.mitre.org/data/definitions/" + cweId.toString() + ".html\""))
                );

        /* @formatter:on */
    }

    @Test
    @WithMockUser
    void get_html_report_without_cwe_id() throws Exception {
        /* prepare */

        reportModelBuilderResult.put("redList", Arrays.asList(new SecHubFinding()));
        reportModelBuilderResult.put("codeScanEntries", new ArrayList<>());

        when(modelBuilder.build(any())).thenReturn(reportModelBuilderResult);

        /* execute + test @formatter:off */
        this.mockMvc.perform(
                get(https(PORT_USED).buildGetJobReportUrl(PROJECT1_ID,randomUUID)).accept(MediaType.TEXT_HTML).
                    contentType(MediaType.APPLICATION_JSON_VALUE)
                ).  andDo(print()).
                    andExpect(status().isOk()).
                    andExpect(content().contentType("text/html;charset=UTF-8")).
                    andExpect(content().encoding("UTF-8")).
                    andExpect(content().string(containsString(randomUUID.toString()))).
                    andExpect(content().string(not(containsString("CWE-")))
                );

        /* @formatter:on */
    }

    @Test
    @WithMockUser
    void get_spdx_json_report() throws Exception {
        /* prepare */
        String spdxJsonReport = "{ spdx }";
        when(serecoSpdaxDownloadService.getScanSpdxJsonReport(PROJECT1_ID, randomUUID)).thenReturn(spdxJsonReport);

        /* execute + test @formatter:off */
        this.mockMvc.perform(
                get(https(PORT_USED).buildGetJobReportUrlSpdx(PROJECT1_ID, randomUUID)).accept(MediaType.APPLICATION_JSON)
                ).
        			andDo(print()).
        			andExpect(status().isOk())
                ;

        /* @formatter:on */
    }

    private void internalTestAcceptedAndReturnsJSON(MediaType acceptedType) throws Exception {
        /* prepare */
        ScanReport report = new ScanReport(randomUUID, PROJECT1_ID);
        report.setResult("{'count':'1'}");
        report.setTrafficLight(TrafficLight.YELLOW);

        ScanSecHubReport scanSecHubReport = new ScanSecHubReport(report);
        when(downloadReportService.getScanSecHubReport(PROJECT1_ID, randomUUID)).thenReturn(scanSecHubReport);

        /* execute + test @formatter:off */
	    this.mockMvc.perform(
	    		get(https(PORT_USED).buildGetJobReportUrl(PROJECT1_ID,randomUUID)).accept(acceptedType).
	    			contentType(MediaType.APPLICATION_JSON_VALUE)
	    		).
	    			andExpect(status().isOk()).
	    			andExpect(content().json("{\"jobUUID\":\""+randomUUID.toString()+"\",\"result\":{\"count\":0,\"findings\":[]},\"trafficLight\":\"YELLOW\"}")
	    		);

	    /* @formatter:on */
    }

    private void internalTestAcceptedAndReturnsHTML(MediaType acceptedType) throws Exception {
        /* prepare */
        ScanReport report = new ScanReport(randomUUID, PROJECT1_ID);
        report.setResult("{'count':'1'}");
        report.setTrafficLight(TrafficLight.YELLOW);

        ScanSecHubReport scanSecHubReport = new ScanSecHubReport(report);
        when(downloadReportService.getScanSecHubReport(PROJECT1_ID, randomUUID)).thenReturn(scanSecHubReport);

        /* execute + test @formatter:off */
        this.mockMvc.perform(
        		get(https(PORT_USED).buildGetJobReportUrl(PROJECT1_ID,randomUUID)).accept(acceptedType).
        			contentType(MediaType.APPLICATION_JSON_VALUE)
        		).  andDo(print()).
        			andExpect(status().isOk()).
        			andExpect(content().contentType("text/html;charset=UTF-8")).
        			andExpect(content().encoding("UTF-8")).
        			andExpect(content().string(containsString(randomUUID.toString()))).
        			andExpect(content().string(containsString("theRedStyle"))

        		);

        /* @formatter:on */
    }

    @TestConfiguration
    @EnableAutoConfiguration
    public static class SimpleTestConfiguration {

    }

    @BeforeEach
    void beforeEach() throws Exception {
        randomUUID = UUID.randomUUID();

        reportModelBuilderResult = new HashMap<>();

        /* define report model builder default result */
        reportModelBuilderResult.put("jobuuid", randomUUID);
        reportModelBuilderResult.put("styleRed", "theRedStyle");
        reportModelBuilderResult.put("styleGreen", "display:none");
        reportModelBuilderResult.put("styleYellow", "display:none");
        reportModelBuilderResult.put("redList", new ArrayList<>());
        reportModelBuilderResult.put("yellowList", new ArrayList<>());
        reportModelBuilderResult.put("greenList", new ArrayList<>());
        reportModelBuilderResult.put("isWebDesignMode", false);
        reportModelBuilderResult.put("metaData", null);
        reportModelBuilderResult.put("codeScanSupport", new HtmlCodeScanDescriptionSupport());
        reportModelBuilderResult.put("scanTypeCountSet", new TreeSet<ScanTypeCount>());

        when(modelBuilder.build(any())).thenReturn(reportModelBuilderResult);
    }

}
