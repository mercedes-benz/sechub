// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.product;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.commons.model.ScanType;
import com.mercedesbenz.sechub.commons.model.SecHubLicenseScanConfiguration;
import com.mercedesbenz.sechub.sharedkernel.UUIDTraceLogID;
import com.mercedesbenz.sechub.sharedkernel.configuration.SecHubConfiguration;
import com.mercedesbenz.sechub.sharedkernel.execution.SecHubExecutionContext;

@Service
public class LicenseScanProductExecutionServiceImpl extends AbstractProductExecutionService implements LicenseScanProductExecutionService {
	
	private static final Logger LOG = LoggerFactory.getLogger(LicenseScanProductExecutionServiceImpl.class);

    @Override
    protected ScanType getScanType() {
        return ScanType.LICENSE_SCAN;
    }

    @Override
    protected boolean isExecutionNecessary(SecHubExecutionContext context, UUIDTraceLogID traceLogID, SecHubConfiguration configuration) {
        Optional<SecHubLicenseScanConfiguration> licenseScanConfiguration = configuration.getLicenseScan();
        if (!licenseScanConfiguration.isPresent()) {
            LOG.trace("No license scan option found for {}", traceLogID);
            return false;
        }
        LOG.trace("License scan option found {}", traceLogID);
        return true;
    }

}
