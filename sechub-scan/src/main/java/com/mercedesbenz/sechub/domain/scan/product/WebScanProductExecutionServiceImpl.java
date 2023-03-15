// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.product;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.commons.model.ScanType;
import com.mercedesbenz.sechub.commons.model.SecHubWebScanConfiguration;
import com.mercedesbenz.sechub.domain.scan.SecHubExecutionContext;
import com.mercedesbenz.sechub.sharedkernel.UUIDTraceLogID;
import com.mercedesbenz.sechub.sharedkernel.configuration.SecHubConfiguration;

/**
 * This service executes all registered product executors having scan type
 * {@link ScanType#WEB_SCAN}
 *
 * @author Albert Tregnaghi
 *
 */
@Service
public class WebScanProductExecutionServiceImpl extends AbstractProductExecutionService implements WebScanProductExecutionService {

    private static final Logger LOG = LoggerFactory.getLogger(WebScanProductExecutionServiceImpl.class);

    public boolean isExecutionNecessary(SecHubExecutionContext context, UUIDTraceLogID traceLogID, SecHubConfiguration configuration) {
        Optional<SecHubWebScanConfiguration> webScanOption = configuration.getWebScan();
        if (!webScanOption.isPresent()) {
            LOG.trace("No webscan options found for {}", traceLogID);
            return false;
        }
        LOG.trace("Web scan options found {}", traceLogID);
        return true;
    }

}
