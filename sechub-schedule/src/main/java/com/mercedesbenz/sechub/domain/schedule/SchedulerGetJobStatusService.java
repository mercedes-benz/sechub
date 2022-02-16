// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.schedule;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.mercedesbenz.sechub.domain.schedule.job.ScheduleSecHubJob;
import com.mercedesbenz.sechub.sharedkernel.Step;
import com.mercedesbenz.sechub.sharedkernel.usecases.user.execute.UseCaseUserChecksJobStatus;
import com.mercedesbenz.sechub.sharedkernel.validation.UserInputAssertion;

@Service
public class SchedulerGetJobStatusService {

    @Autowired
    ScheduleAssertService scheduleAssert;

    @Autowired
    UserInputAssertion assertion;

    @Validated
    @UseCaseUserChecksJobStatus(@Step(number = 2, name = "Try to find project and fail or return job status"))
    public ScheduleJobStatus getJobStatus(String projectId, UUID jobUUID) {
        assertion.isValidProjectId(projectId);
        assertion.isValidJobUUID(jobUUID);

        scheduleAssert.assertUserHasAccessToProject(projectId);
        scheduleAssert.assertProjectAllowsReadAccess(projectId);

        ScheduleSecHubJob secHubJob = scheduleAssert.assertJob(projectId, jobUUID);

        return new ScheduleJobStatus(secHubJob);
    }

}
