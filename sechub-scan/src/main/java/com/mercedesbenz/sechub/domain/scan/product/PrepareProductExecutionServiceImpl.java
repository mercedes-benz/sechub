// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.product;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.commons.core.prepare.PrepareResult;
import com.mercedesbenz.sechub.commons.model.ScanType;
import com.mercedesbenz.sechub.domain.scan.SecHubExecutionContext;
import com.mercedesbenz.sechub.sharedkernel.UUIDTraceLogID;
import com.mercedesbenz.sechub.sharedkernel.configuration.SecHubConfiguration;

/**
 * This service executes all registered product executors having scan type
 * {@link ScanType#PREPARE}
 */

@Service
public class PrepareProductExecutionServiceImpl extends AbstractProductExecutionService implements PrepareProductExecutionService {

    private static final Logger LOG = LoggerFactory.getLogger(PrepareProductExecutionServiceImpl.class);

    @Override
    protected boolean isExecutionNecessary(SecHubExecutionContext context, UUIDTraceLogID traceLogID, SecHubConfiguration configuration) {
        LOG.debug("prepare product executor");
        return true;
    }

    protected void afterProductResultsStored(List<ProductResult> productResults, SecHubExecutionContext context) {

        /*
         * when at least one of the prepare results is not done we mark the preparation
         * as failed
         */
        for (ProductResult productResult : productResults) {
            PrepareResult prepareResult = PrepareResult.fromString(productResult.getResult());

            if (!prepareResult.isPreparationDone()) {
                context.markPrepareFailed();
                break;
            }
        }
    }
}
