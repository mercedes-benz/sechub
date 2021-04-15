// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan;

import static com.daimler.sechub.test.TestURLBuilder.*;
import static org.hamcrest.CoreMatchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import com.daimler.sechub.commons.model.SecHubFinding;
import com.daimler.sechub.commons.model.TrafficLight;
import com.daimler.sechub.domain.scan.product.ReportProductExecutionService;
import com.daimler.sechub.domain.scan.report.CreateScanReportService;
import com.daimler.sechub.domain.scan.report.DownloadScanReportService;
import com.daimler.sechub.domain.scan.report.ScanReport;
import com.daimler.sechub.domain.scan.report.ScanReportRepository;
import com.daimler.sechub.domain.scan.report.ScanReportRestController;
import com.daimler.sechub.domain.scan.report.ScanReportResult;
import com.daimler.sechub.domain.scan.report.ScanReportTrafficLightCalculator;
import com.daimler.sechub.test.TestPortProvider;

@RunWith(SpringRunner.class)
@WebMvcTest(ScanReportRestController.class)
@ContextConfiguration(classes = { ScanReportRestController.class, ScanReportRestControllerMockTest.SimpleTestConfiguration.class })
public class ScanReportRestControllerMockTest {

    private static final String PROJECT1_ID = "project1";

    private static final int PORT_USED = TestPortProvider.DEFAULT_INSTANCE.getWebMVCTestHTTPSPort();

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CreateScanReportService createReportService;

    @MockBean
    private DownloadScanReportService downloadReportService;

    @MockBean
    SecHubResultService secHubResultService;

    @MockBean
    ReportProductExecutionService reportProductExecutionService;

    @MockBean
    ScanReportTrafficLightCalculator trafficLightCalculator;

    @MockBean
    ScanReportRepository reportRepository;

    @MockBean
    HTMLScanResultReportModelBuilder modelBuilder;

    private UUID randomUUID;

    @Test
    @WithMockUser
    public void get_report_from_existing_job_returns_information_as_json_when_type_is_APPLICATION_JSON_UTF8() throws Exception {
        internalTestAcceptedAndReturnsJSON(MediaType.APPLICATION_JSON);
    }

    @Test
    @WithMockUser
    public void get_report_from_existing_job_returns_406_NOT_ACCEPTABLE__when_type_is_APPLICATION_PDF() throws Exception {
        /* prepare */
        ScanReport report = new ScanReport(randomUUID, PROJECT1_ID);
        report.setResult("{'count':'1'}");
        report.setTrafficLight(TrafficLight.YELLOW);

        ScanReportResult result1 = new ScanReportResult(report);
        when(downloadReportService.getScanReportResult(PROJECT1_ID, randomUUID)).thenReturn(result1);

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
    public void get_report_from_existing_job_returns_information_as_html_when_type_is_APPLICATION_XHTML_XML() throws Exception {
        internalTestAcceptedAndReturnsHTML(MediaType.APPLICATION_XHTML_XML);
    }

    @Test
    @WithMockUser
    public void get_report_from_existing_job_returns_information_as_html_when_type_is_TEXT_HTML() throws Exception {
        internalTestAcceptedAndReturnsHTML(MediaType.TEXT_HTML);
    }

    @Test
    @WithMockUser
    public void get_html_report_with_cwe_id() throws Exception {
        /* prepare */

        Integer cweId = Integer.valueOf(77);

        SecHubFinding finding = new SecHubFinding();
        finding.setCweId(cweId);

        Map<String, Object> map = new HashMap<>();
        map.put("jobuuid", randomUUID);
        map.put("styleRed", "theRedStyle");
        map.put("styleGreen", "display:none");
        map.put("styleYellow", "display:none");
        map.put("redList", Arrays.asList(finding));
        map.put("yellowList", new ArrayList<>());
        map.put("greenList", new ArrayList<>());
        map.put("isWebDesignMode", false);
        map.put("codeScanSupport", new HtmlCodeScanDescriptionSupport());
        map.put("codeScanEntries", new ArrayList<>());

        when(modelBuilder.build(any())).thenReturn(map);

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
    public void get_html_report_without_cwe_id() throws Exception {
        /* prepare */
        
        Map<String, Object> map = new HashMap<>();
        map.put("jobuuid", randomUUID);
        map.put("styleRed", "theRedStyle");
        map.put("styleGreen", "display:none");
        map.put("styleYellow", "display:none");
        map.put("redList", Arrays.asList(new SecHubFinding()));
        map.put("yellowList", new ArrayList<>());
        map.put("greenList", new ArrayList<>());
        map.put("isWebDesignMode", false);
        map.put("codeScanSupport", new HtmlCodeScanDescriptionSupport());
        map.put("codeScanEntries", new ArrayList<>());

        when(modelBuilder.build(any())).thenReturn(map);

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

    private void internalTestAcceptedAndReturnsJSON(MediaType acceptedType) throws Exception {
        /* prepare */
        ScanReport report = new ScanReport(randomUUID, PROJECT1_ID);
        report.setResult("{'count':'1'}");
        report.setTrafficLight(TrafficLight.YELLOW);

        ScanReportResult result1 = new ScanReportResult(report);
        when(downloadReportService.getScanReportResult(PROJECT1_ID, randomUUID)).thenReturn(result1);

        /* execute + test @formatter:off */
	    this.mockMvc.perform(
	    		get(https(PORT_USED).buildGetJobReportUrl(PROJECT1_ID,randomUUID)).accept(acceptedType).
	    			contentType(MediaType.APPLICATION_JSON_VALUE)
	    		).
	    			andExpect(status().isOk()).
	    			andExpect(content().json("{\"jobUUID\":\""+randomUUID.toString()+"\",\"result\":{\"count\":1,\"findings\":[]},\"trafficLight\":\"YELLOW\"}")
	    		);

	    /* @formatter:on */
    }

    private void internalTestAcceptedAndReturnsHTML(MediaType acceptedType) throws Exception {
        /* prepare */
        ScanReport report = new ScanReport(randomUUID, PROJECT1_ID);
        report.setResult("{'count':'1'}");
        report.setTrafficLight(TrafficLight.YELLOW);

        ScanReportResult result1 = new ScanReportResult(report);
        when(downloadReportService.getScanReportResult(PROJECT1_ID, randomUUID)).thenReturn(result1);

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

    @Before
    public void before() throws Exception {
        randomUUID = UUID.randomUUID();
        Map<String, Object> map = new HashMap<>();
        map.put("jobuuid", randomUUID);
        map.put("styleRed", "theRedStyle");
        map.put("styleGreen", "display:none");
        map.put("styleYellow", "display:none");
        map.put("redList", new ArrayList<>());
        map.put("yellowList", new ArrayList<>());
        map.put("greenList", new ArrayList<>());
        map.put("isWebDesignMode", false);
        when(modelBuilder.build(any())).thenReturn(map);
    }

}
