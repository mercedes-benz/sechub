// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan;

import static com.daimler.sechub.sharedkernel.messaging.DomainDataTraceLogID.*;
import static com.daimler.sechub.sharedkernel.messaging.MessageDataKeys.*;
import static com.daimler.sechub.sharedkernel.util.Assert.*;

import java.io.IOException;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.daimler.sechub.domain.scan.log.ProjectScanLogService;
import com.daimler.sechub.domain.scan.product.CodeScanProductExecutionService;
import com.daimler.sechub.domain.scan.product.InfrastructureScanProductExecutionService;
import com.daimler.sechub.domain.scan.product.WebScanProductExecutionService;
import com.daimler.sechub.domain.scan.project.ScanProjectConfig;
import com.daimler.sechub.domain.scan.project.ScanProjectConfigID;
import com.daimler.sechub.domain.scan.project.ScanProjectConfigService;
import com.daimler.sechub.domain.scan.report.CreateScanReportService;
import com.daimler.sechub.domain.scan.report.ScanReport;
import com.daimler.sechub.domain.scan.report.ScanReportException;
import com.daimler.sechub.sharedkernel.LogConstants;
import com.daimler.sechub.sharedkernel.configuration.SecHubConfiguration;
import com.daimler.sechub.sharedkernel.execution.SecHubExecutionContext;
import com.daimler.sechub.sharedkernel.execution.SecHubExecutionException;
import com.daimler.sechub.sharedkernel.messaging.DomainDataTraceLogID;
import com.daimler.sechub.sharedkernel.messaging.DomainMessage;
import com.daimler.sechub.sharedkernel.messaging.DomainMessageSynchronousResult;
import com.daimler.sechub.sharedkernel.messaging.IsRecevingSyncMessage;
import com.daimler.sechub.sharedkernel.messaging.IsSendingSyncMessageAnswer;
import com.daimler.sechub.sharedkernel.messaging.MessageID;
import com.daimler.sechub.sharedkernel.messaging.SynchronMessageHandler;
import com.daimler.sechub.sharedkernel.storage.StorageService;
import com.daimler.sechub.sharedkernel.util.JSONConverterException;
import com.daimler.sechub.storage.core.JobStorage;

/**
 * Scan service - main entry point for scans. We use a REQUIRES_NEW propagation
 * to abtain a new transaction
 *
 * @author Albert Tregnaghi
 *
 */
@Service
public class ScanService implements SynchronMessageHandler {

	private static final Logger LOG = LoggerFactory.getLogger(ScanService.class);
	@Autowired
	StorageService storageService;

	@Autowired
	CodeScanProductExecutionService codeScanProductExecutionService;

	@Autowired
	WebScanProductExecutionService webScanProductExecutionService;

	@Autowired
	InfrastructureScanProductExecutionService infraScanProductExecutionService;

	@Autowired
	CreateScanReportService reportService;

	@Autowired
	ProjectScanLogService scanLogService;
	
	@Autowired
	ScanProjectConfigService scanProjectConfigService;
	

	@IsSendingSyncMessageAnswer(value = MessageID.SCAN_DONE, answeringTo = MessageID.START_SCAN, branchName="success")
	@IsSendingSyncMessageAnswer(value = MessageID.SCAN_FAILED, answeringTo = MessageID.START_SCAN, branchName="failure")
	DomainMessageSynchronousResult startScan(DomainMessage request) {
		SecHubExecutionContext context = null;
		try {
			context = createExecutionContext(request);

			executeScan(context,request);

			ScanReport report = reportService.createReport(context);

			DomainMessageSynchronousResult response = new DomainMessageSynchronousResult(MessageID.SCAN_DONE);
			response.set(REPORT_TRAFFIC_LIGHT, report.getTrafficLightAsString());
			return response;

		} catch (ScanReportException e) {
			LOG.error("Execution was possible, but report failed." + traceLogID(request), e);
			return new DomainMessageSynchronousResult(MessageID.SCAN_FAILED, e);

		} catch (SecHubExecutionException e) {
			LOG.error("Execution problems on scan." + traceLogID(request), e);
			return new DomainMessageSynchronousResult(MessageID.SCAN_FAILED, e);
		} catch (Exception e) {
			LOG.error("Was not able to start scan." + traceLogID(request), e);
			return new DomainMessageSynchronousResult(MessageID.SCAN_FAILED, e);
		}finally {
			cleanupStorage(context);
		}
	}

	protected void executeScan(SecHubExecutionContext context, DomainMessage request) throws SecHubExecutionException {
		DomainDataTraceLogID sechubJobUUID = traceLogID(request);
		MDC.put(LogConstants.MDC_SECHUB_JOB_UUID, sechubJobUUID.getPlainId());

		LOG.info("start scan for {}", sechubJobUUID);

		UUID logUUID = scanLogService.logScanStarted(context);
		try {
			codeScanProductExecutionService.executeProductsAndStoreResults(context);
			webScanProductExecutionService.executeProductsAndStoreResults(context);
			infraScanProductExecutionService.executeProductsAndStoreResults(context);
			scanLogService.logScanEnded(logUUID);
		}catch(Exception e) {
			scanLogService.logScanFailed(logUUID);
			throw new SecHubExecutionException("Execute scan failed", e);
		}

	}

	/*
	 * Cleans storage for current job
	 */
	private void cleanupStorage(SecHubExecutionContext context) {
		if (context==null) {
			LOG.warn("No context available so no cleanup possible");
			return;
		}
		SecHubConfiguration configuration = context.getConfiguration();
		if (configuration==null) {
			LOG.warn("No configuration available so no cleanup possible");
			return;
		}
		String projectId = configuration.getProjectId();
		UUID jobUUID = context.getSechubJobUUID();
		JobStorage storage = storageService.getJobStorage(projectId, jobUUID);

		try {
			storage.deleteAll();
		} catch (IOException e) {
			LOG.error("Was not able to delete storage for job {}",jobUUID,e);
		}

	}


	private SecHubExecutionContext createExecutionContext(DomainMessage message) throws JSONConverterException {
		UUID uuid = message.get(SECHUB_UUID);
		SecHubConfiguration configuration = message.get(SECHUB_CONFIG);
		String executedBy = message.get(EXECUTED_BY);
		
		SecHubExecutionContext executionContext = new SecHubExecutionContext(uuid, configuration,executedBy);
		ScanProjectConfig projectMockConfig = scanProjectConfigService.get(configuration.getProjectId(), ScanProjectConfigID.MOCK_CONFIGURATION);
		executionContext.getOptions().put(ScanProjectConfigID.MOCK_CONFIGURATION.getId(),projectMockConfig);
		
		return executionContext;
	}

	@Override
	@IsRecevingSyncMessage(MessageID.START_SCAN)
	public DomainMessageSynchronousResult receiveSynchronMessage(DomainMessage request) {
		notNull(request, "Request may not be null!");

		if (!request.hasID(MessageID.START_SCAN)) {
			return new DomainMessageSynchronousResult(MessageID.UNSUPPORTED_OPERATION,
					new UnsupportedOperationException("Can only handle " + MessageID.START_SCAN));
		}
		return startScan(request);
	}

}
