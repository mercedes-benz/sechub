// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan;

import static com.mercedesbenz.sechub.sharedkernel.messaging.DomainDataTraceLogID.*;
import static com.mercedesbenz.sechub.sharedkernel.messaging.MessageDataKeys.*;
import static com.mercedesbenz.sechub.sharedkernel.util.Assert.*;

import java.io.IOException;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.commons.model.JSONConverterException;
import com.mercedesbenz.sechub.commons.model.SecHubMessagesList;
import com.mercedesbenz.sechub.commons.model.SecHubReportModel;
import com.mercedesbenz.sechub.domain.scan.log.ProjectScanLogService;
import com.mercedesbenz.sechub.domain.scan.product.ProductResultService;
import com.mercedesbenz.sechub.domain.scan.project.ScanProjectConfig;
import com.mercedesbenz.sechub.domain.scan.project.ScanProjectConfigID;
import com.mercedesbenz.sechub.domain.scan.project.ScanProjectConfigService;
import com.mercedesbenz.sechub.domain.scan.project.ScanProjectMockDataConfiguration;
import com.mercedesbenz.sechub.domain.scan.report.CreateScanReportService;
import com.mercedesbenz.sechub.domain.scan.report.ScanReport;
import com.mercedesbenz.sechub.domain.scan.report.ScanReportException;
import com.mercedesbenz.sechub.sharedkernel.MustBeDocumented;
import com.mercedesbenz.sechub.sharedkernel.ProgressStateFetcher;
import com.mercedesbenz.sechub.sharedkernel.Step;
import com.mercedesbenz.sechub.sharedkernel.configuration.SecHubConfiguration;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessageSynchronousResult;
import com.mercedesbenz.sechub.sharedkernel.messaging.IsRecevingSyncMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.IsSendingSyncMessageAnswer;
import com.mercedesbenz.sechub.sharedkernel.messaging.MessageID;
import com.mercedesbenz.sechub.sharedkernel.messaging.SynchronMessageHandler;
import com.mercedesbenz.sechub.sharedkernel.storage.SecHubStorageService;
import com.mercedesbenz.sechub.sharedkernel.usecases.other.UseCaseSystemSuspendsJobsWhenSigTermReceived;
import com.mercedesbenz.sechub.storage.core.JobStorage;

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
    SecHubStorageService storageService;

    @Autowired
    ProductExecutionServiceContainer productExecutionServiceContainer;

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
    ScanProgressStateFetcherFactory monitorFactory;

    @MustBeDocumented("Define delay in milliseconds, for before next job cancellation check will be executed.")
    @Value("${sechub.config.check.canceljob.delay:" + DEFAULT_CHECK_CANCELJOB_DELAY_MILLIS + "}")
    private int millisecondsToWaitBeforeCancelCheck = DEFAULT_CHECK_CANCELJOB_DELAY_MILLIS;

    @IsSendingSyncMessageAnswer(value = MessageID.SCAN_DONE, answeringTo = MessageID.START_SCAN, branchName = "success")
    @IsSendingSyncMessageAnswer(value = MessageID.SCAN_FAILED, answeringTo = MessageID.START_SCAN, branchName = "failure")
    @IsSendingSyncMessageAnswer(value = MessageID.SCAN_SUSPENDED, answeringTo = MessageID.START_SCAN, branchName = "suspended")
    @UseCaseSystemSuspendsJobsWhenSigTermReceived(@Step(number = 3, name = "Scheduler job executor suspends current jobs", description = "Scheduler instance is terminating. Will mark current running jobs of this instance as SUSPENDED"))
    DomainMessageSynchronousResult startScan(DomainMessage request) {

        SecHubExecutionContext context = null;
        try {
            context = createExecutionContext(request);

            executeScan(context, request);

            if (context.isSuspended()) {
                DomainMessageSynchronousResult response = new DomainMessageSynchronousResult(MessageID.SCAN_SUSPENDED);
                return response;

            }
            ScanReport report = reportService.createReport(context);

            DomainMessageSynchronousResult response = new DomainMessageSynchronousResult(MessageID.SCAN_DONE);
            response.set(REPORT_TRAFFIC_LIGHT, report.getTrafficLightAsString());

            String result = report.getResult();
            if (result == null) {
                LOG.warn("Report had no result, means no sechub report model available! Cannot set report messages");
            } else {
                SecHubReportModel model = SecHubReportModel.fromJSONString(result);
                response.set(REPORT_MESSAGES, new SecHubMessagesList(model.getMessages()));
            }
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
        } finally {
            if (context == null) {
                LOG.warn("No sechub execution context available, so cannot check state or cleanup storage");
            } else {

                if (context.isSuspended()) {
                    LOG.info("Skip storage cleanup because only suspended: {}", context.getSechubJobUUID());
                } else {
                    cleanupStorage(context);
                }

            }
        }
    }

    protected void executeScan(SecHubExecutionContext context, DomainMessage request) throws SecHubExecutionException {
        UUID sechubJobUUID = context.getSechubJobUUID();

        LOG.info("Start scan for SecHub job: {}", sechubJobUUID);

        UUID logUUID = scanLogService.logScanStarted(context);

        try {
            ProgressStateFetcher progressStateFetcher = monitorFactory.createProgressStateFetcher(sechubJobUUID);

            /* delegate execution : */
            ScanJobExecutor executor = new ScanJobExecutor(productExecutionServiceContainer, scanJobListener, context, progressStateFetcher,
                    millisecondsToWaitBeforeCancelCheck);
            executor.startScanAndInspectCancelRequests();

            scanLogService.logScanEnded(logUUID);

        } catch (Exception e) {
            scanLogService.logScanFailed(logUUID);

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
        JobStorage storage = storageService.createJobStorageForProject(projectId, jobUUID);

        try {
            storage.deleteAll();
        } catch (IOException e) {
            LOG.error("Was not able to delete storage for job {}", jobUUID, e);
        }

    }

    private SecHubExecutionContext createExecutionContext(DomainMessage message) throws JSONConverterException {
        UUID executionUUID = message.get(SECHUB_EXECUTION_UUID);

        UUID sechubJobUUID = message.get(SECHUB_JOB_UUID);
        String executedBy = message.get(EXECUTED_BY);

        SecHubConfiguration configuration = message.get(SECHUB_UNENCRYPTED_CONFIG);
        if (configuration == null) {
            throw new IllegalStateException("SecHubConfiguration not found in message - so cannot execute!");
        }
        SecHubExecutionContext executionContext = new SecHubExecutionContext(sechubJobUUID, configuration, executedBy, executionUUID);

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
            return failBecauseUnsupportedMessage();
        }
        return startScan(request);
    }

    @IsSendingSyncMessageAnswer(value = MessageID.UNSUPPORTED_OPERATION, answeringTo = MessageID.START_SCAN, branchName = "failure")
    private DomainMessageSynchronousResult failBecauseUnsupportedMessage() {
        return new DomainMessageSynchronousResult(MessageID.UNSUPPORTED_OPERATION,
                new UnsupportedOperationException("Can only handle " + MessageID.START_SCAN));
    }

}
