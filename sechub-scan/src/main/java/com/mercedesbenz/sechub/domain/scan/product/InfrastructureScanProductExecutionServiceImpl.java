// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.product;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.commons.model.SecHubInfrastructureScanConfiguration;
import com.mercedesbenz.sechub.sharedkernel.UUIDTraceLogID;
import com.mercedesbenz.sechub.sharedkernel.configuration.SecHubConfiguration;
import com.mercedesbenz.sechub.sharedkernel.execution.SecHubExecutionContext;

/**
 * This service executes all registered
 * {@link InfrastructureScanProductExecutor} instances and stores those data
 *
 * @author Albert Tregnaghi
 *
 */
@Service
public class InfrastructureScanProductExecutionServiceImpl extends AbstractProductExecutionService implements InfrastructureScanProductExecutionService {

    private static final Logger LOG = LoggerFactory.getLogger(InfrastructureScanProductExecutionServiceImpl.class);

    private List<InfrastructureScanProductExecutor> webscanExecutors = new ArrayList<>();

    @Autowired
    public InfrastructureScanProductExecutionServiceImpl(List<InfrastructureScanProductExecutor> infraScanExecutors) {
        this.webscanExecutors.addAll(infraScanExecutors);

        LOG.info("Registered infra scan executors:{}", infraScanExecutors);
    }

    @Override
    protected List<InfrastructureScanProductExecutor> getProductExecutors() {
        return webscanExecutors;
    }

    public boolean isExecutionNecessary(SecHubExecutionContext context, UUIDTraceLogID traceLogID, SecHubConfiguration configuration) {
        Optional<SecHubInfrastructureScanConfiguration> infraScanConfiguration = configuration.getInfraScan();
        if (!infraScanConfiguration.isPresent()) {
            LOG.debug("No infrastructure options found for {}", traceLogID);
            return false;
        }
        LOG.debug("Infrastructure scan options found {}", traceLogID);
        return true;
    }

}
