// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.product;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.commons.model.ScanType;
import com.mercedesbenz.sechub.commons.model.SecHubInfrastructureScanConfiguration;
import com.mercedesbenz.sechub.domain.scan.SecHubExecutionContext;
import com.mercedesbenz.sechub.sharedkernel.UUIDTraceLogID;
import com.mercedesbenz.sechub.sharedkernel.configuration.SecHubConfiguration;

/**
 * This service executes all registered product executors having scan type
 * {@link ScanType#INFRA_SCAN}
 *
 * @author Albert Tregnaghi
 *
 */
@Service
public class InfrastructureScanProductExecutionServiceImpl extends AbstractProductExecutionService implements InfrastructureScanProductExecutionService {

    private static final Logger LOG = LoggerFactory.getLogger(InfrastructureScanProductExecutionServiceImpl.class);

    @Override
    protected ScanType getScanType() {
        return ScanType.INFRA_SCAN;
    }

    public boolean isExecutionNecessary(SecHubExecutionContext context, UUIDTraceLogID traceLogID, SecHubConfiguration configuration) {
        Optional<SecHubInfrastructureScanConfiguration> infraScanConfiguration = configuration.getInfraScan();
        if (!infraScanConfiguration.isPresent()) {
            LOG.trace("No infrastructure options found for {}", traceLogID);
            return false;
        }
        LOG.trace("Infrastructure scan options found {}", traceLogID);
        return true;
    }

}
