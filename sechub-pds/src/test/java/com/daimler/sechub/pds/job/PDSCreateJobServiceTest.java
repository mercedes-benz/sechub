package com.daimler.sechub.pds.job;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

public class PDSCreateJobServiceTest {

    private PDSCreateJobService serviceToTest;
    private UUID sechubJobUUID;
    private PDSJobRepository repository;
    private UUID createdJob1UUID;
    private PDSJob resultJob1;

    @Before
    public void before() throws Exception {
        sechubJobUUID = UUID.randomUUID();
        createdJob1UUID = UUID.randomUUID();
        repository=mock(PDSJobRepository.class);
        
        serviceToTest = new PDSCreateJobService();
        serviceToTest.repository=repository;
        
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

}
