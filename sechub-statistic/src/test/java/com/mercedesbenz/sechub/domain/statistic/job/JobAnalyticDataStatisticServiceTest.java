package com.mercedesbenz.sechub.domain.statistic.job;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.mercedesbenz.sechub.domain.statistic.StatisticDataContainer;
import com.mercedesbenz.sechub.domain.statistic.StatisticDataKeyValue;
import com.mercedesbenz.sechub.sharedkernel.analytic.AnalyticData;
import com.mercedesbenz.sechub.sharedkernel.analytic.CodeAnalyticData;

class JobAnalyticDataStatisticServiceTest {

    private JobAnalyticDataStatisticService serviceToTest;
    private UUID executionUUID;
    private JobRunStatisticTransactionService jobRunStatistictTansactionService;

    @BeforeEach
    void beforeEach() {

        jobRunStatistictTansactionService = mock(JobRunStatisticTransactionService.class);

        serviceToTest = new JobAnalyticDataStatisticService();
        serviceToTest.jobRunStatistictTansactionService = jobRunStatistictTansactionService;
        executionUUID = UUID.randomUUID();
    }

    @SuppressWarnings("unchecked")
    @Test
    void storeStatisticData_calls_transaction_services_even_when_no_code_data() {
        /* prepare */
        AnalyticData data = new AnalyticData();

        /* execute */
        serviceToTest.storeStatisticData(executionUUID, data);
        
        /* test */
        ArgumentCaptor<StatisticDataContainer<JobRunStatisticDataType>> dataContainerCaptor = ArgumentCaptor.forClass(StatisticDataContainer.class);
        verify(jobRunStatistictTansactionService).insertJobRunStatisticData(eq(executionUUID), dataContainerCaptor.capture());
  
        StatisticDataContainer<JobRunStatisticDataType> dataContainer = dataContainerCaptor.getValue();
        Set<JobRunStatisticDataType> types = dataContainer.getTypes();
        assertTrue(types.isEmpty());
        
    }

    @SuppressWarnings("unchecked")
    @Test
    void storeStatisticData_writes_loc_files_loc_lan_and_files_lang() {
        /* prepare */
        CodeAnalyticData codeAnalyticData = new CodeAnalyticData();
        codeAnalyticData.setFilesForLanguage("java", 1000L);
        codeAnalyticData.setFilesForLanguage("go", 500L);
        codeAnalyticData.setLinesOfCodeForLanguage("java", 2500L);
        codeAnalyticData.setLinesOfCodeForLanguage("go", 1500L);

        AnalyticData data = new AnalyticData();
        data.setCodeAnalyticData(codeAnalyticData);

        /* execute */
        serviceToTest.storeStatisticData(executionUUID, data);

        /* test */
        ArgumentCaptor<StatisticDataContainer<JobRunStatisticDataType>> dataContainerCaptor = ArgumentCaptor.forClass(StatisticDataContainer.class);
        verify(jobRunStatistictTansactionService).insertJobRunStatisticData(eq(executionUUID), dataContainerCaptor.capture());

        // check statistic types stored as expected
        StatisticDataContainer<JobRunStatisticDataType> dataContainer = dataContainerCaptor.getValue();
        Set<JobRunStatisticDataType> types = dataContainer.getTypes();

        assertTrue(types.contains(JobRunStatisticDataType.FILES));
        assertTrue(types.contains(JobRunStatisticDataType.FILES_LANG));

        assertTrue(types.contains(JobRunStatisticDataType.LOC_LANG));
        assertTrue(types.contains(JobRunStatisticDataType.LOC));

        assertEquals(4, types.size());

        // check files
        List<StatisticDataKeyValue> files = dataContainer.getKeyValues(JobRunStatisticDataType.FILES);
        assertEquals(1, files.size());
        StatisticDataKeyValue file1 = files.iterator().next();
        assertEquals(AnalyticStatisticDataKey.ALL, file1.getKey());
        assertEquals(BigInteger.valueOf(1500L), file1.getValue());

        // check loc
        List<StatisticDataKeyValue> linesOfCodeList = dataContainer.getKeyValues(JobRunStatisticDataType.LOC);
        assertEquals(1, linesOfCodeList.size());
        StatisticDataKeyValue linesOfCode = linesOfCodeList.iterator().next();
        assertEquals(AnalyticStatisticDataKey.ALL, linesOfCode.getKey());
        assertEquals(BigInteger.valueOf(4000L), linesOfCode.getValue());

        // check files per language
        List<StatisticDataKeyValue> filesPerLanguage = dataContainer.getKeyValues(JobRunStatisticDataType.FILES_LANG);
        assertEquals(2, filesPerLanguage.size());

        Map<String, BigInteger> filesLangMap = new HashMap<>();
        for (StatisticDataKeyValue fileKeyValue : filesPerLanguage) {
            String keyString = fileKeyValue.getKey().getKeyValue();
            filesLangMap.put(keyString, fileKeyValue.getValue());
        }
        assertEquals(BigInteger.valueOf(1000L), filesLangMap.get("java"));
        assertEquals(BigInteger.valueOf(500L), filesLangMap.get("go"));

        // check lines of code per language
        List<StatisticDataKeyValue> locPerLanguage = dataContainer.getKeyValues(JobRunStatisticDataType.LOC_LANG);
        assertEquals(2, locPerLanguage.size());

        Map<String, BigInteger> locPerLanguageMap = new HashMap<>();
        for (StatisticDataKeyValue locKeyValue : locPerLanguage) {
            String keyString = locKeyValue.getKey().getKeyValue();
            locPerLanguageMap.put(keyString, locKeyValue.getValue());
        }
        assertEquals(BigInteger.valueOf(2500L), locPerLanguageMap.get("java"));
        assertEquals(BigInteger.valueOf(1500L), locPerLanguageMap.get("go"));

    }

}
