// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.statistic.job;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;

import com.mercedesbenz.sechub.commons.model.TrafficLight;
import com.mercedesbenz.sechub.domain.statistic.AnyTextAsKey;
import com.mercedesbenz.sechub.domain.statistic.StatisticDataContainer;
import com.mercedesbenz.sechub.domain.statistic.StatisticDataKey;

class JobRunStatisticTransactionServiceTest {

    private JobRunStatisticTransactionService serviceToTest;
    private JobRunStatisticDataRepository jobRunStatisticDataRepository;
    private JobRunStatisticRepository jobRunStatisticRepository;
    private JobStatisticRepository jobStatisticRepository;
    private UUID jobUUID;
    private UUID executionUUID;

    @BeforeEach
    void beforeEach() {
        jobUUID = UUID.randomUUID();
        executionUUID = UUID.randomUUID();

        serviceToTest = new JobRunStatisticTransactionService();

        jobRunStatisticDataRepository = mock(JobRunStatisticDataRepository.class);
        jobRunStatisticRepository = mock(JobRunStatisticRepository.class);
        jobStatisticRepository = mock(JobStatisticRepository.class);

        serviceToTest.jobRunStatisticDataRepository = jobRunStatisticDataRepository;
        serviceToTest.jobRunStatisticRepository = jobRunStatisticRepository;
        serviceToTest.jobStatisticRepository = jobStatisticRepository;
    }

    /**
     * Some JobStatistic entity data will be duplicated by the creation of a new job
     * run statistic entity.
     */
    @Test
    void job_statistic_data_exists_createJobRunStatistic_call_saves_job_run_statistic() {
        /* prepare */
        LocalDateTime started = LocalDateTime.now();
        JobStatistic jobStatistic = new JobStatistic();
        jobStatistic.setSechubJobUUID(jobUUID);
        jobStatistic.setProjectId("project-x");

        when(jobStatisticRepository.findById(jobUUID)).thenReturn(Optional.of(jobStatistic));

        /* execute */
        serviceToTest.createJobRunStatistic(executionUUID, jobUUID, started);

        /* test */
        ArgumentCaptor<JobRunStatistic> captor = ArgumentCaptor.forClass(JobRunStatistic.class);
        verify(jobRunStatisticRepository).save(captor.capture());

        JobRunStatistic value = captor.getValue();
        assertEquals(executionUUID, value.executionUUID);
        assertFalse(value.failed);
        assertNull(value.trafficLight);
        assertEquals(started, value.started);
        assertEquals("project-x", value.projectId);

    }

    @EnumSource(TrafficLight.class)
    @ParameterizedTest
    void markJobRunEnded(TrafficLight trafficLight) {
        // we emulate failure here via traffic light
        boolean failed = TrafficLight.OFF.equals(trafficLight);

        LocalDateTime creationTime = LocalDateTime.now().minusSeconds(30);
        LocalDateTime ended = LocalDateTime.now();

        JobRunStatistic formerCreated = new JobRunStatistic();
        formerCreated.created = creationTime;
        formerCreated.projectId = "p1";
        formerCreated.executionUUID = executionUUID;
        formerCreated.sechubJobUUID = UUID.randomUUID();

        when(jobRunStatisticRepository.findById(executionUUID)).thenReturn(Optional.of(formerCreated));

        /* execute */
        serviceToTest.markJobRunEnded(executionUUID, trafficLight, ended, failed);

        /* test */
        ArgumentCaptor<JobRunStatistic> captor = ArgumentCaptor.forClass(JobRunStatistic.class);
        verify(jobRunStatisticRepository).save(captor.capture());

        JobRunStatistic value = captor.getValue();
        assertEquals(executionUUID, value.executionUUID);
        assertEquals(failed, value.failed);
        assertEquals(creationTime, value.created);
        assertNotNull(value.ended);
        assertTrue(value.ended.isAfter(value.created));

    }

    /**
     * JobStatistic entity is written when the job is created and contains the
     * creation timestamp of the job. To provide easier SQL joins in statistic
     * reporting tools, we duplicate some project information into the run data, but
     * we must fetch those data... from JobStatistic.
     *
     * If none exists, we cannot duplicate and so a write is not possible.
     */
    @Test
    void job_statistic_data_does_not_exist_createJobRunStatistic_call_saves_no_job_run_statistic() {
        /* prepare */
        LocalDateTime started = LocalDateTime.now();
        when(jobStatisticRepository.findById(jobUUID)).thenReturn(Optional.empty());

        /* execute */
        serviceToTest.createJobRunStatistic(executionUUID, jobUUID, started);

        /* test */
        verify(jobRunStatisticRepository, never()).save(any(JobRunStatistic.class));

    }

    @Test
    void key_accepted_by_type_insertJobRunStatisticData_writes_job_run_statistic_data() {
        /* prepare */
        JobRunStatisticDataType type = JobRunStatisticDataType.LOC;
        StatisticDataKey key = AnalyticStatisticDataKey.ALL;

        BigInteger value = BigInteger.valueOf(4711);

        /* check precondition */
        assertTrue(type.isKeyAccepted(key));

        /* execute */
        serviceToTest.insertJobRunStatisticData(executionUUID, type, key, value);

        /* test */
        ArgumentCaptor<JobRunStatisticData> captor = ArgumentCaptor.forClass(JobRunStatisticData.class);
        verify(jobRunStatisticDataRepository).save(captor.capture());

        JobRunStatisticData data = captor.getValue();
        assertNull(data.timeStamp); // time stamp is automatically set by Hibernate/DB
        assertEquals(executionUUID, data.executionUUID);
        assertEquals(value, data.value);
        assertEquals("ALL", data.id);
    }

