// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.schedule.job;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;

import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import com.mercedesbenz.sechub.domain.schedule.ExecutionResult;
import com.mercedesbenz.sechub.domain.schedule.ExecutionState;
import com.mercedesbenz.sechub.sharedkernel.UserContextService;
import com.mercedesbenz.sechub.sharedkernel.configuration.SecHubConfiguration;
import com.mercedesbenz.sechub.test.SechubTestComponent;

@SechubTestComponent
public class JobCreator {

    private TestEntityManager entityManager;
    private ScheduleSecHubJob job;
    private SecHubJobFactory jobFactory;
    private String projectId;
    private UserContextService userContextService;

    private JobCreator(String projectId, TestEntityManager entityManager) {
        this.entityManager = entityManager;
        this.projectId = projectId;
        this.jobFactory = new SecHubJobFactory();
        userContextService = mock(UserContextService.class);
        this.jobFactory.userContextService = userContextService;
        when(userContextService.getUserId()).thenReturn("loggedInUser");

        newJob();
    }

    public static JobCreator jobCreator(String projectId, TestEntityManager entityManager) {
        return new JobCreator(projectId, entityManager);
    }

    public JobCreator being(ExecutionState state) {
        job.executionState = state;
        return this;
    }

    public JobCreator result(ExecutionResult result) {
        job.executionResult = result;
        return this;
    }

    public JobCreator started(LocalDateTime dateTime) {
        job.started = dateTime;
        return this;
    }
    
    public JobCreator created(LocalDateTime dateTime) {
        job.created= dateTime;
        return this;
    }

    /**
     * Creates the job and returns builder agani
     *
     * @return
     */
    public JobCreator createAnd() {
        create();
        return this;
    }

    /**
     * Creates the job, does an automatic assert that ID is created. The creator
     * will reinit after job was created and can be reused.
     *
     * @return created job
     */
    public ScheduleSecHubJob create() {
        entityManager.persist(job);
        entityManager.flush();

        assertNotNull(job.getUUID());

        ScheduleSecHubJob result = job;
        newJob();

        return result;
    }

    public JobCreator newJob() {
        return newJob(new SecHubConfiguration());
    }

    public JobCreator newJob(SecHubConfiguration configuration) {
        configuration.setProjectId(projectId);
        job = jobFactory.createJob(configuration);
        return this;
    }

}
