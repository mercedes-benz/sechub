package com.mercedesbenz.sechub.domain.schedule.encryption;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.domain.schedule.job.SecHubJobRepository;

class ScheduleCipherPoolCleanupServiceTest {

    private static final long POOL_ID_0 = 0L;
    private static final long POOL_ID_3 = 3L;
    private static final long POOL_ID_2 = 2L;
    private static final long POOL_ID_1 = 1L;
    private ScheduleCipherPoolCleanupService serviceToTest;
    private ScheduleEncryptionService encryptionService;
    private SecHubJobRepository jobRepository;
    private ScheduleCipherPoolDataRepository poolDataRepository;
    private ScheduleLatestCipherPoolDataCalculator latestCipherPoolDataCalculator;
    private SecHubOutdatedEncryptionPoolSupport outdatedEncryptionPoolSupport;

    @BeforeEach
    public void beforeEach() throws Exception {
        serviceToTest = new ScheduleCipherPoolCleanupService();

        encryptionService = mock(ScheduleEncryptionService.class);
        jobRepository = mock(SecHubJobRepository.class);
        poolDataRepository = mock(ScheduleCipherPoolDataRepository.class);
        latestCipherPoolDataCalculator = mock(ScheduleLatestCipherPoolDataCalculator.class);
        outdatedEncryptionPoolSupport = mock(SecHubOutdatedEncryptionPoolSupport.class);

        serviceToTest.encryptionService = encryptionService;
        serviceToTest.jobRepository = jobRepository;
        serviceToTest.poolDataRepository = poolDataRepository;
        serviceToTest.latestCipherPoolDataCalculator = latestCipherPoolDataCalculator;
        serviceToTest.outdatedEncryptionPoolSupport = outdatedEncryptionPoolSupport;
    }

    @Test
    void when_cluster_could_be_outdated_no_further_inspection_is_done() throws Exception {

        /* prepare */
        when(outdatedEncryptionPoolSupport.isOutdatedEncryptionPoolPossibleInCluster()).thenReturn(true);

        /* execute */
        serviceToTest.cleanupCipherPoolDataIfNecessaryAndPossible();

        /* test */
        verify(outdatedEncryptionPoolSupport).isOutdatedEncryptionPoolPossibleInCluster();

        verifyNoInteractions(encryptionService);
        verifyNoInteractions(jobRepository);
        verifyNoInteractions(poolDataRepository);
        verifyNoInteractions(latestCipherPoolDataCalculator);

    }

    @Test
    void no_pool_data_found_nothing_else_is_done() throws Exception {

        /* prepare */
        when(outdatedEncryptionPoolSupport.isOutdatedEncryptionPoolPossibleInCluster()).thenReturn(false);
        List<ScheduleCipherPoolData> list = new ArrayList<>();
        when(poolDataRepository.findAll()).thenReturn(list);

        /* execute */
        serviceToTest.cleanupCipherPoolDataIfNecessaryAndPossible();

        /* test */
        verify(outdatedEncryptionPoolSupport).isOutdatedEncryptionPoolPossibleInCluster();
        verify(poolDataRepository).findAll();

        verifyNoInteractions(encryptionService);
        verifyNoInteractions(jobRepository);
        verifyNoInteractions(latestCipherPoolDataCalculator);

    }

    @Test
    void pool_entries_exist_but_encryption_pool_has_not_latest_no_jobs_are_inspected() throws Exception {

        /* prepare */
        when(outdatedEncryptionPoolSupport.isOutdatedEncryptionPoolPossibleInCluster()).thenReturn(false);
        List<ScheduleCipherPoolData> list = new ArrayList<>();
        ScheduleCipherPoolData poolData1 = createTestPoolData(POOL_ID_1);
        list.add(poolData1);

        ScheduleCipherPoolData poolData2 = createTestPoolData(POOL_ID_2);
        list.add(poolData2);

        when(poolDataRepository.findAll()).thenReturn(list);
        when(latestCipherPoolDataCalculator.calculateLatestPoolData(list)).thenReturn(poolData2);

        when(encryptionService.getLatestCipherPoolId()).thenReturn(POOL_ID_1); // different than calculated

        /* execute */
        serviceToTest.cleanupCipherPoolDataIfNecessaryAndPossible();

        /* test */
        verify(encryptionService).getLatestCipherPoolId();
        verify(outdatedEncryptionPoolSupport).isOutdatedEncryptionPoolPossibleInCluster();
        verify(poolDataRepository).findAll();
        verify(latestCipherPoolDataCalculator).calculateLatestPoolData(list);

        verifyNoInteractions(jobRepository);

    }

