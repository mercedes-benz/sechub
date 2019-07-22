// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.product;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.daimler.sechub.sharedkernel.UUIDTraceLogID;
import com.daimler.sechub.sharedkernel.configuration.SecHubConfiguration;
import com.daimler.sechub.sharedkernel.configuration.SecHubInfrastructureScanConfiguration;
import com.daimler.sechub.sharedkernel.execution.SecHubExecutionContext;

/**
 * This service executes all registered {@link InfrastructureScanProductExecutor} instances
 * and stores those data
 * 
 * @author Albert Tregnaghi
 *
 */
@Service
public class InfrastructureScanProductExecutionServiceImpl extends AbstractProductExecutionService
		implements InfrastructureScanProductExecutionService {

	private static final Logger LOG = LoggerFactory.getLogger(InfrastructureScanProductExecutionServiceImpl.class);

	@Autowired
	public InfrastructureScanProductExecutionServiceImpl(List<InfrastructureScanProductExecutor> webscanExecutors) {
		register(webscanExecutors);
	}

	public boolean isExecutionNecessary(SecHubExecutionContext context, UUIDTraceLogID traceLogID,
			SecHubConfiguration configuration) {
		Optional<SecHubInfrastructureScanConfiguration> webScanOption = configuration.getInfraScan();
		if (!webScanOption.isPresent()) {
			LOG.debug("No infrastructure options found for {}", traceLogID);
			return false;
		}
		LOG.debug("Infrastructure scan options found {}", traceLogID);
		return true;
	}

}
