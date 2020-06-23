package com.daimler.sechub.pds.job;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import com.daimler.sechub.pds.security.PDSUserContextService;

public class PDSCreateJobServiceTest {

    private PDSCreateJobService serviceToTest;
    private UUID sechubJobUUID;
    private PDSJobRepository repository;
    private UUID createdJob1UUID;
    private PDSJob resultJob1;
    private PDSUserContextService userContextService;

    @Before
    public void before() throws Exception {
        sechubJobUUID = UUID.randomUUID();
        createdJob1UUID = UUID.randomUUID();
        repository=mock(PDSJobRepository.class);
        userContextService=mock(PDSUserContextService.class);
        when(userContextService.getUserId()).thenReturn("callerName");
        
        serviceToTest = new PDSCreateJobService();
        serviceToTest.repository=repository;
        serviceToTest.userContextService=userContextService;
        
        resultJob1=new PDSJob();
        resultJob1.uUID=createdJob1UUID;
        
        when(repository.save(any())).thenReturn(resultJob1);
    }

    @Test
    public void creating_a_job_returns_jobUUD_of_stored_job_in_repository() {
        /* prepare */
        PDSConfiguration configuration = new PDSConfiguration();
        configuration.setSechubJobUUID(sechubJobUUID);
        
        /* execute */
        PDSJobCreateResult result = serviceToTest.createJob(configuration);
        
        /* test */
        assertNotNull(result);
        UUID pdsJobUUID = result.getJobId();
        assertEquals(createdJob1UUID, pdsJobUUID);
    }
    
    @Test
    public void creating_a_job_sets_current_user_as_owner() {
        /* prepare */
        PDSConfiguration configuration = new PDSConfiguration();
        
        /* execute */
        PDSJobCreateResult result = serviceToTest.createJob(configuration);
        
        /* test */
        assertNotNull(result);
        ArgumentCaptor<PDSJob> jobCaptor = ArgumentCaptor.forClass(PDSJob.class); 
        verify(repository).save(jobCaptor.capture());
        assertEquals("callerName", jobCaptor.getValue().getOwner());
    }

}
