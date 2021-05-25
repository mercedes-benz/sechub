// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan;

import static com.daimler.sechub.sharedkernel.messaging.DomainDataTraceLogID.*;
import static com.daimler.sechub.sharedkernel.messaging.MessageDataKeys.*;
import static com.daimler.sechub.sharedkernel.util.Assert.*;

import java.io.IOException;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.daimler.sechub.commons.model.JSONConverterException;
import com.daimler.sechub.domain.scan.log.ProjectScanLogService;
import com.daimler.sechub.domain.scan.product.CodeScanProductExecutionService;
import com.daimler.sechub.domain.scan.product.InfrastructureScanProductExecutionService;
import com.daimler.sechub.domain.scan.product.ProductResultService;
import com.daimler.sechub.domain.scan.product.WebScanProductExecutionService;
import com.daimler.sechub.domain.scan.project.ScanProjectConfig;
import com.daimler.sechub.domain.scan.project.ScanProjectConfigID;
import com.daimler.sechub.domain.scan.project.ScanProjectConfigService;
import com.daimler.sechub.domain.scan.project.ScanProjectMockDataConfiguration;
import com.daimler.sechub.domain.scan.report.CreateScanReportService;
import com.daimler.sechub.domain.scan.report.ScanReport;
import com.daimler.sechub.domain.scan.report.ScanReportException;
import com.daimler.sechub.sharedkernel.MustBeDocumented;
import com.daimler.sechub.sharedkernel.ProgressMonitor;
import com.daimler.sechub.sharedkernel.configuration.SecHubConfiguration;
import com.daimler.sechub.sharedkernel.execution.SecHubExecutionAbandonedException;
import com.daimler.sechub.sharedkernel.execution.SecHubExecutionContext;
import com.daimler.sechub.sharedkernel.execution.SecHubExecutionException;
import com.daimler.sechub.sharedkernel.messaging.BatchJobMessage;
import com.daimler.sechub.sharedkernel.messaging.DomainDataTraceLogID;
import com.daimler.sechub.sharedkernel.messaging.DomainMessage;
import com.daimler.sechub.sharedkernel.messaging.DomainMessageSynchronousResult;
import com.daimler.sechub.sharedkernel.messaging.IsRecevingSyncMessage;
import com.daimler.sechub.sharedkernel.messaging.IsSendingSyncMessageAnswer;
import com.daimler.sechub.sharedkernel.messaging.MessageDataKeys;
import com.daimler.sechub.sharedkernel.messaging.MessageID;
import com.daimler.sechub.sharedkernel.messaging.SynchronMessageHandler;
import com.daimler.sechub.storage.core.JobStorage;
import com.daimler.sechub.storage.core.StorageService;

/**
 * Scan service - main entry point for scans
 *
 * @author Albert Tregnaghi
 *
 */
@Service
public class ScanService implements SynchronMessageHandler {

    private static final Logger LOG = LoggerFactory.getLogger(ScanService.class);

    /**
     * Default time out is one minute (60 * 1000 milliseconds)
     */
    private static final int DEFAULT_CHECK_CANCELJOB_DELAY_MILLIS = 60000;

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

    @Autowired
    ScanJobListener scanJobListener;

    @Autowired
    ProductResultService productResultService;

    @Autowired
    ScanProgressMonitorFactory monitorFactory;

    @MustBeDocumented("Define delay in milliseconds, for before next job cancelation check will be executed.")
    @Value("${sechub.config.check.canceljob.delay:" + DEFAULT_CHECK_CANCELJOB_DELAY_MILLIS + "}")
    private int millisecondsToWaitBeforeCancelCheck = DEFAULT_CHECK_CANCELJOB_DELAY_MILLIS;

