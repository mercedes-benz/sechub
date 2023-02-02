package com.mercedesbenz.sechub.domain.statistic.job;

import static com.mercedesbenz.sechub.domain.statistic.job.AnalyticStatisticDataKey.*;
import static com.mercedesbenz.sechub.domain.statistic.job.JobRunStatisticDataType.*;

import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.domain.statistic.AnyTextAsKey;
import com.mercedesbenz.sechub.domain.statistic.StatisticDataContainer;
import com.mercedesbenz.sechub.sharedkernel.analytic.AnalyticData;
import com.mercedesbenz.sechub.sharedkernel.analytic.CodeAnalyticData;

@Service
public class JobAnaylticDataStatisticService {

    @Autowired
    JobRunStatisticTransactionService transactionService;

    public void storeStatisticData(UUID executionUUID, AnalyticData analyticData) {
        StatisticDataContainer<JobRunStatisticDataType> dataContainer = collectCodeAnalyticData(analyticData);
        
        transactionService.insertJobRunStatsticData(executionUUID, dataContainer);
  
    }


    private StatisticDataContainer<JobRunStatisticDataType> collectCodeAnalyticData(AnalyticData analyticData) {
        StatisticDataContainer<JobRunStatisticDataType> dataContainer =  new StatisticDataContainer<>();
        
        CodeAnalyticData codeAnalyticData = analyticData.getCodeAnalyticData();
        
        dataContainer.add(FILES, ALL, codeAnalyticData.getAmountOfFiles());
        dataContainer.add(LOC, ALL, codeAnalyticData.getLinesOfCode());
        
        Set<String> languages = codeAnalyticData.getLanguages();
        for (String language: languages) {
            
            long lines = codeAnalyticData.getLinesOfCodeForLanguage(language);
            long files = codeAnalyticData.getAmountFilesForLanguage(language);
            
            dataContainer.add(LOC_LANG, new AnyTextAsKey(language), lines);
            dataContainer.add(FILES_LANG, new AnyTextAsKey(language), files);
            
        }
        return dataContainer;
    }

}
