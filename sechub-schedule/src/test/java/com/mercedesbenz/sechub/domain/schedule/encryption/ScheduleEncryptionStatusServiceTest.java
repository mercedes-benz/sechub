package com.mercedesbenz.sechub.domain.schedule.encryption;

import static org.assertj.core.api.Assertions.*;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.commons.model.job.ExecutionState;
import com.mercedesbenz.sechub.domain.schedule.job.SecHubJobRepository;
import com.mercedesbenz.sechub.sharedkernel.encryption.SecHubCipherAlgorithm;
import com.mercedesbenz.sechub.sharedkernel.encryption.SecHubCipherPasswordSourceType;
import com.mercedesbenz.sechub.sharedkernel.encryption.SecHubDomainEncryptionData;
import com.mercedesbenz.sechub.sharedkernel.encryption.SecHubDomainEncryptionStatus;

class ScheduleEncryptionStatusServiceTest {

    private ScheduleEncryptionStatusService serviceToTest;
    private ScheduleCipherPoolDataRepository poolDataRepository;
    private SecHubJobRepository jobRepository;

    @BeforeEach
    void beforeEach() {

        poolDataRepository = mock(ScheduleCipherPoolDataRepository.class);
        jobRepository = mock(SecHubJobRepository.class);

        serviceToTest = new ScheduleEncryptionStatusService();
        serviceToTest.poolDataRepository = poolDataRepository;
        serviceToTest.jobRepository = jobRepository;
    }

    @Test
    void createEncryptionStatus__no_pool_data() {
        /* prepare */
        when(poolDataRepository.findAll()).thenReturn(List.of());

        /* execute */
        SecHubDomainEncryptionStatus status = serviceToTest.createEncryptionStatus();

        /* test */
        assertThat(status).isNotNull();
        assertThat(status.getData()).isEmpty();

    }

    @Test
    void createEncryptionStatus__one_pool_data() {

        /* prepare */
        ScheduleCipherPoolData poolData1 = mock(ScheduleCipherPoolData.class);
        LocalDateTime creationTime = LocalDateTime.now();
        String user = "user1";
        String passwordSourceData = "the-data";
        SecHubCipherPasswordSourceType passwordSourceType = SecHubCipherPasswordSourceType.ENVIRONMENT_VARIABLE;
        long poolid = 33L;

        long count1Initializing = 1L;
        long count1Canceled = 20L;
        long count1Ended = 1023L;

        when(poolData1.getId()).thenReturn(poolid);
        when(poolData1.getAlgorithm()).thenReturn(SecHubCipherAlgorithm.AES_GCM_SIV_128);
        when(poolData1.getCreated()).thenReturn(creationTime);
        when(poolData1.getCreatedFrom()).thenReturn(user);
        when(poolData1.getPasswordSourceData()).thenReturn(passwordSourceData);
        when(poolData1.getPasswordSourceType()).thenReturn(passwordSourceType);

        when(poolDataRepository.findAll()).thenReturn(List.of(poolData1));
        when(jobRepository.countJobsInExecutionStateAndEncryptedWithPoolId(ExecutionState.INITIALIZING, poolid)).thenReturn(count1Initializing);
        when(jobRepository.countJobsInExecutionStateAndEncryptedWithPoolId(ExecutionState.CANCELED, poolid)).thenReturn(count1Canceled);
        when(jobRepository.countJobsInExecutionStateAndEncryptedWithPoolId(ExecutionState.ENDED, poolid)).thenReturn(count1Ended);

        /* execute */
        SecHubDomainEncryptionStatus status = serviceToTest.createEncryptionStatus();

        /* test */
        assertThat(status).isNotNull();
        assertThat(status.getData()).isNotEmpty().hasSize(1);

        SecHubDomainEncryptionData data1 = status.getData().iterator().next();
        assertThat(data1.getId()).isEqualTo(String.valueOf(poolid));
        assertThat(data1.getCreated()).isEqualTo(creationTime);
        assertThat(data1.getCreatedFrom()).isEqualTo(user);
        assertThat(data1.getPasswordSource().getType()).isEqualTo(passwordSourceType);
        assertThat(data1.getPasswordSource().getData()).isEqualTo(passwordSourceData);

        assertThat(data1.getUsage()).containsEntry("job.state.canceled", count1Canceled);
        assertThat(data1.getUsage()).containsEntry("job.state.initializing", count1Initializing);
        assertThat(data1.getUsage()).containsEntry("job.state.ended", count1Ended);

    }

