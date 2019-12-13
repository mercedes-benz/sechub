// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.daimler.sechub.domain.scan.access.ScanAccessCountService;
import com.daimler.sechub.domain.scan.product.ProductResultCountService;
import com.daimler.sechub.domain.scan.report.ScanReportCountService;
import com.daimler.sechub.sharedkernel.APIConstants;
import com.daimler.sechub.sharedkernel.Profiles;

/**
 * Contains additional rest call functionality for integration tests on scan domain
 *
 * @author Albert Tregnaghi
 *
 */
@RestController
@Profile(Profiles.INTEGRATIONTEST)
public class IntegrationTestScanRestController {

	@Autowired
	private ScanAccessCountService scanAccessCountService;

	@Autowired
	private ProductResultCountService productResultCountService;

	@Autowired
	private ScanReportCountService scanReportCountService;

	@RequestMapping(path = APIConstants.API_ANONYMOUS + "integrationtest/project/{projectId}/scan/access/count", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	public long countProjectAccess(@PathVariable("projectId") String projectId) {
		return scanAccessCountService.countProjectAccess(projectId);
	}


	@RequestMapping(path = APIConstants.API_ANONYMOUS + "integrationtest/project/{projectId}/scan/report/count", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	public long countProductResults(@PathVariable("projectId") String projectId) {
		return scanReportCountService.countProjectProductResults(projectId);
	}

	@RequestMapping(path = APIConstants.API_ANONYMOUS + "integrationtest/project/{projectId}/scan/productresult/count", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	public long countScanResults(@PathVariable("projectId") String projectId) {
		return productResultCountService.countProjectScanResults(projectId);
	}


}
