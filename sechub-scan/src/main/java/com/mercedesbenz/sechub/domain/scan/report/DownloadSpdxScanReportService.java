// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.report;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.commons.model.SecHubRuntimeException;
import com.mercedesbenz.sechub.domain.scan.ScanAssertService;
import com.mercedesbenz.sechub.domain.scan.product.ProductIdentifier;
import com.mercedesbenz.sechub.domain.scan.product.ProductResult;
import com.mercedesbenz.sechub.domain.scan.product.ProductResultRepository;
import com.mercedesbenz.sechub.domain.scan.resolve.ProductResultSpdxJsonResolver;
import com.mercedesbenz.sechub.sharedkernel.error.NotFoundException;
import com.mercedesbenz.sechub.sharedkernel.logging.AuditLogService;
import com.mercedesbenz.sechub.sharedkernel.validation.UserInputAssertion;

@Service
public class DownloadSpdxScanReportService {

    @Autowired
    ScanAssertService scanAssertService;

    @Autowired
    UserInputAssertion assertion;

    @Autowired
    AuditLogService auditLogService;

    @Autowired
    ProductResultRepository productResultRepository;

    @Autowired
    ProductResultSpdxJsonResolver spdxJsonResolver;

    public String getScanSpdxJsonReport(String projectId, UUID jobUUID) {
        /* validate */
        assertion.assertIsValidProjectId(projectId);
        assertion.assertIsValidJobUUID(jobUUID);

        scanAssertService.assertUserHasAccessToProject(projectId);
        scanAssertService.assertProjectAllowsReadAccess(projectId);

        /* audit */
        auditLogService.log("starts download of SPDX Json report for job: {}", jobUUID);

        List<ProductResult> productResults = productResultRepository.findAllProductResults(jobUUID, ProductIdentifier.SERECO);

        if (productResults.size() != 1) {
            throw new SecHubRuntimeException(
                    "Did not found exactly one SERECO product result. Instead, " + productResults.size() + " product results were found.");
        }

        ProductResult productResult = productResults.iterator().next();
        String spdxJson = spdxJsonResolver.resolveSpdxJson(productResult);

        if (spdxJson == null) {
            throw new NotFoundException("There was no JSON SPDX report available for job: " + jobUUID);
        }

        return spdxJson;
    }
}