    @Test
    void createEncryptionStatus__two_pool_data() {

        /* prepare */
        ScheduleCipherPoolData poolData0 = mock(ScheduleCipherPoolData.class);
        LocalDateTime creationTime0 = LocalDateTime.now();
        String user0 = "user0";
        String passwordSourceData0 = "the-data0";
        SecHubCipherPasswordSourceType passwordSourceType0 = SecHubCipherPasswordSourceType.NONE;
        long poolid0 = 12;

        long count0Initializing = 10L;
        long count0Canceled = 30L;
        long count0Ended = 2023L;
        long count0CancelRequest = 3;

        when(poolData0.getId()).thenReturn(poolid0);
        when(poolData0.getAlgorithm()).thenReturn(SecHubCipherAlgorithm.AES_GCM_SIV_128);
        when(poolData0.getCreated()).thenReturn(creationTime0);
        when(poolData0.getCreatedFrom()).thenReturn(user0);
        when(poolData0.getPasswordSourceData()).thenReturn(passwordSourceData0);
        when(poolData0.getPasswordSourceType()).thenReturn(passwordSourceType0);

        when(jobRepository.countJobsInExecutionStateAndEncryptedWithPoolId(ExecutionState.INITIALIZING, poolid0)).thenReturn(count0Initializing);
        when(jobRepository.countJobsInExecutionStateAndEncryptedWithPoolId(ExecutionState.CANCELED, poolid0)).thenReturn(count0Canceled);
        when(jobRepository.countJobsInExecutionStateAndEncryptedWithPoolId(ExecutionState.ENDED, poolid0)).thenReturn(count0Ended);
        when(jobRepository.countJobsInExecutionStateAndEncryptedWithPoolId(ExecutionState.CANCEL_REQUESTED, poolid0)).thenReturn(count0CancelRequest);

        ScheduleCipherPoolData poolData1 = mock(ScheduleCipherPoolData.class);
        LocalDateTime creationTime1 = LocalDateTime.now();
        String user = "user1";
        String passwordSourceData1 = "the-data";
        SecHubCipherPasswordSourceType passwordSourceType1 = SecHubCipherPasswordSourceType.ENVIRONMENT_VARIABLE;
        long poolid1 = 33L;

        long count1Initializing = 1L;
        long count1Canceled = 20L;
        long count1Ended = 1023L;

        when(poolData1.getId()).thenReturn(poolid1);
        when(poolData1.getAlgorithm()).thenReturn(SecHubCipherAlgorithm.AES_GCM_SIV_128);
        when(poolData1.getCreated()).thenReturn(creationTime1);
        when(poolData1.getCreatedFrom()).thenReturn(user);
        when(poolData1.getPasswordSourceData()).thenReturn(passwordSourceData1);
        when(poolData1.getPasswordSourceType()).thenReturn(passwordSourceType1);

        when(jobRepository.countJobsInExecutionStateAndEncryptedWithPoolId(ExecutionState.INITIALIZING, poolid1)).thenReturn(count1Initializing);
        when(jobRepository.countJobsInExecutionStateAndEncryptedWithPoolId(ExecutionState.CANCELED, poolid1)).thenReturn(count1Canceled);
        when(jobRepository.countJobsInExecutionStateAndEncryptedWithPoolId(ExecutionState.ENDED, poolid1)).thenReturn(count1Ended);

        when(poolDataRepository.findAll()).thenReturn(List.of(poolData0, poolData1));

        /* execute */
        SecHubDomainEncryptionStatus status = serviceToTest.createEncryptionStatus();

        /* test */
        assertThat(status).isNotNull();
        assertThat(status.getData()).isNotEmpty().hasSize(2);

        Iterator<SecHubDomainEncryptionData> iterator = status.getData().iterator();
        SecHubDomainEncryptionData data0 = iterator.next();
        assertThat(data0.getId()).isEqualTo(String.valueOf(poolid0));
        assertThat(data0.getCreated()).isEqualTo(creationTime0);
        assertThat(data0.getCreatedFrom()).isEqualTo(user0);
        assertThat(data0.getPasswordSource().getType()).isEqualTo(passwordSourceType0);
        assertThat(data0.getPasswordSource().getData()).isEqualTo(passwordSourceData0);

        assertThat(data0.getUsage()).containsEntry("job.state.canceled", count0Canceled);
        assertThat(data0.getUsage()).containsEntry("job.state.initializing", count0Initializing);
        assertThat(data0.getUsage()).containsEntry("job.state.ended", count0Ended);
        assertThat(data0.getUsage()).containsEntry("job.state.cancel_requested", count0CancelRequest);

        SecHubDomainEncryptionData data1 = iterator.next();
        assertThat(data1.getId()).isEqualTo(String.valueOf(poolid1));
        assertThat(data1.getCreated()).isEqualTo(creationTime1);
        assertThat(data1.getCreatedFrom()).isEqualTo(user);
        assertThat(data1.getPasswordSource().getType()).isEqualTo(passwordSourceType1);
        assertThat(data1.getPasswordSource().getData()).isEqualTo(passwordSourceData1);

        assertThat(data1.getUsage()).containsEntry("job.state.canceled", count1Canceled);
        assertThat(data1.getUsage()).containsEntry("job.state.initializing", count1Initializing);
        assertThat(data1.getUsage()).containsEntry("job.state.ended", count1Ended);

    }

}
