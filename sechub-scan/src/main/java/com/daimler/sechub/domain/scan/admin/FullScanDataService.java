// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.admin;

import java.util.List;
import java.util.UUID;

import javax.annotation.security.RolesAllowed;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.daimler.sechub.domain.scan.log.ProjectScanLog;
import com.daimler.sechub.domain.scan.log.ProjectScanLogService;
import com.daimler.sechub.domain.scan.product.ProductResult;
import com.daimler.sechub.domain.scan.product.ProductResultService;
import com.daimler.sechub.sharedkernel.RoleConstants;
import com.daimler.sechub.sharedkernel.Step;
import com.daimler.sechub.sharedkernel.usecases.admin.project.UseCaseAdministratorDownloadsFullScanDataForJob;
import com.daimler.sechub.sharedkernel.validation.UserInputAssertion;

@Service
@RolesAllowed(RoleConstants.ROLE_SUPERADMIN)
public class FullScanDataService {

	private static final Logger LOG = LoggerFactory.getLogger(FullScanDataService.class);

	@Autowired
	ProjectScanLogService projectScanLogService;

	@Autowired
	ProductResultService productResultService;

	@Autowired
	UserInputAssertion assertion;

	@UseCaseAdministratorDownloadsFullScanDataForJob(@Step(number=2, name="Collect all scan data"))
	public FullScanData getFullScanData(UUID sechubJobUUID) {
		assertion.isValidJobUUID(sechubJobUUID);

		LOG.debug("Start getting full scan data for {}",sechubJobUUID);

		FullScanData data = new FullScanData();
		data.sechubJobUUID=sechubJobUUID;

		List<ProjectScanLog> logs = projectScanLogService.fetchLogsForJob(sechubJobUUID);
		data.allScanLogs.addAll(logs);

		List<ProductResult> results = productResultService.fetchAllResultsForJob(sechubJobUUID);
		for (ProductResult result:results) {
			ScanData scanData = new ScanData();
			scanData.productId=result.getProductIdentifier().toString();
			scanData.executorConfigUUID=result.getProductExecutorConfigUUID();
			
			scanData.result=result.getResult();
			
			scanData.metaData=result.getMetaData();
			
			data.allScanData.add(scanData);
			
		}
		return data;
	}

}
