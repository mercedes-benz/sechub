// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.schedule;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import com.mercedesbenz.sechub.domain.schedule.access.ScheduleAccessRepository;
import com.mercedesbenz.sechub.domain.schedule.job.ScheduleSecHubJob;
import com.mercedesbenz.sechub.domain.schedule.job.SecHubJobFactory;
import com.mercedesbenz.sechub.domain.schedule.job.SecHubJobRepository;
import com.mercedesbenz.sechub.sharedkernel.Profiles;
import com.mercedesbenz.sechub.sharedkernel.configuration.SecHubConfiguration;
import com.mercedesbenz.sechub.sharedkernel.error.NotFoundException;
import com.mercedesbenz.sechub.sharedkernel.validation.UserInputAssertion;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.yml")
@ActiveProfiles(Profiles.TEST)
public class SchedulerGetJobStatusServiceTest {

    private static final String PROJECT_ID = "project1";

    @Autowired
    private SchedulerGetJobStatusService serviceToTest;

    @MockBean
    private SecHubJobFactory jobFactory;

    @MockBean
    private SecHubJobRepository jobRepository;

    @MockBean
    private ScheduleAccessRepository projectUserAccessRepository;

    @MockBean
    private UserInputAssertion assertion;

    private SecHubConfiguration configuration;
    private ScheduleSecHubJob job;

    private UUID jobUUID;

    private String project;

    private String projectUUID = "projectId1";

    @BeforeEach
    public void beforeEach() {
        jobUUID = UUID.randomUUID();
        job = mock(ScheduleSecHubJob.class);
        configuration = mock(SecHubConfiguration.class);
        project = "projectId";

        when(job.getProjectId()).thenReturn(project);

        when(job.getUUID()).thenReturn(jobUUID);
        when(job.getProjectId()).thenReturn(projectUUID);
        when(jobFactory.createJob(eq(configuration))).thenReturn(job);
    }

    @Test
    public void get_a_job_status_from_an_unexisting_project_throws_NOT_FOUND_exception() {
        /* prepare */
        UUID jobUUID = UUID.randomUUID();
        when(jobRepository.findById(jobUUID)).thenReturn(Optional.of(mock(ScheduleSecHubJob.class)));// should not be necessary, but to

        /* execute + test */
        // prevent dependency to call
        // hierachy... we simulate job can be
        // found
        Assertions.assertThrows(NotFoundException.class, () -> {
            serviceToTest.getJobStatus("a-project-not-existing", jobUUID);
        });
    }

    @Test
    public void get_a_job_status_from_an_exsting_project_but_no_job_throws_NOT_FOUND_exception() {
        /* prepare */
        UUID jobUUID = UUID.randomUUID();
        when(jobRepository.findById(jobUUID)).thenReturn(Optional.empty()); // not found...

        /* execute + test */
        Assertions.assertThrows(NotFoundException.class, () -> {
            serviceToTest.getJobStatus(PROJECT_ID, jobUUID);
        });
    }

}
