// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.product;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.commons.model.ScanType;
import com.mercedesbenz.sechub.commons.model.SecHubCodeScanConfiguration;
import com.mercedesbenz.sechub.commons.model.SecHubLicenseScanConfiguration;
import com.mercedesbenz.sechub.domain.scan.SecHubExecutionContext;
import com.mercedesbenz.sechub.sharedkernel.UUIDTraceLogID;
import com.mercedesbenz.sechub.sharedkernel.configuration.SecHubConfiguration;

/**
 * This service executes all registered product executors having scan type
 * {@link ScanType#ANALYTICS}
 *
 * @author Albert Tregnaghi
 *
 */
@Service
public class AnalyticsProductExecutionServiceImpl extends AbstractProductExecutionService implements AnalyticsProductExecutionService {

    private static final Logger LOG = LoggerFactory.getLogger(AnalyticsProductExecutionServiceImpl.class);

    @Override
    protected void afterProductResultsStored(List<ProductResult> productResults, SecHubExecutionContext context) {
        LOG.debug("{} analytics product results stored.", productResults.size());
    }

    public boolean isExecutionNecessary(SecHubExecutionContext context, UUIDTraceLogID traceLogID, SecHubConfiguration configuration) {

        Optional<SecHubCodeScanConfiguration> codeScanOption = configuration.getCodeScan();
        Optional<SecHubLicenseScanConfiguration> licenseScan = configuration.getLicenseScan();

        if (codeScanOption.isPresent() || licenseScan.isPresent()) {
            return true;
        }
        return false;
    }

}
