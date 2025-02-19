// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.report;

import static com.mercedesbenz.sechub.domain.scan.report.HTMLScanResultReportModelBuilder.DEFAULT_THEME;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.http.MediaType;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mercedesbenz.sechub.sharedkernel.Step;
import com.mercedesbenz.sechub.sharedkernel.security.APIConstants;
import com.mercedesbenz.sechub.sharedkernel.security.RoleConstants;
import com.mercedesbenz.sechub.sharedkernel.usecases.user.execute.UseCaseUserDownloadsJobReport;
import com.mercedesbenz.sechub.sharedkernel.usecases.user.execute.UseCaseUserDownloadsSpdxJobReport;
import com.mercedesbenz.sechub.sharedkernel.usecases.user.execute.UseCaseUserStartsSynchronousScanByClient;

import jakarta.annotation.security.RolesAllowed;
import jakarta.servlet.http.HttpServletResponse;

/**
 * The rest API for job scheduling. It shall be same obvious like
 * https://developer.github.com/v3/issues/labels/
 *
 * @author Albert Tregnaghi
 *
 */
@RestController
@EnableAutoConfiguration
@RequestMapping(APIConstants.API_PROJECT + "{projectId}") // API like https://developer.github.com/v3/issues/labels/#create-a-label
@RolesAllowed({ RoleConstants.ROLE_USER, RoleConstants.ROLE_SUPERADMIN })
public class ScanReportRestController {

    @Autowired
    private HTMLScanResultReportModelBuilder htmlModelBuilder;

    @Autowired
    private DownloadScanReportService downloadReportService;

    @Autowired
    private DownloadSpdxScanReportService serecoSpdxDownloadService;

    /* @formatter:off */
	@UseCaseUserDownloadsJobReport(@Step(number=1, next= {3}, name="REST API call to get JSON report", needsRestDoc=true))
	@UseCaseUserStartsSynchronousScanByClient(@Step(number=4, name="download job report and traffic light"))
	@RequestMapping(path = "/report/{jobUUID}", method = RequestMethod.GET, produces= {MediaType.APPLICATION_JSON_VALUE})
	public ScanSecHubReport getScanSecHubReportAsJSON(
			@PathVariable("projectId") String projectId,
			@PathVariable("jobUUID") UUID jobUUID
			) {
		/* @formatter:on */
        return fetchObfuscatedScanSecHubReport(projectId, jobUUID);

    }

    /* @formatter:off */
	@UseCaseUserDownloadsJobReport(@Step(number=2, next= {3}, name="REST API call to get HTML report", needsRestDoc=true))
	@RequestMapping(path = "/report/{jobUUID}", method = RequestMethod.GET, produces= {"application/xhtml+xml", "text/html","text/html;charset=UTF-8"})
	@ResponseBody
	public ModelAndView getScanSecHubReportAsHTML(
			@PathVariable("projectId") String projectId,
			@PathVariable("jobUUID") UUID jobUUID,
            @RequestParam(value = "theme", required = false, defaultValue = DEFAULT_THEME) String theme
			) {
		/* @formatter:on */
        ScanSecHubReport scanSecHubReport = fetchObfuscatedScanSecHubReport(projectId, jobUUID);

        Map<String, Object> model = htmlModelBuilder.build(scanSecHubReport, theme);
        return new ModelAndView("report/html/report", model);
    }

    /* @formatter:off */
    @UseCaseUserDownloadsJobReport(@Step(number=2, next= {3}, name="REST API call to get HTML report", needsRestDoc=true))
    @RequestMapping(path = "/report", method = RequestMethod.GET, produces= {"application/xhtml+xml", "text/html","text/html;charset=UTF-8"})
    @ResponseBody
    public ModelAndView getScanSecHubReportAsHTML(HttpServletResponse response,
                                                  @PathVariable("projectId") String projectId,
                                                  @RequestParam(value = "theme", required = false, defaultValue = DEFAULT_THEME) String theme)throws IOException {
        /* @formatter:on */
        // ScanSecHubReport scanSecHubReport =
        // fetchObfuscatedScanSecHubReport(projectId);

        ScanSecHubReport scanSecHubReport;

        File file = ResourceUtils.getFile("classpath:defaultFullReport.json");
        scanSecHubReport = new ObjectMapper().readValue(file, ScanSecHubReport.class);
        Map<String, Object> model = htmlModelBuilder.build(scanSecHubReport, theme);

        String nonce = UUID.randomUUID().toString().replace("-", ""); // Generate a unique nonce
        response.setHeader("Content-Security-Policy", "script-src 'nonce-" + nonce + "' 'strict-dynamic';");

        model.put("nonce", nonce);

        return new ModelAndView("report/html/report", model);
    }

    /* @formatter:off */
	@UseCaseUserDownloadsSpdxJobReport(@Step(number=1,name="REST API call to get SPDX JSON report", needsRestDoc=true))
	@RequestMapping(path = "/report/spdx/{jobUUID}", method = RequestMethod.GET, produces= {MediaType.APPLICATION_JSON_VALUE})
	@ResponseBody
	public String getScanSecHubReportAsSpdxJson(
			@PathVariable("projectId") String projectId,
			@PathVariable("jobUUID") UUID jobUUID
			) {
		/* @formatter:on */
        String spdxDocument = serecoSpdxDownloadService.getScanSpdxJsonReport(projectId, jobUUID);

        return spdxDocument;
    }

    private ScanSecHubReport fetchObfuscatedScanSecHubReport(String projectId) {
        return downloadReportService.getObfuscatedScanSecHubReport(projectId);
    }

    private ScanSecHubReport fetchObfuscatedScanSecHubReport(String projectId, UUID jobUUID) {
        return downloadReportService.getObfuscatedScanSecHubReport(projectId, jobUUID);
    }

    private static final String defaultReport = """
            {
              "result" : {
                "count" : 0,
                "findings" : [ ]
              },
              "jobUUID" : "b1928324-b240-4e78-b8ac-7afe89ce421e",
              "reportVersion" : "1.0",
              "messages" : [ {
                "type" : "WARNING",
                "text" : "No results from a security product available for this job!"
              } ],
              "trafficLight" : "OFF",
              "metaData" : {
                "labels" : { },
                "summary" : { }
              },
              "status" : "SUCCESS"
            }
            """;
}