    @Test
    void no_unused_pool_entries() throws Exception {

        /* prepare */
        when(outdatedEncryptionPoolSupport.isOutdatedEncryptionPoolPossibleInCluster()).thenReturn(false);
        List<ScheduleCipherPoolData> list = new ArrayList<>();
        ScheduleCipherPoolData poolData1 = createTestPoolData(POOL_ID_1);
        list.add(poolData1);

        ScheduleCipherPoolData poolData2 = createTestPoolData(POOL_ID_2);
        list.add(poolData2);

        when(poolDataRepository.findAll()).thenReturn(list);
        when(latestCipherPoolDataCalculator.calculateLatestPoolData(list)).thenReturn(poolData2);

        when(encryptionService.getLatestCipherPoolId()).thenReturn(POOL_ID_2);

        List<Long> collectList = List.of(POOL_ID_1, POOL_ID_2);
        when(jobRepository.collectAllUsedEncryptionPoolIdsInsideJobs()).thenReturn(collectList);

        /* execute */
        serviceToTest.cleanupCipherPoolDataIfNecessaryAndPossible();

        /* test */
        verify(outdatedEncryptionPoolSupport).isOutdatedEncryptionPoolPossibleInCluster();
        verify(poolDataRepository).findAll();
        verify(latestCipherPoolDataCalculator).calculateLatestPoolData(list);

        verify(jobRepository).collectAllUsedEncryptionPoolIdsInsideJobs();

        verify(poolDataRepository, never()).delete(any(ScheduleCipherPoolData.class));
        ;

    }

    @Test
    void unused_pool_entry_latest_is_used() throws Exception {

        /* prepare */
        when(outdatedEncryptionPoolSupport.isOutdatedEncryptionPoolPossibleInCluster()).thenReturn(false);
        List<ScheduleCipherPoolData> list = new ArrayList<>();
        ScheduleCipherPoolData poolData1 = createTestPoolData(POOL_ID_1);
        list.add(poolData1);

        ScheduleCipherPoolData poolData2 = createTestPoolData(POOL_ID_2);
        list.add(poolData2);

        ScheduleCipherPoolData poolData3 = createTestPoolData(POOL_ID_3);
        list.add(poolData3);

        when(poolDataRepository.findAll()).thenReturn(list);
        when(latestCipherPoolDataCalculator.calculateLatestPoolData(list)).thenReturn(poolData3);

        when(encryptionService.getLatestCipherPoolId()).thenReturn(POOL_ID_3);

        List<Long> collectList = List.of(POOL_ID_2, POOL_ID_3);
        when(jobRepository.collectAllUsedEncryptionPoolIdsInsideJobs()).thenReturn(collectList);

        /* execute */
        serviceToTest.cleanupCipherPoolDataIfNecessaryAndPossible();

        /* test */
        verify(outdatedEncryptionPoolSupport).isOutdatedEncryptionPoolPossibleInCluster();
        verify(poolDataRepository).findAll();
        verify(latestCipherPoolDataCalculator).calculateLatestPoolData(list);

        verify(jobRepository).collectAllUsedEncryptionPoolIdsInsideJobs();

        verify(poolDataRepository).delete(poolData1);
        verify(poolDataRepository, never()).delete(poolData2);
        verify(poolDataRepository, never()).delete(poolData3);

    }

