package com.mercedesbenz.sechub.domain.statistic.job;

import static com.mercedesbenz.sechub.domain.statistic.job.AnalyticStatisticDataKey.*;
import static com.mercedesbenz.sechub.domain.statistic.job.JobRunStatisticDataType.*;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.sharedkernel.analytic.AnalyticData;
import com.mercedesbenz.sechub.sharedkernel.analytic.CodeAnalyticData;

@Service
public class JobAnaylticDataStatisticService {

    @Autowired
    JobRunStatisticTransactionService transactionService;

    public void storeStatisticData(UUID executionUUID, AnalyticData analyticData) {
        CodeAnalyticData linesOfCode = analyticData.getCodeAnalyticData();
        store(executionUUID, linesOfCode);
    }

    private void store(UUID executionUUID, CodeAnalyticData codeAnalyticData) {
        transactionService.insertJobRunStatisticData(executionUUID, FILES, ALL, codeAnalyticData.getAmountOfFiles());
        transactionService.insertJobRunStatisticData(executionUUID, LOC, ALL, codeAnalyticData.getLinesOfCode());
    }

}
