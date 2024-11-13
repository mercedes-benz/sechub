// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.schedule.job;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;

import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import com.mercedesbenz.sechub.commons.encryption.EncryptionConstants;
import com.mercedesbenz.sechub.commons.encryption.EncryptionResult;
import com.mercedesbenz.sechub.commons.encryption.InitializationVector;
import com.mercedesbenz.sechub.commons.model.ModuleGroup;
import com.mercedesbenz.sechub.commons.model.SecHubConfigurationModelSupport;
import com.mercedesbenz.sechub.commons.model.job.ExecutionResult;
import com.mercedesbenz.sechub.commons.model.job.ExecutionState;
import com.mercedesbenz.sechub.domain.schedule.encryption.ScheduleEncryptionResult;
import com.mercedesbenz.sechub.domain.schedule.encryption.ScheduleEncryptionService;
import com.mercedesbenz.sechub.sharedkernel.configuration.SecHubConfiguration;
import com.mercedesbenz.sechub.sharedkernel.security.UserContextService;
import com.mercedesbenz.sechub.test.SechubTestComponent;

@SechubTestComponent
public class JobCreator {

    private TestEntityManager entityManager;
    private ScheduleSecHubJob job;
    private SecHubJobFactory jobFactory;
    private String projectId;
    private UserContextService userContextService;
    private SecHubConfigurationModelSupport modelSupport;
    private ScheduleEncryptionService encryptionService;

    private JobCreator(String projectId, TestEntityManager entityManager) {
        this.modelSupport = new SecHubConfigurationModelSupport();
        this.entityManager = entityManager;
        this.projectId = projectId;
        this.jobFactory = new SecHubJobFactory();

        userContextService = mock(UserContextService.class);
        encryptionService = mock(ScheduleEncryptionService.class);

        this.jobFactory.userContextService = userContextService;
        this.jobFactory.modelSupport = modelSupport;
        this.jobFactory.encryptionService = encryptionService;
        when(userContextService.getUserId()).thenReturn("loggedInUser");
        doAnswer(invocation -> {
            String textToEncrypt = invocation.getArgument(0);

            EncryptionResult encryptionResult = new EncryptionResult(textToEncrypt.getBytes(EncryptionConstants.UTF8_CHARSET_ENCODING),
                    new InitializationVector(new byte[] {}));
            ScheduleEncryptionResult result = new ScheduleEncryptionResult(Long.valueOf(0), encryptionResult);
            return result;
        }).when(encryptionService).encryptWithLatestCipher(any());

        newJob();
    }

    public static JobCreator jobCreator(String projectId, TestEntityManager entityManager) {
        return new JobCreator(projectId, entityManager);
    }

    public JobCreator module(ModuleGroup moduleGroup) {
        job.moduleGroup = moduleGroup;
        return this;
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
        job.created = dateTime;
        return this;
    }

    public JobCreator ended(LocalDateTime dateTime) {
        job.ended = dateTime;
        return this;
    }

    public JobCreator project(String projectId) {
        job.projectId = projectId;
        return this;
    }

    public JobCreator encryptionPoolId(Long encryptionCipherPoolId) {
        job.encryptionCipherPoolId = encryptionCipherPoolId;
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
        if (job.created == null) {
            job.created = LocalDateTime.now();
        }
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
