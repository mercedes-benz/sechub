// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.product;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.commons.model.SecHubWebScanConfiguration;
import com.mercedesbenz.sechub.sharedkernel.UUIDTraceLogID;
import com.mercedesbenz.sechub.sharedkernel.configuration.SecHubConfiguration;
import com.mercedesbenz.sechub.sharedkernel.execution.SecHubExecutionContext;

/**
 * This service executes all registered {@link WebScanProductExecutor} instances
 * and stores those data
 *
 * @author Albert Tregnaghi
 *
 */
@Service
public class WebScanProductExecutionServiceImpl extends AbstractProductExecutionService implements WebScanProductExecutionService {

    private static final Logger LOG = LoggerFactory.getLogger(WebScanProductExecutionServiceImpl.class);

    private List<WebScanProductExecutor> webscanExecutors = new ArrayList<>();

    @Autowired
    public WebScanProductExecutionServiceImpl(List<WebScanProductExecutor> webscanExecutors) {
        this.webscanExecutors.addAll(webscanExecutors);

        LOG.info("Registered web scan executors:{}", webscanExecutors);
    }

    @Override
    protected List<WebScanProductExecutor> getProductExecutors() {
        return webscanExecutors;
    }

    public boolean isExecutionNecessary(SecHubExecutionContext context, UUIDTraceLogID traceLogID, SecHubConfiguration configuration) {
        Optional<SecHubWebScanConfiguration> webScanOption = configuration.getWebScan();
        if (!webScanOption.isPresent()) {
            LOG.debug("No webscan options found for {}", traceLogID);
            return false;
        }
        LOG.debug("Web scan options found {}", traceLogID);
        return true;
    }

}
