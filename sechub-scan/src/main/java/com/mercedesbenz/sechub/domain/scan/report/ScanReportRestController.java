// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.report;

import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import com.mercedesbenz.sechub.sharedkernel.APIConstants;
import com.mercedesbenz.sechub.sharedkernel.RoleConstants;
import com.mercedesbenz.sechub.sharedkernel.Step;
import com.mercedesbenz.sechub.sharedkernel.usecases.user.execute.UseCaseUserDownloadsJobReport;
import com.mercedesbenz.sechub.sharedkernel.usecases.user.execute.UseCaseUserDownloadsSpdxJobReport;
import com.mercedesbenz.sechub.sharedkernel.usecases.user.execute.UseCaseUserStartsSynchronousScanByClient;

import jakarta.annotation.security.RolesAllowed;

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
			@PathVariable("jobUUID") UUID jobUUID
			) {
		/* @formatter:on */
        ScanSecHubReport scanSecHubReport = fetchObfuscatedScanSecHubReport(projectId, jobUUID);

        Map<String, Object> model = htmlModelBuilder.build(scanSecHubReport);
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

    private ScanSecHubReport fetchObfuscatedScanSecHubReport(String projectId, UUID jobUUID) {
        return downloadReportService.getObfuscatedScanSecHubReport(projectId, jobUUID);
    }

}
