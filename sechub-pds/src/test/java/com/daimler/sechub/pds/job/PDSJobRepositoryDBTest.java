// SPDX-License-Identifier: MIT
package com.daimler.sechub.pds.job;

import static org.junit.Assert.*;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import com.daimler.sechub.pds.PDSProfiles;
import com.daimler.sechub.pds.PDSShutdownService;
import com.daimler.sechub.pds.config.PDSPathExecutableValidator;
import com.daimler.sechub.pds.config.PDSProductIdentifierValidator;
import com.daimler.sechub.pds.config.PDSServerConfigurationService;
import com.daimler.sechub.pds.config.PDSServerConfigurationValidator;
import com.daimler.sechub.pds.config.PDSServerIdentifierValidator;


@ActiveProfiles(PDSProfiles.TEST)
@RunWith(SpringRunner.class)
@DataJpaTest
@ContextConfiguration(classes = { PDSPathExecutableValidator.class, PDSServerIdentifierValidator.class , PDSServerConfigurationValidator.class, PDSProductIdentifierValidator.class, PDSShutdownService.class, PDSJobRepository.class,
        PDSServerConfigurationService.class, PDSJobRepositoryDBTest.SimpleTestConfiguration.class })
public class PDSJobRepositoryDBTest {
	@Autowired
	private TestEntityManager entityManager;

	@Autowired
	private PDSJobRepository repositoryToTest;
	
	@Autowired
    private PDSServerConfigurationService serverConfigService;

	@Before
	public void before() {
	}

	@Test
	public void when_no_job_created_findNextJobToExecute_returns_optional_not_present() {
	    /* execute */
	    Optional<PDSJob> nextJob = repositoryToTest.findNextJobToExecute();
	    
	    /* test */
	    assertFalse(nextJob.isPresent());

	}
	
	@Test
    public void when_one_jobs_created_findNextJobToExecute_returns_none() {
        /* prepare */
        createJob(PDSJobStatusState.CREATED, 0);
        
        /* execute */
        Optional<PDSJob> nextJob = repositoryToTest.findNextJobToExecute();
        
        /* test */
        assertFalse(nextJob.isPresent());

    }
	
    @Test
    public void when_one_jobs_marked_as_ready_to_start_findNextJobToExecute_returns_this_one() {
        /* prepare */
        PDSJob job1 = createJob(PDSJobStatusState.READY_TO_START, 0);
        
        /* execute */
        Optional<PDSJob> nextJob = repositoryToTest.findNextJobToExecute();
        
        /* test */
        assertTrue(nextJob.isPresent());
        assertEquals(job1,nextJob.get());

    }
	
	@Test
    public void when_two_jobs_just_created_findNextJobToExecute_returns_none() {
        /* prepare */
        createJob(PDSJobStatusState.CREATED, 0);
        createJob(PDSJobStatusState.CREATED, 1);
        
        entityManager.flush();
        
        /* execute */
        Optional<PDSJob> nextJob = repositoryToTest.findNextJobToExecute();
        
        /* test */
        assertFalse(nextJob.isPresent());

    }
	
	@Test
    public void when_two_jobs_ready_to_start_findNextJobToExecute_returns_older_one() {
	    /* prepare */
	    PDSJob job1 = createJob(PDSJobStatusState.READY_TO_START, 0);
	    createJob(PDSJobStatusState.READY_TO_START, 1);
	    
        /* execute */
        Optional<PDSJob> nextJob = repositoryToTest.findNextJobToExecute();
        
        /* test */
        assertTrue(nextJob.isPresent());
        assertEquals(job1,nextJob.get());

    }
	
	@Test
    public void when_two_jobs_exist_but_older_is_already_running_findNextJobToExecute_returns_new_ready_to_start() {
        /* prepare */
	    createJob(PDSJobStatusState.RUNNING, 0);
	    PDSJob job2 = createJob(PDSJobStatusState.READY_TO_START, 1);
        
        /* execute */
        Optional<PDSJob> nextJob = repositoryToTest.findNextJobToExecute();
        
        /* test */
        assertTrue(nextJob.isPresent());
        assertEquals(job2,nextJob.get());

    }
	
	@Test
    public void when_two_jobs_exist_but_older_is_done_and_newer_already_running_findNextJobToExecute_returns_none() {
        /* prepare */
        createJob(PDSJobStatusState.DONE, 0);
        createJob(PDSJobStatusState.RUNNING, 1);
        
        /* execute */
        Optional<PDSJob> nextJob = repositoryToTest.findNextJobToExecute();
        
        /* test */
        assertFalse(nextJob.isPresent());

    }
	
	@Test
    public void when_two_jobs_exist_but_older_is_canceled_and_newer_failed_findNextJobToExecute_returns_none() {
        /* prepare */
	    createJob(PDSJobStatusState.CANCEL_REQUESTED, 0);
        createJob(PDSJobStatusState.FAILED, 1);
        
        /* execute */
        Optional<PDSJob> nextJob = repositoryToTest.findNextJobToExecute();
        
        /* test */
        assertFalse(nextJob.isPresent());

    }
	
	private PDSJob createJob(PDSJobStatusState state, int minutes) {
        PDSJob job = new PDSJob();
        job.serverId=serverConfigService.getServerId();
        // necessary because must be not null
        job.created=LocalDateTime.of(2020, 06, 24,13,55,01).plusMinutes(minutes);
        job.owner = "owner";
        job.jsonConfiguration="{}";
        job.state=state;

        /* persist */
        job=entityManager.persistAndFlush(job);
        return job;
    }


	@TestConfiguration
	@EnableAutoConfiguration
	public static class SimpleTestConfiguration{

	}


}
