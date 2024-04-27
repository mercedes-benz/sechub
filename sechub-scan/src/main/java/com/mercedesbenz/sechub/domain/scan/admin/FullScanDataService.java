// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.admin;

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.domain.scan.log.ProjectScanLog;
import com.mercedesbenz.sechub.domain.scan.log.ProjectScanLogService;
import com.mercedesbenz.sechub.domain.scan.product.ProductResult;
import com.mercedesbenz.sechub.domain.scan.product.ProductResultService;
import com.mercedesbenz.sechub.sharedkernel.RoleConstants;
import com.mercedesbenz.sechub.sharedkernel.Step;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.project.UseCaseAdminDownloadsFullScanDataForJob;
import com.mercedesbenz.sechub.sharedkernel.validation.UserInputAssertion;

import jakarta.annotation.security.RolesAllowed;

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

    @UseCaseAdminDownloadsFullScanDataForJob(@Step(number = 2, name = "Collect all scan data"))
    public FullScanData getFullScanData(UUID sechubJobUUID) {
        assertion.assertIsValidJobUUID(sechubJobUUID);

        LOG.debug("Start getting full scan data for {}", sechubJobUUID);

        FullScanData data = new FullScanData();
        data.sechubJobUUID = sechubJobUUID;

        List<ProjectScanLog> logs = projectScanLogService.fetchLogsForJob(sechubJobUUID);
        data.allScanLogs.addAll(logs);

        List<ProductResult> results = productResultService.fetchAllResultsForJob(sechubJobUUID);
        for (ProductResult result : results) {
            ScanData scanData = new ScanData();
            scanData.productId = result.getProductIdentifier().toString();
            scanData.executorConfigUUID = result.getProductExecutorConfigUUID();

            scanData.result = result.getResult();

            scanData.metaData = result.getMetaData();

            data.allScanData.add(scanData);

        }
        return data;
    }

}
