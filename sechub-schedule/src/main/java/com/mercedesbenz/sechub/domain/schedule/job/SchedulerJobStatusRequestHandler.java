// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.schedule.job;

import static com.mercedesbenz.sechub.sharedkernel.util.Assert.*;

import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.commons.model.job.ExecutionState;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessageSynchronousResult;
import com.mercedesbenz.sechub.sharedkernel.messaging.IsRecevingSyncMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.IsSendingSyncMessageAnswer;
import com.mercedesbenz.sechub.sharedkernel.messaging.MessageDataKeys;
import com.mercedesbenz.sechub.sharedkernel.messaging.MessageID;
import com.mercedesbenz.sechub.sharedkernel.messaging.SchedulerJobMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.SynchronMessageHandler;

@Component
public class SchedulerJobStatusRequestHandler implements SynchronMessageHandler {

    private static final Logger LOG = LoggerFactory.getLogger(SchedulerJobStatusRequestHandler.class);

    @Autowired
    SecHubJobRepository repository;

    @Override
    @IsRecevingSyncMessage(MessageID.REQUEST_SCHEDULER_JOB_STATUS)
    public DomainMessageSynchronousResult receiveSynchronMessage(DomainMessage request) {
        notNull(request, "Request may not be null!");

        if (!request.hasID(MessageID.REQUEST_SCHEDULER_JOB_STATUS)) {
            return new DomainMessageSynchronousResult(MessageID.UNSUPPORTED_OPERATION,
                    new UnsupportedOperationException("Can only handle " + MessageID.REQUEST_SCHEDULER_JOB_STATUS));
        }
        return returnStatus(request);
    }

    @IsSendingSyncMessageAnswer(value = MessageID.SCHEDULER_JOB_STATUS, answeringTo = MessageID.REQUEST_SCHEDULER_JOB_STATUS, branchName = "success")
    private DomainMessageSynchronousResult returnStatus(DomainMessage request) {
        DomainMessageSynchronousResult result = new DomainMessageSynchronousResult(MessageID.SCHEDULER_JOB_STATUS);
        SchedulerJobMessage requestMessage = request.get(MessageDataKeys.SCHEDULER_JOB_STATUS);

        UUID sechubJobUUID = requestMessage.getSechubJobUUID();

        Optional<ScheduleSecHubJob> repositoryResult = repository.findById(sechubJobUUID);

        SchedulerJobMessage status = new SchedulerJobMessage();
        status.setSecHubJobUUID(sechubJobUUID);

        boolean exists = repositoryResult.isPresent();
        status.setExisting(exists);

        if (exists) {
            ScheduleSecHubJob job = repositoryResult.get();

            ExecutionState executionState = job.getExecutionState();
            if (executionState != null) {

                switch (executionState) {
                case CANCEL_REQUESTED:
                    status.setCancelRequested(true);
                    break;
                case SUSPENDED:
                    status.setSuspended(true);
                    break;
                case CANCELED:
                    status.setCanceled(true);
                    break;
                case ENDED:
                    status.setEnded(true);
                    break;
                case INITIALIZING:
                    status.setInitializing(true);
                    break;
                case READY_TO_START:
                    status.setReadyToStart(true);
                    break;
                case STARTED:
                    status.setStarted(true);
                    break;
                default:
                    LOG.warn("Unsupported execution state detected:{}", executionState);

                }
            } else {
                LOG.warn("Execution state null detected - there could be side effects!");
            }
        }

        result.set(MessageDataKeys.SCHEDULER_JOB_STATUS, status);

        return result;
    }
}
