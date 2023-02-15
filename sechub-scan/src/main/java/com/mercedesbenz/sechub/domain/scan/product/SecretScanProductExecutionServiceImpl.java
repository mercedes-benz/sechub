// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.product;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.commons.model.ScanType;
import com.mercedesbenz.sechub.commons.model.SecHubSecretScanConfiguration;
import com.mercedesbenz.sechub.domain.scan.SecHubExecutionContext;
import com.mercedesbenz.sechub.sharedkernel.UUIDTraceLogID;
import com.mercedesbenz.sechub.sharedkernel.configuration.SecHubConfiguration;

@Service
public class SecretScanProductExecutionServiceImpl extends AbstractProductExecutionService implements SecretScanProductExecutionService {

    private static final Logger LOG = LoggerFactory.getLogger(SecretScanProductExecutionServiceImpl.class);

    @Override
    protected ScanType getScanType() {
        return ScanType.SECRET_SCAN;
    }

    @Override
    protected boolean isExecutionNecessary(SecHubExecutionContext context, UUIDTraceLogID traceLogID, SecHubConfiguration configuration) {
        Optional<SecHubSecretScanConfiguration> secretScanConfiguration = configuration.getSecretScan();
        if (!secretScanConfiguration.isPresent()) {
            LOG.trace("No secrets scan option found for {}", traceLogID);
            return false;
        }
        LOG.trace("Secret scan option found {}", traceLogID);
        return true;
    }

}
