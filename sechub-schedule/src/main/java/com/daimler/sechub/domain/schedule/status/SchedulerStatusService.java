// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.schedule.status;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.daimler.sechub.domain.schedule.config.SchedulerConfigService;
import com.daimler.sechub.domain.schedule.job.SecHubJobRepository;
import com.daimler.sechub.sharedkernel.messaging.DomainMessage;
import com.daimler.sechub.sharedkernel.messaging.DomainMessageFactory;
import com.daimler.sechub.sharedkernel.messaging.DomainMessageService;
import com.daimler.sechub.sharedkernel.messaging.IsSendingAsyncMessage;
import com.daimler.sechub.sharedkernel.messaging.MessageDataKeys;
import com.daimler.sechub.sharedkernel.messaging.MessageID;
import com.daimler.sechub.sharedkernel.messaging.SchedulerMessage;

@Service
public class SchedulerStatusService {

    @Autowired
    @Lazy
    DomainMessageService eventBus;

    @Autowired
    SecHubJobRepository jobRepository;

    @Autowired
    SchedulerConfigService configService;

    @IsSendingAsyncMessage(MessageID.SCHEDULER_STATUS_UPDATE)
    public void buildStatus() {
        DomainMessage message = DomainMessageFactory.createEmptyRequest(MessageID.SCHEDULER_STATUS_UPDATE);
        SchedulerMessage sm = new SchedulerMessage();

        long amountOfJobsAll = jobRepository.count();
        long amountOfRunningJobs = jobRepository.countRunningJobs();
        long amountOfWaitingJobs = jobRepository.countWaitingJobs();

        boolean processingEnabled = configService.isJobProcessingEnabled();

        sm.setAmountOfJobsAll(amountOfJobsAll);
        sm.setAmountOfRunningJobs(amountOfRunningJobs);
        sm.setAmountOfWaitingJobs(amountOfWaitingJobs);
        sm.setJobProcessingEnabled(processingEnabled);

        message.set(MessageDataKeys.SCHEDULER_STATUS_DATA, sm);

        eventBus.sendAsynchron(message);
    }

}
