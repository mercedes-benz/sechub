// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.product;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.commons.model.ScanType;
import com.mercedesbenz.sechub.commons.model.SecHubIacScanConfiguration;
import com.mercedesbenz.sechub.domain.scan.SecHubExecutionContext;
import com.mercedesbenz.sechub.sharedkernel.UUIDTraceLogID;
import com.mercedesbenz.sechub.sharedkernel.configuration.SecHubConfiguration;

/**
 * This service executes all registered product executors having scan type
 * {@link ScanType#IAC_SCAN}
 *
 * @author Albert Tregnaghi
 *
 */
@Service
public class IacScanProductExecutionServiceImpl extends AbstractProductExecutionService implements IacScanProductExecutionService {

    private static final Logger LOG = LoggerFactory.getLogger(IacScanProductExecutionServiceImpl.class);

    public boolean isExecutionNecessary(SecHubExecutionContext context, UUIDTraceLogID traceLogID, SecHubConfiguration configuration) {
        Optional<SecHubIacScanConfiguration> iacScanOption = configuration.getIacScan();
        if (!iacScanOption.isPresent()) {
            LOG.trace("No iac scan options found for {}", traceLogID);
            return false;
        }
        LOG.trace("Iac scan options found {}", traceLogID);
        return true;
    }

}