    @Test
    void unused_pool_entry_latest_is_NOT_used() throws Exception {

        /* prepare */
        when(outdatedEncryptionPoolSupport.isOutdatedEncryptionPoolPossibleInCluster()).thenReturn(false);
        List<ScheduleCipherPoolData> list = new ArrayList<>();
        ScheduleCipherPoolData poolData1 = createTestPoolData(POOL_ID_1);
        list.add(poolData1);

        ScheduleCipherPoolData poolData2 = createTestPoolData(POOL_ID_2);
        list.add(poolData2);

        ScheduleCipherPoolData poolData3 = createTestPoolData(POOL_ID_3);
        list.add(poolData3);

        when(poolDataRepository.findAll()).thenReturn(list);
        when(latestCipherPoolDataCalculator.calculateLatestPoolData(list)).thenReturn(poolData3);

        when(encryptionService.getLatestCipherPoolId()).thenReturn(POOL_ID_3);

        List<Long> collectList = List.of(POOL_ID_2);
        when(jobRepository.collectAllUsedEncryptionPoolIdsInsideJobs()).thenReturn(collectList);

        /* execute */
        serviceToTest.cleanupCipherPoolDataIfNecessaryAndPossible();

        /* test */
        verify(outdatedEncryptionPoolSupport).isOutdatedEncryptionPoolPossibleInCluster();
        verify(poolDataRepository).findAll();
        verify(latestCipherPoolDataCalculator).calculateLatestPoolData(list);

        verify(jobRepository).collectAllUsedEncryptionPoolIdsInsideJobs();

        verify(poolDataRepository).delete(poolData1);
        verify(poolDataRepository, never()).delete(poolData2);
        verify(poolDataRepository, never()).delete(poolData3);// is not used, but latest ,so not deleted

    }

    @Test
    void two_unused_pool_entries_latest_is_NOT_used() throws Exception {

        /* prepare */
        when(outdatedEncryptionPoolSupport.isOutdatedEncryptionPoolPossibleInCluster()).thenReturn(false);
        List<ScheduleCipherPoolData> list = new ArrayList<>();
        ScheduleCipherPoolData poolData0 = createTestPoolData(POOL_ID_0);
        list.add(poolData0);

        ScheduleCipherPoolData poolData1 = createTestPoolData(POOL_ID_1);
        list.add(poolData1);

        ScheduleCipherPoolData poolData2 = createTestPoolData(POOL_ID_2);
        list.add(poolData2);

        ScheduleCipherPoolData poolData3 = createTestPoolData(POOL_ID_3);
        list.add(poolData3);

        when(poolDataRepository.findAll()).thenReturn(list);
        when(latestCipherPoolDataCalculator.calculateLatestPoolData(list)).thenReturn(poolData3);

        when(encryptionService.getLatestCipherPoolId()).thenReturn(POOL_ID_3);

        List<Long> collectList = List.of(POOL_ID_2);
        when(jobRepository.collectAllUsedEncryptionPoolIdsInsideJobs()).thenReturn(collectList);

        /* execute */
        serviceToTest.cleanupCipherPoolDataIfNecessaryAndPossible();

        /* test */
        verify(outdatedEncryptionPoolSupport).isOutdatedEncryptionPoolPossibleInCluster();
        verify(poolDataRepository).findAll();
        verify(latestCipherPoolDataCalculator).calculateLatestPoolData(list);

        verify(jobRepository).collectAllUsedEncryptionPoolIdsInsideJobs();

        verify(poolDataRepository).delete(poolData0);
        verify(poolDataRepository).delete(poolData1);
        verify(poolDataRepository, never()).delete(poolData2);
        verify(poolDataRepository, never()).delete(poolData3);// is not used, but latest ,so not deleted

    }

    private ScheduleCipherPoolData createTestPoolData(long poolId) {
        ScheduleCipherPoolData poolData1 = mock(ScheduleCipherPoolData.class, "Test pool data:" + poolId);
        when(poolData1.getId()).thenReturn(poolId);
        return poolData1;
    }

}
