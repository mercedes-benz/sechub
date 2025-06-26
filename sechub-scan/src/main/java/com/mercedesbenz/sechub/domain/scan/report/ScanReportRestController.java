// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.report;

import static com.mercedesbenz.sechub.domain.scan.report.HTMLScanResultReportModelBuilder.DEFAULT_THEME;
import static java.util.Objects.requireNonNull;

import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import com.mercedesbenz.sechub.sharedkernel.Step;
import com.mercedesbenz.sechub.sharedkernel.security.APIConstants;
import com.mercedesbenz.sechub.sharedkernel.security.RoleConstants;
import com.mercedesbenz.sechub.sharedkernel.usecases.user.execute.UseCaseUserDownloadsJobReport;
import com.mercedesbenz.sechub.sharedkernel.usecases.user.execute.UseCaseUserDownloadsSpdxJobReport;
import com.mercedesbenz.sechub.sharedkernel.usecases.user.execute.UseCaseUserStartsSynchronousScanByClient;

import jakarta.annotation.security.RolesAllowed;
import jakarta.servlet.http.HttpServletResponse;

/**
 * The API for job scheduling. It shall be same obvious like
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
    public ScanSecHubReport getScanSecHubReportAsJSON(@PathVariable("projectId") String projectId,
                                                      @PathVariable("jobUUID") UUID jobUUID) {
        /* @formatter:on */
        return fetchObfuscatedScanSecHubReport(projectId, jobUUID);
    }

    /* @formatter:off */
	@UseCaseUserDownloadsJobReport(@Step(number=2, next= {3}, name="REST API call to get HTML report", needsRestDoc=true))
	@RequestMapping(path = "/report/{jobUUID}", method = RequestMethod.GET, produces= {"application/xhtml+xml", "text/html","text/html;charset=UTF-8"})
	public ModelAndView getScanSecHubReportAsHTML(HttpServletResponse response,
                                                  @PathVariable("projectId") String projectId,
                                                  @PathVariable("jobUUID") UUID jobUUID,
                                                  @RequestParam(value = "interactive", required = false, defaultValue = "false") boolean interactive,
                                                  @RequestParam(value = "theme", required = false, defaultValue = DEFAULT_THEME) String theme) {
		/* @formatter:on */
        ScanSecHubReport scanSecHubReport = fetchObfuscatedScanSecHubReport(projectId, jobUUID);

        if (interactive) {
            String nonce = generateNumberUsedOnce();
            setNumberUsedOnceHeader(response, nonce);
            return createInteractiveHtmlReport(scanSecHubReport, interactive, theme, nonce);
        }

        return createHtmlReport(scanSecHubReport, theme);
    }

    /* @formatter:off */
	@UseCaseUserDownloadsSpdxJobReport(@Step(number=1,name="REST API call to get SPDX JSON report", needsRestDoc=true))
	@RequestMapping(path = "/report/spdx/{jobUUID}", method = RequestMethod.GET, produces= {MediaType.APPLICATION_JSON_VALUE})
	public String getScanSecHubReportAsSpdxJson(
			@PathVariable("projectId") String projectId,
			@PathVariable("jobUUID") UUID jobUUID
			) {
		/* @formatter:on */
        String spdxDocument = serecoSpdxDownloadService.getScanSpdxJsonReport(projectId, jobUUID);

        return spdxDocument;
    }

    private ScanSecHubReport fetchObfuscatedScanSecHubReport(String projectId, UUID jobUUID) {
        return downloadReportService.getObfuscatedScanSecHubReport(projectId, jobUUID);
    }

    /**
     * Generates a number-used-once (nonce) for the CSP header using a random UUID.
     * The UUID is converted to a string and the hyphens are removed.
     */
    private static String generateNumberUsedOnce() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * Sets the Content Security Policy header with a nonce for script sources. This
     * is used to allow inline scripts that have the same nonce value.
     *
     * @param response the HTTP response to set the header on
     * @param nonce    the nonce value to use in the header
     */
    private static void setNumberUsedOnceHeader(HttpServletResponse response, String nonce) {
        requireNonNull(response, "Parameter 'response' must not be null");
        requireNonNull(nonce, "Parameter 'nonce' must not be null");

        response.setHeader(CONTENT_SECURITY_POLICY_HEADER, SCRIPT_SRC_NONCE_HEADER_VALUE_FORMAT.formatted(nonce));
    }

    private ModelAndView createHtmlReport(ScanSecHubReport scanSecHubReport, String theme) {
        requireNonNull(scanSecHubReport, "Parameter 'scanSecHubReport' may not be null");
        requireNonNull(theme, "Parameter 'theme' must not be null");

        Map<String, Object> model = htmlModelBuilder.build(scanSecHubReport, theme);
        return new ModelAndView("report/html/report", model);
    }

    private ModelAndView createInteractiveHtmlReport(ScanSecHubReport scanSecHubReport, boolean interactive, String theme, String nonce) {
        requireNonNull(scanSecHubReport, "Parameter 'scanSecHubReport' may not be null");
        requireNonNull(theme, "Parameter 'theme' must not be null");
        requireNonNull(nonce, "Parameter 'nonce' must not be null for interactive HTML report");

        Map<String, Object> model = htmlModelBuilder.buildInteractiveReport(scanSecHubReport, theme, nonce);
        return new ModelAndView("report/html/report", model);
    }

}
