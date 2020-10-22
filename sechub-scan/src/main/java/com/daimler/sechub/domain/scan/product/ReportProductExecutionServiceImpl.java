// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.product;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.daimler.sechub.domain.scan.report.ScanReportProductExecutor;
import com.daimler.sechub.sharedkernel.UUIDTraceLogID;
import com.daimler.sechub.sharedkernel.configuration.SecHubConfiguration;
import com.daimler.sechub.sharedkernel.execution.SecHubExecutionContext;

/**
 * This service executes all registered {@link ScanReportProductExecutor} instances
 * and stores those data
 * 
 * @author Albert Tregnaghi
 *
 */
@Service
public class ReportProductExecutionServiceImpl extends AbstractProductExecutionService
		implements ReportProductExecutionService {
    

    private static final Logger LOG = LoggerFactory.getLogger(ReportProductExecutionServiceImpl.class);

    private List<ScanReportProductExecutor> reportProductExecutors = new ArrayList<>();
    
	@Autowired
	public ReportProductExecutionServiceImpl(List<ScanReportProductExecutor> reportProductExecutors) {
	    this.reportProductExecutors.addAll(reportProductExecutors);
	    
	    LOG.info("Registered report product executors:{}", reportProductExecutors);
	}
	
	@Override
	protected List<ScanReportProductExecutor> getProductExecutors() {
	    return reportProductExecutors;
	}

	public boolean isExecutionNecessary(SecHubExecutionContext context, UUIDTraceLogID traceLogID,
			SecHubConfiguration configuration) {
		/*
		 * TODO Albert Tregnaghi, 2018-02-15: here should be checked if this is a reentrant - and
		 * an result is already existing for the report - if exists, just return
		 * false... and we save time...
		 */
		return true;
	}

}
