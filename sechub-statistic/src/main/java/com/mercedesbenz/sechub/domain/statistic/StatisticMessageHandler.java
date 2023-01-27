// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.statistic;

import java.time.LocalDateTime;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.commons.model.TrafficLight;
import com.mercedesbenz.sechub.sharedkernel.analytic.AnalyticData;
import com.mercedesbenz.sechub.sharedkernel.messaging.AnalyticMessageData;
import com.mercedesbenz.sechub.sharedkernel.messaging.AsynchronMessageHandler;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.IsReceivingAsyncMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.JobMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.MessageDataKeys;
import com.mercedesbenz.sechub.sharedkernel.messaging.MessageID;
import com.mercedesbenz.sechub.sharedkernel.messaging.StorageMessageData;

@Component
public class StatisticMessageHandler implements AsynchronMessageHandler {

    @Autowired
    StatisticService statisticService;

    private static final Logger LOG = LoggerFactory.getLogger(StatisticMessageHandler.class);

    @Override
    public void receiveAsyncMessage(DomainMessage request) {
        MessageID messageId = request.getMessageId();
        LOG.debug("received domain request: {}", request);

        switch (messageId) {
        case JOB_CREATED:
            handleJobCreated(request);
            break;
        case JOB_DONE:
            handleJobDone(request);
            break;
        case JOB_FAILED:
            handleJobFailed(request);
            break;
        case ANALYZE_SCAN_RESULTS_AVAILABLE:
            handleAnalyzeScanResultsAvailable(request);
            break;
        default:
            throw new IllegalStateException("unhandled message id:" + messageId);
        }
    }

    @IsReceivingAsyncMessage(MessageID.JOB_CREATED)
    private void handleJobCreated(DomainMessage request) {
        JobMessage jobMessage = request.get(MessageDataKeys.JOB_CREATED_DATA);

        UUID jobUUID = jobMessage.getJobUUID();
        LocalDateTime created = jobMessage.getSince();
        String projectId = jobMessage.getProjectId();

        statisticService.handleJobCreated(jobUUID, created, projectId);

    }

    @IsReceivingAsyncMessage(MessageID.START_SCAN)
    private void handleScanStarted(DomainMessage request) {
        JobMessage jobMessage = request.get(MessageDataKeys.JOB_STARTED_DATA);

        UUID jobUUID = jobMessage.getJobUUID();
        LocalDateTime started = jobMessage.getSince();
        UUID executionUUID = jobMessage.getExecutionUUID();

        statisticService.handleJobStarted(jobUUID, started, executionUUID);

    }

    @IsReceivingAsyncMessage(MessageID.ANALYZE_SCAN_RESULTS_AVAILABLE)
    private void handleAnalyzeScanResultsAvailable(DomainMessage request) {
        AnalyticMessageData analyticMessageData = request.get(MessageDataKeys.ANALYTIC_SCAN_RESULT_DATA);

        UUID executionUUID = request.get(MessageDataKeys.SECHUB_EXECUTION_UUID);
        AnalyticData analyticData = analyticMessageData.getAnalyticData();

        statisticService.handleAnalyticData(executionUUID, analyticData);
    }

    @IsReceivingAsyncMessage(MessageID.JOB_DONE)
    private void handleJobDone(DomainMessage request) {
        JobMessage jobMessage = request.get(MessageDataKeys.JOB_DONE_DATA);

        UUID jobUUID = jobMessage.getJobUUID();
        LocalDateTime since = jobMessage.getSince();
        TrafficLight trafficLight = jobMessage.getTrafficLight();

        statisticService.handleJobDone(jobUUID, since, trafficLight);

    }

    @IsReceivingAsyncMessage(MessageID.JOB_FAILED)
    private void handleJobFailed(DomainMessage request) {
        JobMessage jobMessage = request.get(MessageDataKeys.JOB_FAILED_DATA);

        UUID jobUUID = jobMessage.getJobUUID();
        LocalDateTime since = jobMessage.getSince();
        TrafficLight trafficLight = jobMessage.getTrafficLight();

        statisticService.handleJobFailed(jobUUID, since, trafficLight);

    }

    @IsReceivingAsyncMessage(MessageID.SOURCE_UPLOAD_DONE)
    private void handleSourceUploadDone(DomainMessage request) {
        StorageMessageData storageMessageData = request.get(MessageDataKeys.UPLOAD_STORAGE_DATA);

        UUID jobUUID = storageMessageData.getJobUUID();
        LocalDateTime since = storageMessageData.getSince();
        long sizeInBytes = storageMessageData.getSizeInBytes();

        statisticService.handleSourceUploadDone(jobUUID, since, sizeInBytes);

    }

    @IsReceivingAsyncMessage(MessageID.BINARY_UPLOAD_DONE)
    private void handleBinaryUploadDone(DomainMessage request) {
        StorageMessageData storageMessageData = request.get(MessageDataKeys.UPLOAD_STORAGE_DATA);

        UUID jobUUID = storageMessageData.getJobUUID();
        LocalDateTime since = storageMessageData.getSince();
        long sizeInBytes = storageMessageData.getSizeInBytes();

        statisticService.handleBinaryUploadDone(jobUUID, since, sizeInBytes);

    }

}
