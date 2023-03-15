// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.product;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.commons.model.ScanType;
import com.mercedesbenz.sechub.commons.model.SecHubCodeScanConfiguration;
import com.mercedesbenz.sechub.sharedkernel.UUIDTraceLogID;
import com.mercedesbenz.sechub.sharedkernel.configuration.SecHubConfiguration;
import com.mercedesbenz.sechub.sharedkernel.execution.SecHubExecutionContext;

/**
 * This service executes all registered product executors having scan type
 * {@link ScanType#CODE_SCAN}
 *
 * @author Albert Tregnaghi
 *
 */
@Service
public class CodeScanProductExecutionServiceImpl extends AbstractProductExecutionService implements CodeScanProductExecutionService {

    private static final Logger LOG = LoggerFactory.getLogger(CodeScanProductExecutionServiceImpl.class);

    @Override
    protected ScanType getScanType() {
        return ScanType.CODE_SCAN;
    }

    public boolean isExecutionNecessary(SecHubExecutionContext context, UUIDTraceLogID traceLogID, SecHubConfiguration configuration) {
        Optional<SecHubCodeScanConfiguration> codeScanOption = configuration.getCodeScan();
        if (!codeScanOption.isPresent()) {
            LOG.trace("No codescan options found for {}", traceLogID);
            return false;
        }
        LOG.trace("Code scan options found {}", traceLogID);
        return true;
    }

}
