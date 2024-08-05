// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.job;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import com.mercedesbenz.sechub.commons.pds.data.PDSJobStatus;
import com.mercedesbenz.sechub.commons.pds.data.PDSJobStatusState;
import com.mercedesbenz.sechub.pds.PDSNotFoundException;

public class PDSGetJobStatusServiceTest {

    private static final LocalDateTime CREATION_TIME = LocalDateTime.of(2020, 06, 23, 16, 35, 01);
    private static final LocalDateTime END_TIME = LocalDateTime.of(2020, 06, 23, 16, 37, 03);
    private PDSGetJobStatusService serviceToTest;
    private UUID jobUUID;
    private PDSJobRepository repository;
    private PDSJob job;

    @BeforeEach
    void beforeEach() throws Exception {
        repository = mock(PDSJobRepository.class);

        jobUUID = UUID.randomUUID();

        serviceToTest = new PDSGetJobStatusService();
        serviceToTest.repository = repository;
    }

    @ParameterizedTest
    @EnumSource(PDSJobStatusState.class)
    void get_status_works_for_any_state(PDSJobStatusState state) {
        /* prepare */
        prepareJob(state, END_TIME, false);

        /* execute */
        PDSJobStatus result = serviceToTest.getJobStatus(jobUUID);

        /* test */
        assertThat(result.getOwner()).isEqualTo(job.owner);
        assertThat(result.getCreated()).isEqualTo(CREATION_TIME.toString());
        assertThat(result.getEnded()).isEqualTo(END_TIME.toString());
        assertThat(result.getState()).isEqualTo(job.state);
        assertThat(result.isEncryptionOutOfSync()).isEqualTo(false);
    }

    @ParameterizedTest
    @EnumSource(PDSJobStatusState.class)
    void get_status_works_for_any_state_encryption_out_of_sync(PDSJobStatusState state) {
        /* prepare */
        prepareJob(state, END_TIME, true);

        /* execute */
        PDSJobStatus result = serviceToTest.getJobStatus(jobUUID);

        /* test */
        assertThat(result.getOwner()).isEqualTo(job.owner);
        assertThat(result.getCreated()).isEqualTo(CREATION_TIME.toString());
        assertThat(result.getEnded()).isEqualTo(END_TIME.toString());
        assertThat(result.getState()).isEqualTo(job.state);
        assertThat(result.isEncryptionOutOfSync()).isEqualTo(true);
    }

    @Test
    void job_not_found_throws_pds_not_found_exception() {
        /* prepare */
        UUID notExistingJobUUID = UUID.randomUUID();

        /* execute + test */
        assertThatThrownBy(() -> serviceToTest.getJobStatus(notExistingJobUUID)).isInstanceOf(PDSNotFoundException.class)
                .hasMessageContaining("Given job does not exist");

    }

    private PDSJob prepareJob(PDSJobStatusState state, LocalDateTime expectedEnded, boolean encryptionOutOfSync) {
        job = new PDSJob();
        job.uUID = jobUUID;
        job.created = CREATION_TIME;
        job.setOwner("theOwner");
        job.setEnded(expectedEnded);
        job.setState(state);
        job.setEncryptionOutOfSync(encryptionOutOfSync);

        when(repository.findById(jobUUID)).thenReturn(Optional.of(job));
        return job;
    }

}
