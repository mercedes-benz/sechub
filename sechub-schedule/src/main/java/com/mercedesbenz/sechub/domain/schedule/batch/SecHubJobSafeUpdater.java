// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.schedule.batch;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.mercedesbenz.sechub.commons.model.SecHubMessage;
import com.mercedesbenz.sechub.commons.model.TrafficLight;
import com.mercedesbenz.sechub.commons.model.job.ExecutionResult;
import com.mercedesbenz.sechub.commons.model.job.ExecutionState;
import com.mercedesbenz.sechub.domain.schedule.job.ScheduleSecHubJob;
import com.mercedesbenz.sechub.domain.schedule.job.ScheduleSecHubJobMessagesSupport;
import com.mercedesbenz.sechub.domain.schedule.job.SecHubJobRepository;

@Component
public class SecHubJobSafeUpdater {

    @Autowired
    private SecHubJobRepository repository;

    private ScheduleSecHubJobMessagesSupport jobMessagesSupport = new ScheduleSecHubJobMessagesSupport();

    private static final Logger LOG = LoggerFactory.getLogger(SecHubJobSafeUpdater.class);

    /**
     * Saves the job and also ensures we got a NEW transaction - this is necessary
     * when one of the former db actions did come to an ROLLBACK. We want to ensure
     * the job will be updated even in such case! This is the reason for the
     * REQUIRES_NEW <br>
     *
     * @see https://www.ibm.com/developerworks/java/library/j-ts1/index.html for
     *      more details about usage of Propagation.REQUIRES_NEW
     * @param secHubJob
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void safeUpdateOfSecHubJob(UUID sechubUUID, ExecutionResult result, String trafficLightString, List<SecHubMessage> messages) {
        Optional<ScheduleSecHubJob> secHubJobOptional = repository.findById(sechubUUID);
        if (!secHubJobOptional.isPresent()) {
            LOG.error("Sechub job with UUID:{} not found! Maybe deleted in meantime?", sechubUUID);
            return;
        }
        ScheduleSecHubJob secHubJob = secHubJobOptional.get();
        if (ExecutionState.CANCEL_REQUESTED.equals(secHubJob.getExecutionState())) {
            LOG.warn("Did not store sechub job data, because cancel requested");
            return;
        }
        secHubJob.setExecutionState(ExecutionState.ENDED);
        secHubJob.setExecutionResult(result);
        secHubJob.setTrafficLight(TrafficLight.fromString(trafficLightString));
        secHubJob.setEnded(LocalDateTime.now());

        if (messages != null) {
            jobMessagesSupport.addMessages(secHubJob, messages);
        }

        repository.save(secHubJob);

    }
}
