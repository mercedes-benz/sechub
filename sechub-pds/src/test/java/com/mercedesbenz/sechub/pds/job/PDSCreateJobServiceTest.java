// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.job;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.mercedesbenz.sechub.commons.encryption.EncryptionResult;
import com.mercedesbenz.sechub.commons.encryption.InitializationVector;
import com.mercedesbenz.sechub.pds.PDSNotAcceptableException;
import com.mercedesbenz.sechub.pds.config.PDSServerConfigurationService;
import com.mercedesbenz.sechub.pds.encryption.PDSEncryptionService;
import com.mercedesbenz.sechub.pds.security.PDSUserContextService;

public class PDSCreateJobServiceTest {

    private PDSCreateJobService serviceToTest;
    private UUID sechubJobUUID;
    private PDSJobRepository repository;
    private UUID createdJob1UUID;
    private PDSJob resultJob1;
    private PDSUserContextService userContextService;
    private PDSJobConfigurationValidator configurationValidator;
    private PDSServerConfigurationService serverConfigurationService;

    private PDSEncryptionService encryptionService;
    private EncryptionResult encryptionResult;

    @BeforeEach
    void before() throws Exception {
        sechubJobUUID = UUID.randomUUID();
        createdJob1UUID = UUID.randomUUID();
        repository = mock(PDSJobRepository.class);
        configurationValidator = mock(PDSJobConfigurationValidator.class);
        serverConfigurationService = mock(PDSServerConfigurationService.class);

        userContextService = mock(PDSUserContextService.class);
        when(userContextService.getUserId()).thenReturn("callerName");
        encryptionService = mock(PDSEncryptionService.class);

        serviceToTest = new PDSCreateJobService();
        serviceToTest.repository = repository;
        serviceToTest.userContextService = userContextService;
        serviceToTest.configurationValidator = configurationValidator;
        serviceToTest.serverConfigurationService = serverConfigurationService;
        serviceToTest.encryptionService = encryptionService;

        resultJob1 = new PDSJob();
        resultJob1.uUID = createdJob1UUID;

        when(repository.save(any())).thenReturn(resultJob1);

        // encryption stup
        encryptionResult = mock(EncryptionResult.class);
        byte[] encryptedBytes = "some-pseudo-encrypted-data".getBytes();
        when(encryptionResult.getEncryptedData()).thenReturn(encryptedBytes);
        InitializationVector initialVector = mock(InitializationVector.class);
        when(encryptionResult.getInitialVector()).thenReturn(initialVector);
        when(encryptionService.encryptString(any())).thenReturn(encryptionResult);
    }

    @Test
    void creating_a_job_returns_jobUUD_of_stored_job_in_repository() {
        /* prepare */
        PDSJobConfiguration configuration = new PDSJobConfiguration();
        configuration.setSechubJobUUID(sechubJobUUID);

        /* execute */
        PDSJobCreateResult result = serviceToTest.createJob(configuration);

        /* test */
        assertThat(result).isNotNull();
        UUID pdsJobUUID = result.getJobUUID();
        assertThat(createdJob1UUID).isEqualTo(pdsJobUUID);
    }

    @Test
    void creating_a_job_sets_current_user_as_owner() {
        /* prepare */
        PDSJobConfiguration configuration = new PDSJobConfiguration();

        /* execute */
        PDSJobCreateResult result = serviceToTest.createJob(configuration);

        /* test */
        assertThat(result).isNotNull();
        ArgumentCaptor<PDSJob> jobCaptor = ArgumentCaptor.forClass(PDSJob.class);
        verify(repository).save(jobCaptor.capture());
        assertThat(jobCaptor.getValue().getOwner()).isEqualTo("callerName");
    }

    @Test
    void creating_a_job_sets_encrypted_configuration() throws Exception {
        /* prepare */
        PDSJobConfiguration configuration = new PDSJobConfiguration();
        // check precondition
        String json = configuration.toJSON();
        // Next line normally not valid, but validator does not throw an exception here,
        // so we can have an empty configuration here... Just to test it
        assertThat(json).isEqualTo("{\"parameters\":[]}");

        // simulate encryption for this parameter
        byte[] encryptedBytes = ("encrypted:" + json).getBytes();
        when(encryptionResult.getEncryptedData()).thenReturn(encryptedBytes);

        /* execute */
        serviceToTest.createJob(configuration);

        /* test */

        ArgumentCaptor<PDSJob> jobCaptor = ArgumentCaptor.forClass(PDSJob.class);
        verify(repository).save(jobCaptor.capture());

        PDSJob persistedJob = jobCaptor.getValue();
        assertThat(persistedJob.getEncryptedConfiguration()).isEqualTo(encryptedBytes);
    }

    @Test
    void creating_a_job_calls_configurationValidator() {
        /* prepare */
        PDSJobConfiguration configuration = new PDSJobConfiguration();

        /* execute */
        serviceToTest.createJob(configuration);

        /* test */
        verify(configurationValidator).assertPDSConfigurationValid(configuration);
    }

    @Test
    void creating_a_job_fires_exception_thrown_by_validator() {
        /* prepare */
        PDSJobConfiguration configuration = new PDSJobConfiguration();
        doThrow(new PDSNotAcceptableException("ups")).when(configurationValidator).assertPDSConfigurationValid(configuration);

        /* execute */
        assertThatThrownBy(() -> serviceToTest.createJob(configuration)).isInstanceOf(PDSNotAcceptableException.class).hasMessage("ups");

    }

}
