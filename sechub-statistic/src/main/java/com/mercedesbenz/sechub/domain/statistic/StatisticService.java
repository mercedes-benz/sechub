package com.mercedesbenz.sechub.domain.statistic;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.commons.model.TrafficLight;
import com.mercedesbenz.sechub.domain.statistic.job.JobAnalyticDataStatisticService;
import com.mercedesbenz.sechub.domain.statistic.job.JobRunStatisticTransactionService;
import com.mercedesbenz.sechub.domain.statistic.job.JobStatisticDataType;
import com.mercedesbenz.sechub.domain.statistic.job.JobStatisticTransactionService;
import com.mercedesbenz.sechub.domain.statistic.job.UploadJobStatisticDataKeys;
import com.mercedesbenz.sechub.sharedkernel.analytic.AnalyticData;

@Service
/**
 * The main entry point for writing statistics
 *
 * @author Albert Tregnaghi
 *
 */
public class StatisticService {

    @Autowired
    JobStatisticTransactionService jobStatisticTransactionService;

    @Autowired
    JobRunStatisticTransactionService jobRunStatisticTransactionService;

    @Autowired
    JobAnalyticDataStatisticService jobAnalyticDataStatisticService;

    public void handleJobCreated(UUID jobUUID, LocalDateTime created, String projectId) {
        jobStatisticTransactionService.createJobStatistic(jobUUID, created, projectId);
    }

    public void handleJobStarted(UUID jobUUID, LocalDateTime started, UUID executionUUID) {
        jobRunStatisticTransactionService.createJobRunStatistic(executionUUID, jobUUID, started);
    }

    public void handleJobDone(UUID jobUUID, LocalDateTime since, TrafficLight trafficLight) {
        jobRunStatisticTransactionService.markJobRunEnded(jobUUID, trafficLight, since, false);
    }

    public void handleJobFailed(UUID jobUUID, LocalDateTime since, TrafficLight trafficLight) {
        jobRunStatisticTransactionService.markJobRunEnded(jobUUID, trafficLight, since, true);
    }

    public void handleAnalyticData(UUID executionUUID, AnalyticData analyticData) {
        jobAnalyticDataStatisticService.storeStatisticData(executionUUID, analyticData);
    }

    public void handleSourceUploadDone(UUID jobUUID, LocalDateTime since, long sizeInBytes) {
        jobStatisticTransactionService.insertJobStatisticData(jobUUID, JobStatisticDataType.UPLOAD_SOURCES, UploadJobStatisticDataKeys.SIZE_IN_BYTES,
                BigInteger.valueOf(sizeInBytes));
    }

    public void handleBinaryUploadDone(UUID jobUUID, LocalDateTime since, long sizeInBytes) {
        jobStatisticTransactionService.insertJobStatisticData(jobUUID, JobStatisticDataType.UPLOAD_BINARIES, UploadJobStatisticDataKeys.SIZE_IN_BYTES,
                BigInteger.valueOf(sizeInBytes));
    }

}
