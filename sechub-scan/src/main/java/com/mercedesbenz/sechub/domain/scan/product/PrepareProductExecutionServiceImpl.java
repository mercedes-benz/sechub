package com.mercedesbenz.sechub.domain.scan.product;

import com.mercedesbenz.sechub.commons.model.ScanType;
import com.mercedesbenz.sechub.domain.scan.SecHubExecutionContext;
import com.mercedesbenz.sechub.sharedkernel.UUIDTraceLogID;
import com.mercedesbenz.sechub.sharedkernel.configuration.SecHubConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;


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
}