    @IsSendingSyncMessageAnswer(value = MessageID.SCAN_DONE, answeringTo = MessageID.START_SCAN, branchName = "success")
    @IsSendingSyncMessageAnswer(value = MessageID.SCAN_FAILED, answeringTo = MessageID.START_SCAN, branchName = "failure")
    DomainMessageSynchronousResult startScan(DomainMessage request) {

        SecHubExecutionContext context = null;
        try {
            context = createExecutionContext(request);

            executeScan(context, request);

            ScanReport report = reportService.createReport(context);

            DomainMessageSynchronousResult response = new DomainMessageSynchronousResult(MessageID.SCAN_DONE);
            response.set(REPORT_TRAFFIC_LIGHT, report.getTrafficLightAsString());
            return response;

        } catch (ScanReportException e) {
            LOG.error("Execution was possible, but report failed." + traceLogID(request), e);
            return new DomainMessageSynchronousResult(MessageID.SCAN_FAILED, e);

        } catch (SecHubExecutionAbandonedException e) {
            LOG.info("Execution abandoned on scan {} - message: {}", traceLogID(request), e.getMessage());
            return new DomainMessageSynchronousResult(MessageID.SCAN_ABANDONDED, e);
        } catch (SecHubExecutionException e) {
            LOG.error("Execution problems on scan." + traceLogID(request), e);
            return new DomainMessageSynchronousResult(MessageID.SCAN_FAILED, e);
        } catch (Exception e) {
            LOG.error("Was not able to start scan." + traceLogID(request), e);
            return new DomainMessageSynchronousResult(MessageID.SCAN_FAILED, e);
        } finally {
            if (context == null) {
                LOG.warn("No sechub execution context available, so cannot check state or cleanup storage");
            }else {
                if (!context.isAbandonded()) {
                    cleanupStorage(context);
                }
            }
        }
    }

    protected void executeScan(SecHubExecutionContext context, DomainMessage request) throws SecHubExecutionException {
        DomainDataTraceLogID sechubJobUUID = traceLogID(request);

        LOG.info("start scan for {}", sechubJobUUID);

        UUID logUUID = scanLogService.logScanStarted(context);
        try {
            BatchJobMessage jobIdMessage = request.get(MessageDataKeys.BATCH_JOB_ID);
            if (jobIdMessage == null) {
                throw new IllegalStateException("no batch job id set for sechub job:" + sechubJobUUID);
            }
            long batchJobId = jobIdMessage.getBatchJobId();

            ProgressMonitor progressMonitor = monitorFactory.createProgressMonitor(batchJobId);

            /* delegate execution : */
            ScanJobExecutor executor = new ScanJobExecutor(this, context, progressMonitor, millisecondsToWaitBeforeCancelCheck);
            executor.execute();

            scanLogService.logScanEnded(logUUID);

        } catch (Exception e) {
            if (context.isAbandonded()) {
                scanLogService.logScanAbandoned(logUUID);
            } else {
                scanLogService.logScanFailed(logUUID);
            }
            /* rethrow when already an execution exception */
            if (e instanceof SecHubExecutionException) {
                SecHubExecutionException exceptionToRethrow = (SecHubExecutionException) e;
                throw exceptionToRethrow;
            }
            /* wrap it */
            throw new SecHubExecutionException("Execute scan failed", e);
        }

    }

    /*
     * Cleans storage for current job
     */
    private void cleanupStorage(SecHubExecutionContext context) {
        if (context == null) {
            LOG.warn("No context available so no cleanup possible");
            return;
        }
        SecHubConfiguration configuration = context.getConfiguration();
        if (configuration == null) {
            LOG.warn("No configuration available so no cleanup possible");
            return;
        }
        String projectId = configuration.getProjectId();
        UUID jobUUID = context.getSechubJobUUID();
        JobStorage storage = storageService.getJobStorage(projectId, jobUUID);

        try {
            storage.deleteAll();
        } catch (IOException e) {
            LOG.error("Was not able to delete storage for job {}", jobUUID, e);
        }

    }

    private SecHubExecutionContext createExecutionContext(DomainMessage message) throws JSONConverterException {
        UUID uuid = message.get(SECHUB_UUID);
        String executedBy = message.get(EXECUTED_BY);

        SecHubConfiguration configuration = message.get(SECHUB_CONFIG);
        if (configuration == null) {
            throw new IllegalStateException("SecHubConfiguration not found in message - so cannot execute!");
        }
        SecHubExecutionContext executionContext = new SecHubExecutionContext(uuid, configuration, executedBy);

        buildOptions(executionContext);

        return executionContext;
    }

    private void buildOptions(SecHubExecutionContext executionContext) {
        /* project specific setup */
        String projectId = executionContext.getConfiguration().getProjectId();
        if (projectId == null) {
            throw new IllegalStateException("projectId not found in configuration - so cannot prepare context options!");
        }
        ScanProjectConfig scanProjectMockConfig = scanProjectConfigService.get(projectId, ScanProjectConfigID.MOCK_CONFIGURATION, false);
        if (scanProjectMockConfig != null) {
            String data = scanProjectMockConfig.getData();
            ScanProjectMockDataConfiguration mockDataConfig = ScanProjectMockDataConfiguration.fromString(data);
            executionContext.putData(ScanKey.PROJECT_MOCKDATA_CONFIGURATION, mockDataConfig);
        }
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
