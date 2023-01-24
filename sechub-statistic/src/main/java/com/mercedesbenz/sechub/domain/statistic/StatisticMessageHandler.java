// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.statistic;

import java.time.LocalDateTime;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.commons.model.TrafficLight;
import com.mercedesbenz.sechub.domain.statistic.execution.AnaylticDataExecutionStatsticService;
import com.mercedesbenz.sechub.domain.statistic.job.JobCreationStatisticService;
import com.mercedesbenz.sechub.domain.statistic.job.JobDetailStatisticService;
import com.mercedesbenz.sechub.sharedkernel.analytic.AnalyticData;
import com.mercedesbenz.sechub.sharedkernel.messaging.AnalyticDataMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.AsynchronMessageHandler;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.IsReceivingAsyncMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.JobMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.MessageDataKeys;
import com.mercedesbenz.sechub.sharedkernel.messaging.MessageID;

@Component
public class StatisticMessageHandler implements AsynchronMessageHandler {

    @Autowired
    AnaylticDataExecutionStatsticService anyalyticScanResultToStatisticTransformationService;
    
    @Autowired
    JobCreationStatisticService jobCreationStatsticService;
    
    @Autowired
    JobDetailStatisticService jobFinalizationStatisticService;
    
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
        
        jobCreationStatsticService.storeJobCreationStatistic(jobUUID, created, projectId);
        
    }

    @IsReceivingAsyncMessage(MessageID.ANALYZE_SCAN_RESULTS_AVAILABLE)
    private void handleAnalyzeScanResultsAvailable(DomainMessage request) {
        AnalyticDataMessage scanResultDataMessage = request.get(MessageDataKeys.ANALYTIC_SCAN_RESULT_DATA);
        
        AnalyticData analyticData = scanResultDataMessage.getAnalyticData();
    
        anyalyticScanResultToStatisticTransformationService.storeStatisticData(analyticData);
    }
    
    @IsReceivingAsyncMessage(MessageID.JOB_DONE)
    private void handleJobDone(DomainMessage request) {
        JobMessage jobMessage = request.get(MessageDataKeys.JOB_DONE_DATA);
        
        UUID jobUUID = jobMessage.getJobUUID();
        LocalDateTime done = jobMessage.getSince();
        TrafficLight trafficLight = jobMessage.getTrafficLight();
                
        jobFinalizationStatisticService.storeJobDone(jobUUID, done, trafficLight);
        
    }
    
    @IsReceivingAsyncMessage(MessageID.JOB_FAILED)
    private void handleJobFailed(DomainMessage request) {
        JobMessage jobMessage = request.get(MessageDataKeys.JOB_FAILED_DATA);
        
        UUID jobUUID = jobMessage.getJobUUID();
        LocalDateTime done = jobMessage.getSince();
        TrafficLight trafficLight = jobMessage.getTrafficLight();
        
        jobFinalizationStatisticService.storeJobFailed(jobUUID, done, trafficLight);
        
    }


}
