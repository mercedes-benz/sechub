// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.product;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.daimler.sechub.sharedkernel.UUIDTraceLogID;
import com.daimler.sechub.sharedkernel.configuration.SecHubCodeScanConfiguration;
import com.daimler.sechub.sharedkernel.configuration.SecHubConfiguration;
import com.daimler.sechub.sharedkernel.execution.SecHubExecutionContext;

/**
 * This service executes all registered {@link WebScanProductExecutor} instances
 * and stores those data
 * 
 * @author Albert Tregnaghi
 *
 */
@Service
public class CodeScanProductExecutionServiceImpl extends AbstractProductExecutionService implements CodeScanProductExecutionService {

    private static final Logger LOG = LoggerFactory.getLogger(CodeScanProductExecutionServiceImpl.class);

    private List<CodeScanProductExecutor> codescanExecutors = new ArrayList<>();

    @Autowired
    public CodeScanProductExecutionServiceImpl(List<CodeScanProductExecutor> codescanExecutors) {
        this.codescanExecutors.addAll(codescanExecutors);
        
        LOG.info("Registered code scan executors:{}",codescanExecutors);
        
    }

    @Override
    protected List<CodeScanProductExecutor> getProductExecutors() {
        return codescanExecutors;
    }

    public boolean isExecutionNecessary(SecHubExecutionContext context, UUIDTraceLogID traceLogID, SecHubConfiguration configuration) {
        Optional<SecHubCodeScanConfiguration> codeScanOption = configuration.getCodeScan();
        if (!codeScanOption.isPresent()) {
            LOG.debug("No codescan options found for {}", traceLogID);
            return false;
        }
        LOG.debug("Code scan options found {}", traceLogID);
        return true;
    }

}
