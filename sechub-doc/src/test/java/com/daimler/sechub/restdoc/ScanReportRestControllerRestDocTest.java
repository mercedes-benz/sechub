// SPDX-License-Identifier: MIT
package com.daimler.sechub.restdoc;
import static com.daimler.sechub.test.TestURLBuilder.*;
import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.*;
import static org.hamcrest.CoreMatchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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

import com.daimler.sechub.commons.model.TrafficLight;
import com.daimler.sechub.docgen.util.RestDocPathFactory;
import com.daimler.sechub.domain.scan.HTMLScanResultReportModelBuilder;
import com.daimler.sechub.domain.scan.report.DownloadScanReportService;
import com.daimler.sechub.domain.scan.report.ScanReport;
import com.daimler.sechub.domain.scan.report.ScanReportRestController;
import com.daimler.sechub.domain.scan.report.ScanReportResult;
import com.daimler.sechub.sharedkernel.usecases.UseCaseRestDoc;
import com.daimler.sechub.sharedkernel.usecases.user.execute.UseCaseUserDownloadsJobReport;
import com.daimler.sechub.test.ExampleConstants;
import com.daimler.sechub.test.TestPortProvider;

@RunWith(SpringRunner.class)
@WebMvcTest(ScanReportRestController.class)
@ContextConfiguration(classes= {ScanReportRestController.class, ScanReportRestControllerRestDocTest.SimpleTestConfiguration.class})
@AutoConfigureRestDocs(uriScheme="https",uriHost=ExampleConstants.URI_SECHUB_SERVER,uriPort=443)
public class ScanReportRestControllerRestDocTest {

	private static final String PROJECT1_ID = "project1";

	private static final int PORT_USED = TestPortProvider.DEFAULT_INSTANCE.getRestDocTestPort();

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private DownloadScanReportService downloadReportService;

	@MockBean
	HTMLScanResultReportModelBuilder modelBuilder;

	private UUID randomUUID;

	@UseCaseRestDoc(useCase=UseCaseUserDownloadsJobReport.class,variant="JSON")
	@Test
	@WithMockUser
	public void get_report_from_existing_job_returns_information_as_json_when_type_is_APPLICATION_JSON_UTF8() throws Exception {
		/* prepare */
		ScanReport report = new ScanReport(randomUUID,PROJECT1_ID);
		report.setResult("{'count':'1'}");
		report.setTrafficLight(TrafficLight.YELLOW);

		ScanReportResult result1 = new ScanReportResult(report);
		when(downloadReportService.getScanReportResult(PROJECT1_ID, randomUUID)).thenReturn(result1);

		/* execute + test @formatter:off */
	    this.mockMvc.perform(
	    		get(https(PORT_USED).buildGetJobReportUrl(PROJECT1_ID,randomUUID)).accept(MediaType.APPLICATION_JSON_VALUE).
	    			contentType(MediaType.APPLICATION_JSON_VALUE)
	    		).
	    			andExpect(status().isOk()).
	    			andExpect(content().json("{\"jobUUID\":\""+randomUUID.toString()+"\",\"result\":{\"count\":1,\"findings\":[]},\"trafficLight\":\"YELLOW\"}")).

	    			andDo(document(RestDocPathFactory.createPath(UseCaseUserDownloadsJobReport.class, "JSON"))

	    					);

	    /* @formatter:on */
	}

	@UseCaseRestDoc(useCase=UseCaseUserDownloadsJobReport.class,variant="HTML")
	@Test
	@WithMockUser
	public void get_report_from_existing_job_returns_information_as_html_when_type_is_APPLICATION_XHTML_XML() throws Exception {
		/* prepare */
		ScanReport report = new ScanReport(randomUUID,PROJECT1_ID);
		report.setResult("{'count':'1'}");
		report.setTrafficLight(TrafficLight.YELLOW);

		ScanReportResult result1 = new ScanReportResult(report);
		when(downloadReportService.getScanReportResult(PROJECT1_ID, randomUUID)).thenReturn(result1);

		/* execute + test @formatter:off */
        this.mockMvc.perform(
        		get(https(PORT_USED).buildGetJobReportUrl(PROJECT1_ID,randomUUID)).accept(MediaType.APPLICATION_XHTML_XML).
        			contentType(MediaType.APPLICATION_JSON_VALUE)
        		).  
        			andExpect(status().isOk()).
        			andExpect(content().contentType("text/html;charset=UTF-8")).
        			andExpect(content().encoding("UTF-8")).
        			andExpect(content().string(containsString(randomUUID.toString()))).
        			andExpect(content().string(containsString("theRedStyle"))).

        			andDo(document(RestDocPathFactory.createPath(UseCaseUserDownloadsJobReport.class, "HTML"))

        		);

        /* @formatter:on */
	}

	@TestConfiguration
	@EnableAutoConfiguration
	public static class SimpleTestConfiguration{

	}

	@Before
	public void before() throws Exception {
		randomUUID=UUID.randomUUID();
		Map<String,Object> map = new HashMap<>();
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
