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
public class SchedulerCreateJobServiceSpringBootTest {

    private static final String PROJECT_ID = "project1";

    @Autowired
    private SchedulerCreateJobService serviceToTest;

    @MockBean
    private SecHubJobFactory jobFactory;

    @MockBean
    private SecHubJobRepository jobRepository;

    @MockBean
    private UserInputAssertion assertion;

    private SecHubConfiguration configuration;
    private ScheduleSecHubJob nextJob;

    private UUID jobUUID;

    private String project;

    private String projectUUID = "projectId1";

    @BeforeEach
    public void beforeEach() {
        jobUUID = UUID.randomUUID();
        nextJob = mock(ScheduleSecHubJob.class);
        configuration = mock(SecHubConfiguration.class);
        project = "projectId";

        when(nextJob.getProjectId()).thenReturn(project);

        when(nextJob.getUUID()).thenReturn(jobUUID);
        when(nextJob.getProjectId()).thenReturn(projectUUID);
        when(jobFactory.createJob(eq(configuration))).thenReturn(nextJob);

        /* prepare */
        when(jobRepository.save(nextJob)).thenReturn(nextJob);
        when(jobRepository.nextJobIdToExecuteFirstInFirstOut()).thenReturn(Optional.of(jobUUID));
    }

    @Test
    public void scheduling_a_new_job_to_an_unexisting_project_throws_NOT_FOUND_exception() {
        /* execute + test */
        Assertions.assertThrows(NotFoundException.class, () -> {
            serviceToTest.createJob("a-project-not-existing", configuration);
        });
    }

    @Test
    public void no_access_entry__scheduling_a_configuration__will_throw_not_found_exception() {
        /* execute + test */
        Assertions.assertThrows(NotFoundException.class, () -> {
            serviceToTest.createJob(PROJECT_ID, configuration);
        });
    }

    @Test
    public void configuration_having_no_project_gets_project_from_URL() {
        /* prepare */
        when(jobRepository.save(nextJob)).thenReturn(nextJob);

        /* execute + test */
    	Assertions.assertThrows(NotFoundException.class, () -> {
    		serviceToTest.createJob(PROJECT_ID, configuration);
    	});
    }

}
