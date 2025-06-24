// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.schedule;

import static com.mercedesbenz.sechub.domain.schedule.job.SecHubJobTraceLogID.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.mercedesbenz.sechub.commons.model.SecHubConfigurationMetaData;
import com.mercedesbenz.sechub.domain.schedule.job.ScheduleSecHubJob;
import com.mercedesbenz.sechub.domain.schedule.job.SecHubJobFactory;
import com.mercedesbenz.sechub.domain.schedule.job.SecHubJobRepository;
import com.mercedesbenz.sechub.domain.schedule.job.SecHubJobTraceLogID;
import com.mercedesbenz.sechub.sharedkernel.Step;
import com.mercedesbenz.sechub.sharedkernel.configuration.SecHubConfiguration;
import com.mercedesbenz.sechub.sharedkernel.configuration.SecHubConfigurationMetaDataMapTransformer;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessageService;
import com.mercedesbenz.sechub.sharedkernel.messaging.IsSendingAsyncMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.JobMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.MessageDataKeys;
import com.mercedesbenz.sechub.sharedkernel.messaging.MessageID;
import com.mercedesbenz.sechub.sharedkernel.usecases.user.execute.UseCaseUserCreatesNewJob;
import com.mercedesbenz.sechub.sharedkernel.validation.UserInputAssertion;

import jakarta.validation.Valid;

@Service
public class SchedulerCreateJobService {

    private static final Logger LOG = LoggerFactory.getLogger(SchedulerCreateJobService.class);

    @Autowired
    private SecHubJobRepository jobRepository;

    @Autowired
    private SecHubJobFactory secHubJobFactory;

    @Autowired
    ScheduleAssertService assertService;

    @Autowired
    UserInputAssertion assertion;

    @Autowired
    DomainMessageService domainMessageService;

    @Autowired
    SecHubConfigurationMetaDataMapTransformer transformer;

    @Validated
    @UseCaseUserCreatesNewJob(@Step(number = 2, name = "Persistence and result", description = "Persist a new job entry and return Job UUID"))
    public SchedulerResult createJob(String projectId, @Valid SecHubConfiguration configuration) {
        assertion.assertIsValidProjectId(projectId);

        /* we set the project id into configuration done by used url! */
        configuration.setProjectId(projectId);

        assertService.assertUserHasAccessToProject(projectId);
        assertService.assertProjectAllowsWriteAccess(projectId);
        assertService.assertExecutionAllowed(configuration);

        assertService.assertValidAtRuntime(configuration);

        ScheduleSecHubJob secHubJob = secHubJobFactory.createJob(configuration);
        jobRepository.save(secHubJob);

        if (configuration.getMetaData().isPresent()) {
            SecHubConfigurationMetaData metaData = configuration.getMetaData().get();

            Map<String, String> map = transformer.transform(metaData);
            /* we add all transformed meta data */
            for (Map.Entry<String, String> entry : map.entrySet()) {
                secHubJob.addData(entry.getKey(), entry.getValue());
            }
            jobRepository.save(secHubJob);
        }
        SecHubJobTraceLogID traceLogId = traceLogID(secHubJob);
        LOG.info("New job added:{}", traceLogId);

        UUID sechubJobUUID = secHubJob.getUUID();

        sendJobCreationEvent(sechubJobUUID, projectId, secHubJob.getCreated(), secHubJob.getOwner());

        return new SchedulerResult(sechubJobUUID);
    }

    @IsSendingAsyncMessage(MessageID.JOB_CREATED)
    private void sendJobCreationEvent(UUID sechubJobUUID, String projectId, LocalDateTime localDateTime, String owner) {
        DomainMessage domainMessage = new DomainMessage(MessageID.JOB_CREATED);
        JobMessage jobData = new JobMessage();

        jobData.setJobUUID(sechubJobUUID);
        jobData.setProjectId(projectId);
        jobData.setSince(localDateTime);
        jobData.setOwner(owner);

        domainMessage.set(MessageDataKeys.JOB_CREATED_DATA, jobData);

        domainMessageService.sendAsynchron(domainMessage);
    }

}