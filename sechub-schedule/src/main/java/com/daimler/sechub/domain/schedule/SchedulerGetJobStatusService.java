// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.schedule;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.daimler.sechub.domain.schedule.job.ScheduleSecHubJob;
import com.daimler.sechub.sharedkernel.Step;
import com.daimler.sechub.sharedkernel.usecases.user.execute.UseCaseUserChecksJobStatus;
import com.daimler.sechub.sharedkernel.validation.UserInputAssertion;

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

        ScheduleSecHubJob secHubJob = scheduleAssert.assertJob(projectId, jobUUID);

        return new ScheduleJobStatus(secHubJob);
    }

}
