package com.mercedesbenz.sechub.pds.job;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.commons.encryption.InitializationVector;
import com.mercedesbenz.sechub.pds.encryption.PDSEncryptionException;
import com.mercedesbenz.sechub.pds.encryption.PDSEncryptionService;
import com.mercedesbenz.sechub.test.TestCanaryException;

class PDSJobConfigurationAccessTest {

    private PDSJobConfigurationAccess accessToTest;
    private PDSEncryptionService encryptionService;

    @BeforeEach
    public void beforeEach() throws Exception {
        accessToTest = new PDSJobConfigurationAccess();
        encryptionService = mock(PDSEncryptionService.class);

        accessToTest.encryptionService = encryptionService;
    }

    @Test
    void access_can_resolve_pds_job_configuration_from_encrypted_bytes() throws Exception {

        /* prepare */
        byte[] persistedInitialVector = "initme".getBytes();
        byte[] persistedEncryptedConfiguration = "i-am-encrypted".getBytes();
        InitializationVector givenVector = new InitializationVector(persistedInitialVector);

        PDSJob job = mock(PDSJob.class);
        when(job.getEncryptionInitialVectorData()).thenReturn(persistedInitialVector);
        when(job.getEncryptedConfiguration()).thenReturn(persistedEncryptedConfiguration);

        PDSJobConfiguration configuration = new PDSJobConfiguration();
        String jsonAsPlainText = configuration.toJSON();

        when(encryptionService.decryptString(eq(persistedEncryptedConfiguration), eq(givenVector))).thenReturn(jsonAsPlainText);

        /* execute */
        PDSJobConfiguration result = accessToTest.resolveUnEncryptedJobConfiguration(job);

        /* test */
        assertThat(result).isNotNull();
        assertThat(result.toJSON()).isEqualTo(jsonAsPlainText);

    }

    @Test
    void access_throws_pds_encryption_exception_when_job_has_encrypted_data_but_encryption_service_throws_an_exception() {
        /* prepare */
        PDSJob job = mock(PDSJob.class);
        when(encryptionService.decryptString(any(), any())).thenThrow(new TestCanaryException());
        when(job.getEncryptedConfiguration()).thenReturn("something".getBytes());
        when(job.getEncryptionInitialVectorData()).thenReturn("init".getBytes());

        /* execute */
        assertThatThrownBy(() -> accessToTest.resolveUnEncryptedJobConfiguration(job)).isInstanceOf(PDSEncryptionException.class)
                .hasRootCauseInstanceOf(TestCanaryException.class);
    }

    @Test
    void access_throws_pds_encryption_exception_when_job_has_NO_encrypted_data() {
        /* prepare */
        PDSJob job = mock(PDSJob.class);
        UUID uuid = UUID.randomUUID();
        when(job.getUUID()).thenReturn(uuid);
        when(job.getEncryptedConfiguration()).thenReturn(null);
        when(job.getEncryptionInitialVectorData()).thenReturn("init".getBytes());

        /* execute */
        assertThatThrownBy(() -> accessToTest.resolveUnEncryptedJobConfiguration(job)).isInstanceOf(PDSEncryptionException.class)
                .hasRootCauseInstanceOf(IllegalStateException.class).hasRootCauseMessage("No encrypted configuration found for PDS job: " + uuid);
    }

    @Test
    void access_throws_pds_encryption_exception_when_job_has_NO_initial_vector_data() {
        /* prepare */
        PDSJob job = mock(PDSJob.class);
        UUID uuid = UUID.randomUUID();
        when(job.getUUID()).thenReturn(uuid);
        when(job.getEncryptedConfiguration()).thenReturn("config".getBytes());
        when(job.getEncryptionInitialVectorData()).thenReturn(null);

        /* execute */
        assertThatThrownBy(() -> accessToTest.resolveUnEncryptedJobConfiguration(job)).isInstanceOf(PDSEncryptionException.class)
                .hasRootCauseInstanceOf(IllegalStateException.class).hasRootCauseMessage("No initial vector data found for PDS job: " + uuid);
    }

}
