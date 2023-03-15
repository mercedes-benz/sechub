// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.product;

import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.commons.model.ScanType;
import com.mercedesbenz.sechub.domain.scan.SecHubExecutionContext;
import com.mercedesbenz.sechub.sharedkernel.UUIDTraceLogID;
import com.mercedesbenz.sechub.sharedkernel.configuration.SecHubConfiguration;

/**
 * This service executes all registered product executors having scan type
 * {@link ScanType#REPORT}
 *
 * @author Albert Tregnaghi
 *
 */
@Service
public class ReportProductExecutionServiceImpl extends AbstractProductExecutionService implements ReportProductExecutionService {

    public boolean isExecutionNecessary(SecHubExecutionContext context, UUIDTraceLogID traceLogID, SecHubConfiguration configuration) {
        /* reporting does not rely on configuration - must be executed always */
        return true;
    }

}
