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
import com.mercedesbenz.sechub.sharedkernel.usecases.user.execute.UseCaseUserDownloadsLatestJobReport;
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

    private static final String CONTENT_SECURITY_POLICY_HEADER = "Content-Security-Policy";
    private static final String SCRIPT_SRC_NONCE_HEADER_VALUE_FORMAT = "script-src 'nonce-%s' 'strict-dynamic';";

    @Autowired
    private HTMLScanResultReportModelBuilder htmlModelBuilder;

    @Autowired
    private DownloadScanReportService downloadReportService;

    @Autowired
    private DownloadSpdxScanReportService serecoSpdxDownloadService;

    // TODO: extend api tests to cover for themes etc.

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
            HttpServletResponse response,
			@PathVariable("projectId") String projectId,
			@PathVariable("jobUUID") UUID jobUUID,
            @RequestParam(value = "theme", required = false, defaultValue = DEFAULT_THEME) String theme) {
		/* @formatter:on */
        ScanSecHubReport scanSecHubReport = fetchObfuscatedScanSecHubReport(projectId, jobUUID);

        Map<String, Object> model = htmlModelBuilder.build(scanSecHubReport, theme);
        String nonce = generateNumberUsedOnce();
        setNonceHeader(response, nonce);
        model.put("nonce", nonce);
        return new ModelAndView("report/html/report", model);
    }

    /* @formatter:off */
    @UseCaseUserDownloadsLatestJobReport(@Step(number=2, next= {3}, name="REST API call to get latest HTML report", needsRestDoc=true))
    @RequestMapping(path = "/report", method = RequestMethod.GET, produces= {"application/xhtml+xml", "text/html","text/html;charset=UTF-8"})
    @ResponseBody
    public ModelAndView getLatestScanSecHubReportAsHTML(HttpServletResponse response,
                                                        @PathVariable("projectId") String projectId,
                                                        @RequestParam(value = "theme", required = false, defaultValue = DEFAULT_THEME) String theme) throws IOException {
        /* @formatter:on */
        // ScanSecHubReport scanSecHubReport =
        // fetchLatestObfuscatedScanSecHubReport(projectId);

        /**
         * For testing purposes, we use a default report. This is only for testing
         * FIXME: Remove this before merging
         */

        File file = ResourceUtils.getFile("classpath:defaultFullReport.json");
        ScanSecHubReport scanSecHubReport = new ObjectMapper().readValue(file, ScanSecHubReport.class);

        Map<String, Object> model = htmlModelBuilder.build(scanSecHubReport, theme);
        String nonce = generateNumberUsedOnce();
        setNonceHeader(response, nonce);
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

    private ScanSecHubReport fetchLatestObfuscatedScanSecHubReport(String projectId) {
        return downloadReportService.getLatestObfuscatedScanSecHubReport(projectId);
    }

    private ScanSecHubReport fetchObfuscatedScanSecHubReport(String projectId, UUID jobUUID) {
        return downloadReportService.getObfuscatedScanSecHubReport(projectId, jobUUID);
    }

    /**
     * Sets the number-used-once (nonce) header for the CSP to allow only trusted
     * inline scripts and styles.
     */
    private static void setNonceHeader(HttpServletResponse response, String nonce) {
        response.setHeader(CONTENT_SECURITY_POLICY_HEADER, SCRIPT_SRC_NONCE_HEADER_VALUE_FORMAT.formatted(nonce));
    }

    /**
     * Generates a number-used-once (nonce) for the CSP header using a random UUID.
     * The UUID is converted to a string and the hyphens are removed.
     */
    private static String generateNumberUsedOnce() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
