// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.schedule.job;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.commons.encryption.InitializationVector;
import com.mercedesbenz.sechub.commons.model.SecHubConfigurationModelSupport;
import com.mercedesbenz.sechub.domain.schedule.encryption.ScheduleEncryptionResult;
import com.mercedesbenz.sechub.domain.schedule.encryption.ScheduleEncryptionService;
import com.mercedesbenz.sechub.sharedkernel.UserContextService;
import com.mercedesbenz.sechub.sharedkernel.configuration.SecHubConfiguration;

public class JobFactoryTest {

    private static final String UNENCRYPTED_CONFIGURATION = "i-am-the-origin-configuration";
    private static final Long LATEST_CIPHER_POOL_ID = Long.valueOf(1234);
    private static final byte[] ENCRYPTED_CONFIGURATION_DATA = "encrypted-data".getBytes();
    private static byte[] ENCRYPTION_INITIAL_VECTOR = "initial-vector".getBytes();

    private SecHubJobFactory factoryToTest;
    private SecHubConfiguration configuration;

    private ScheduleEncryptionService encryptionService;

    @BeforeEach
    void beforeEach() {

        factoryToTest = new SecHubJobFactory();

        encryptionService = mock(ScheduleEncryptionService.class);

        configuration = mock(SecHubConfiguration.class);
        when(configuration.toJSON()).thenReturn(UNENCRYPTED_CONFIGURATION);

        factoryToTest.userContextService = mock(UserContextService.class);
        factoryToTest.modelSupport = mock(SecHubConfigurationModelSupport.class);
        factoryToTest.encryptionService = encryptionService;

        ScheduleEncryptionResult encryptionResult = mock(ScheduleEncryptionResult.class);
        when(encryptionResult.getCipherPoolId()).thenReturn(LATEST_CIPHER_POOL_ID);
        when(encryptionResult.getEncryptedData()).thenReturn(ENCRYPTED_CONFIGURATION_DATA);
        when(encryptionResult.getInitialVector()).thenReturn(new InitializationVector(ENCRYPTION_INITIAL_VECTOR));

        when(encryptionService.encryptWithLatestCipher(UNENCRYPTED_CONFIGURATION)).thenReturn(encryptionResult);

        when(factoryToTest.userContextService.getUserId()).thenReturn("user1");
    }

    @Test
    void new_jobs_have_creation_time_stamp() throws Exception {

        /* execute */
        ScheduleSecHubJob job = factoryToTest.createJob(configuration);

        /* test */
        assertThat(job).isNotNull();
        assertThat(job.getCreated()).isNotNull();

    }

    @Test
    void new_jobs_have_configuration_encrypted_by_encryption_service() throws Exception {

        /* execute */
        ScheduleSecHubJob job = factoryToTest.createJob(configuration);

        /* test */
        assertThat(job.getEncryptedConfiguration()).isEqualTo(ENCRYPTED_CONFIGURATION_DATA);

    }

    @Test
    void new_jobs_have_encryption_initial_vector_from_encryption_service() throws Exception {

        /* execute */
        ScheduleSecHubJob job = factoryToTest.createJob(configuration);

        /* test */
        assertThat(job.getEncryptionInitialVectorData()).isEqualTo(ENCRYPTION_INITIAL_VECTOR);

    }

    @Test
    void new_jobs_have_lastet_encryption_pool_id_from_encryption_service() throws Exception {

        /* execute */
        ScheduleSecHubJob job = factoryToTest.createJob(configuration);

        /* test */
        assertThat(job.getEncryptionCipherPoolId()).isEqualTo(LATEST_CIPHER_POOL_ID);

    }

    @Test
    public void factory_throws_illegal_state_exception_when_no_user() throws Exception {
        /* prepare */
        when(factoryToTest.userContextService.getUserId()).thenReturn(null);

        /* execute */
        assertThatThrownBy(()-> factoryToTest.createJob(configuration)).isInstanceOf(IllegalStateException.class);

    }

}
