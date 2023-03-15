// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.statistic.job;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.mercedesbenz.sechub.domain.statistic.StatisticDataContainer;
import com.mercedesbenz.sechub.domain.statistic.StatisticDataKey;

class JobStatisticTransactionServiceTest {

    private JobStatisticTransactionService serviceToTest;
    private JobStatisticDataRepository jobStatisticDataRepository;
    private JobStatisticRepository jobStatisticRepository;
    private UUID jobUUID;

    @BeforeEach
    void beforeEach() {
        jobUUID = UUID.randomUUID();

        serviceToTest = new JobStatisticTransactionService();

        jobStatisticDataRepository = mock(JobStatisticDataRepository.class);
        jobStatisticRepository = mock(JobStatisticRepository.class);

        serviceToTest.jobStatisticDataRepository = jobStatisticDataRepository;
        serviceToTest.jobStatisticRepository = jobStatisticRepository;
    }

    @Test
    void createJobRunStatistic_call_saves_job_statistic() {
        /* prepare */
        LocalDateTime jobCreated = LocalDateTime.now();

        /* execute */
        serviceToTest.createJobStatistic(jobUUID, jobCreated, "project-y");

        /* test */
        ArgumentCaptor<JobStatistic> captor = ArgumentCaptor.forClass(JobStatistic.class);
        verify(jobStatisticRepository).save(captor.capture());

        JobStatistic value = captor.getValue();
        assertEquals(jobUUID, value.sechubJobUUID);
        assertEquals(jobCreated, value.created);
        assertEquals("project-y", value.projectId);

    }

    @Test
    void key_accepted_by_type_insertJobStatisticData_writes_job_statistic_data() {
        /* prepare */
        JobStatisticDataType type = JobStatisticDataType.UPLOAD_BINARIES;
        StatisticDataKey key = UploadJobStatisticDataKeys.SIZE_IN_BYTES;

        BigInteger value = BigInteger.valueOf(4711);

        /* check precondition */
        assertTrue(type.isKeyAccepted(key));

        /* execute */
        serviceToTest.insertJobStatisticData(jobUUID, type, key, value);

        /* test */
        ArgumentCaptor<JobStatisticData> captor = ArgumentCaptor.forClass(JobStatisticData.class);
        verify(jobStatisticDataRepository).save(captor.capture());

        JobStatisticData data = captor.getValue();
        assertNull(data.timeStamp); // time stamp is automatically set by Hibernate/DB
        assertEquals(jobUUID, data.sechubJobUUID);
        assertEquals(value, data.value);
        assertEquals("SIZE_IN_BYTES", data.id);
    }

    @Test
    void key_accepted_by_type_insertJobStatisticData_writes_job_statistic_data_input_as_map_one_entry() {
        /* prepare */
        JobStatisticDataType type = JobStatisticDataType.UPLOAD_BINARIES;
        StatisticDataKey key = UploadJobStatisticDataKeys.SIZE_IN_BYTES;

        BigInteger value = BigInteger.valueOf(4711);

        /* check precondition */
        assertTrue(type.isKeyAccepted(key));

        StatisticDataContainer<JobStatisticDataType> dataContainer = new StatisticDataContainer<>();
        dataContainer.add(type, key, value);

        /* execute */
        serviceToTest.insertJobStatisticData(jobUUID, dataContainer);

        /* test */
        ArgumentCaptor<JobStatisticData> captor = ArgumentCaptor.forClass(JobStatisticData.class);
        verify(jobStatisticDataRepository).save(captor.capture());

        JobStatisticData data = captor.getValue();
        assertNull(data.timeStamp); // time stamp is automatically set by Hibernate/DB
        assertEquals(jobUUID, data.sechubJobUUID);
        assertEquals(value, data.value);
        assertEquals(UploadJobStatisticDataKeys.SIZE_IN_BYTES.getKeyValue(), data.id);
    }

    @Test
    void some_keys_accepted_by_type_insertJobStatisticData_writes_job_statistic_data_input_as_map_four_entries() {
        /* prepare */
        JobStatisticDataType type1 = JobStatisticDataType.UPLOAD_BINARIES;
        StatisticDataKey key1 = UploadJobStatisticDataKeys.SIZE_IN_BYTES;

        BigInteger value1 = BigInteger.valueOf(4711);

        JobStatisticDataType type2 = JobStatisticDataType.UPLOAD_SOURCES;
        StatisticDataKey key2 = AnalyticStatisticDataKey.ALL;
        BigInteger value2 = BigInteger.valueOf(4712);

        JobStatisticDataType type3 = JobStatisticDataType.UPLOAD_SOURCES;
        StatisticDataKey key3a = UploadJobStatisticDataKeys.SIZE_IN_BYTES;

        BigInteger value3a = BigInteger.valueOf(4713);

        /* check precondition */
        assertTrue(type1.isKeyAccepted(key1));
        assertFalse(type2.isKeyAccepted(key2));
        assertTrue(type3.isKeyAccepted(key3a));

        StatisticDataContainer<JobStatisticDataType> dataContainer = new StatisticDataContainer<>();
        dataContainer.add(type1, key1, value1);
        dataContainer.add(type2, key2, value2);
        dataContainer.add(type3, key3a, value3a);

        /* execute */
        serviceToTest.insertJobStatisticData(jobUUID, dataContainer);

        /* test */
        ArgumentCaptor<JobStatisticData> captor = ArgumentCaptor.forClass(JobStatisticData.class);
        verify(jobStatisticDataRepository, times(2)).save(captor.capture());

        List<JobStatisticData> allValues = captor.getAllValues();
        assertEquals(2, allValues.size());// only type 2 is not accepted and not written!
        Iterator<JobStatisticData> it = allValues.iterator();
        JobStatisticData data1 = it.next();
        JobStatisticData data3 = it.next();

        // time stamp is automatically set by Hibernate/DB
        assertNull(data1.timeStamp);
        assertNull(data3.timeStamp);

        assertEquals(jobUUID, data1.sechubJobUUID);
        assertEquals(jobUUID, data3.sechubJobUUID);

        assertEquals(key1.getKeyValue(), data1.id);
        assertEquals(key3a.getKeyValue(), data3.id);

        assertEquals(value1, data1.value);
        assertEquals(value3a, data3.value);

    }

    @Test
    void key_not_accepted_by_type_insertJobStatisticData_does_not_write_job_statistic_data() {
        /* prepare */
        JobStatisticDataType type = JobStatisticDataType.UPLOAD_SOURCES;
        StatisticDataKey key = AnalyticStatisticDataKey.ALL;

        BigInteger value = BigInteger.valueOf(4711);

        /* check precondition */
        assertFalse(type.isKeyAccepted(key));

        /* execute */
        serviceToTest.insertJobStatisticData(jobUUID, type, key, value);

        /* test */
        verify(jobStatisticDataRepository, never()).save(any(JobStatisticData.class));

    }

}
