// SPDX-License-Identifier: MIT
package com.daimler.sechub.pds.job;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.UUID;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentCaptor;

import com.daimler.sechub.pds.PDSNotAcceptableException;
import com.daimler.sechub.pds.config.PDSServerConfigurationService;
import com.daimler.sechub.pds.security.PDSUserContextService;
import com.daimler.sechub.test.junit4.ExpectedExceptionFactory;

public class PDSCreateJobServiceTest {

    @Rule
    public ExpectedException expected = ExpectedExceptionFactory.none();

    private PDSCreateJobService serviceToTest;
    private UUID sechubJobUUID;
    private PDSJobRepository repository;
    private UUID createdJob1UUID;
    private PDSJob resultJob1;
    private PDSUserContextService userContextService;
    private PDSJobConfigurationValidator configurationValidator;
    private PDSServerConfigurationService serverConfigurationService;

    @Before
    public void before() throws Exception {
        sechubJobUUID = UUID.randomUUID();
        createdJob1UUID = UUID.randomUUID();
        repository = mock(PDSJobRepository.class);
        configurationValidator = mock(PDSJobConfigurationValidator.class);
        serverConfigurationService=mock(PDSServerConfigurationService.class);
        
        
        userContextService = mock(PDSUserContextService.class);
        when(userContextService.getUserId()).thenReturn("callerName");

        serviceToTest = new PDSCreateJobService();
        serviceToTest.repository = repository;
        serviceToTest.userContextService = userContextService;
        serviceToTest.configurationValidator = configurationValidator;
        serviceToTest.serverConfigurationService=serverConfigurationService;

        resultJob1 = new PDSJob();
        resultJob1.uUID = createdJob1UUID;

        when(repository.save(any())).thenReturn(resultJob1);
    }

    @Test
    public void creating_a_job_returns_jobUUD_of_stored_job_in_repository() {
        /* prepare */
        PDSJobConfiguration configuration = new PDSJobConfiguration();
        configuration.setSechubJobUUID(sechubJobUUID);

        /* execute */
        PDSJobCreateResult result = serviceToTest.createJob(configuration);

        /* test */
        assertNotNull(result);
        UUID pdsJobUUID = result.getJobUUID();
        assertEquals(createdJob1UUID, pdsJobUUID);
    }

    @Test
    public void creating_a_job_sets_current_user_as_owner() {
        /* prepare */
        PDSJobConfiguration configuration = new PDSJobConfiguration();

        /* execute */
        PDSJobCreateResult result = serviceToTest.createJob(configuration);

        /* test */
        assertNotNull(result);
        ArgumentCaptor<PDSJob> jobCaptor = ArgumentCaptor.forClass(PDSJob.class);
        verify(repository).save(jobCaptor.capture());
        assertEquals("callerName", jobCaptor.getValue().getOwner());
    }

    @Test
    public void creating_a_job_sets_configuration_as_json() throws Exception{
        /* prepare */
        PDSJobConfiguration configuration = new PDSJobConfiguration();

        /* execute */
        serviceToTest.createJob(configuration);

        /* test */
        ArgumentCaptor<PDSJob> jobCaptor = ArgumentCaptor.forClass(PDSJob.class);
        verify(repository).save(jobCaptor.capture());
        
        String json = configuration.toJSON();
        // Next line normally not valid, but validator does not throw an exception here,
        // so we can have an empty config here... Just to test it
        assertEquals("{\"parameters\":[]}", json);
        assertEquals(json, jobCaptor.getValue().getJsonConfiguration());
    }

    @Test
    public void creating_a_job_calls_configurationValidator() {
        /* prepare */
        PDSJobConfiguration configuration = new PDSJobConfiguration();

        /* execute */
        serviceToTest.createJob(configuration);

        /* test */
        verify(configurationValidator).assertPDSConfigurationValid(configuration);
    }

    @Test
    public void creating_a_job_fires_exception_thrown_by_validator() {
        /* prepare */
        PDSJobConfiguration configuration = new PDSJobConfiguration();
        doThrow(new PDSNotAcceptableException("ups")).when(configurationValidator).assertPDSConfigurationValid(configuration);

        /* test */
        expected.expect(PDSNotAcceptableException.class);
        expected.expectMessage("ups");

        /* execute */
        serviceToTest.createJob(configuration);

    }

}
