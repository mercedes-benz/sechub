// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.schedule;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.commons.model.job.ExecutionState;
import com.mercedesbenz.sechub.domain.schedule.job.ScheduleSecHubJob;
import com.mercedesbenz.sechub.domain.schedule.job.SecHubJobRepository;
import com.mercedesbenz.sechub.sharedkernel.Step;
import com.mercedesbenz.sechub.sharedkernel.error.NotAcceptableException;
import com.mercedesbenz.sechub.sharedkernel.usecases.user.execute.UseCaseUserApprovesJob;
import com.mercedesbenz.sechub.sharedkernel.validation.UserInputAssertion;

@Service
public class SchedulerApproveJobService {

    private static final Logger LOG = LoggerFactory.getLogger(SchedulerApproveJobService.class);

    @Autowired
    private SecHubJobRepository jobRepository;

    @Autowired
    ScheduleAssertService assertService;

    @Autowired
    UserInputAssertion assertion;

    @UseCaseUserApprovesJob(@Step(number = 2, name = "Try to find job annd update execution state", description = "When job is found and user has access job will be marked as ready for execution"))
    public void approveJob(String projectId, UUID jobUUID) {
        assertion.assertIsValidProjectId(projectId);
        assertion.assertIsValidJobUUID(jobUUID);

        assertService.assertUserHasAccessToProject(projectId);
        assertService.assertProjectAllowsWriteAccess(projectId);

        ScheduleSecHubJob secHubJob = assertService.assertJob(projectId, jobUUID);
        ExecutionState state = secHubJob.getExecutionState();
        if (!ExecutionState.INITIALIZING.equals(state)) {
            throw new NotAcceptableException("Not in correct state");
        }
        secHubJob.setExecutionState(ExecutionState.READY_TO_START);
        jobRepository.save(secHubJob);

        LOG.info("job {} now approved", jobUUID);
    }

}