    @Test
    void key_accepted_by_type_insertJobRunStatisticData_writes_job_run_statistic_data_input_as_map_one_entry() {
        /* prepare */
        JobRunStatisticDataType type = JobRunStatisticDataType.LOC;
        StatisticDataKey key = AnalyticStatisticDataKey.ALL;

        BigInteger value = BigInteger.valueOf(4711);

        /* check precondition */
        assertTrue(type.isKeyAccepted(key));

        StatisticDataContainer<JobRunStatisticDataType> dataContainer = new StatisticDataContainer<>();
        dataContainer.add(type, key, value);

        /* execute */
        serviceToTest.insertJobRunStatisticData(executionUUID, dataContainer);

        /* test */
        ArgumentCaptor<JobRunStatisticData> captor = ArgumentCaptor.forClass(JobRunStatisticData.class);
        verify(jobRunStatisticDataRepository).save(captor.capture());

        JobRunStatisticData data = captor.getValue();
        assertNull(data.timeStamp); // time stamp is automatically set by Hibernate/DB
        assertEquals(executionUUID, data.executionUUID);
        assertEquals(value, data.value);
        assertEquals(AnalyticStatisticDataKey.ALL.getKeyValue(), data.id);
    }

    @Test
    void some_keys_accepted_by_type_insertJobRunStatisticData_writes_job_run_statistic_data_input_as_map_four_entries() {
        /* prepare */
        JobRunStatisticDataType type1 = JobRunStatisticDataType.LOC;
        StatisticDataKey key1 = AnalyticStatisticDataKey.ALL;

        BigInteger value1 = BigInteger.valueOf(4711);

        JobRunStatisticDataType type2 = JobRunStatisticDataType.LOC_LANG;
        StatisticDataKey key2 = AnalyticStatisticDataKey.ALL;

        BigInteger value2 = BigInteger.valueOf(4712);

        JobRunStatisticDataType type3 = JobRunStatisticDataType.LOC_LANG;
        StatisticDataKey key3a = new AnyTextAsKey("lang1");
        StatisticDataKey key3b = new AnyTextAsKey("lang1");

        BigInteger value3a = BigInteger.valueOf(4713);
        BigInteger value3b = BigInteger.valueOf(5713);

        /* check precondition */
        assertTrue(type1.isKeyAccepted(key1));
        assertFalse(type2.isKeyAccepted(key2));
        assertTrue(type3.isKeyAccepted(key3a));
        assertTrue(type3.isKeyAccepted(key3b));

        StatisticDataContainer<JobRunStatisticDataType> dataContainer = new StatisticDataContainer<>();
        dataContainer.add(type1, key1, value1);
        dataContainer.add(type2, key2, value2);
        dataContainer.add(type3, key3a, value3a);
        dataContainer.add(type3, key3b, value3b);

        /* execute */
        serviceToTest.insertJobRunStatisticData(executionUUID, dataContainer);

        /* test */
        ArgumentCaptor<JobRunStatisticData> captor = ArgumentCaptor.forClass(JobRunStatisticData.class);
        verify(jobRunStatisticDataRepository, times(3)).save(captor.capture());

        List<JobRunStatisticData> allValues = captor.getAllValues();
        assertEquals(3, allValues.size());// only type 2 is not accepted and not written!
        Iterator<JobRunStatisticData> it = allValues.iterator();
        JobRunStatisticData data1 = it.next();
        JobRunStatisticData data3a = it.next();
        JobRunStatisticData data3b = it.next();

        // time stamp is automatically set by Hibernate/DB
        assertNull(data1.timeStamp);
        assertNull(data3a.timeStamp);
        assertNull(data3b.timeStamp);

        assertEquals(executionUUID, data1.executionUUID);
        assertEquals(executionUUID, data3a.executionUUID);
        assertEquals(executionUUID, data3b.executionUUID);

        assertEquals(key1.getKeyValue(), data1.id);
        assertEquals(key3a.getKeyValue(), data3a.id);
        assertEquals(key3b.getKeyValue(), data3b.id);

        assertEquals(value1, data1.value);
        assertEquals(value3a, data3a.value);
        assertEquals(value3b, data3b.value);

    }

    @Test
    void key_not_accepted_by_type_insertJobRunStatisticData_does_not_write_job_run_statistic_data() {
        /* prepare */
        JobRunStatisticDataType type = JobRunStatisticDataType.LOC;
        StatisticDataKey key = new AnyTextAsKey("testkey"); // not accepted by LOC type

        BigInteger value = BigInteger.valueOf(4711);

        /* check precondition */
        assertFalse(type.isKeyAccepted(key));

        /* execute */
        serviceToTest.insertJobRunStatisticData(executionUUID, type, key, value);

        /* test */
        verify(jobRunStatisticDataRepository, never()).save(any(JobRunStatisticData.class));

    }

}
