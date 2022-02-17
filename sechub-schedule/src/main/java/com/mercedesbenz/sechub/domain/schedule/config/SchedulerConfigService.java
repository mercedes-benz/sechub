// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.schedule.config;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.sharedkernel.SecHubEnvironment;
import com.mercedesbenz.sechub.sharedkernel.Step;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessageFactory;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessageService;
import com.mercedesbenz.sechub.sharedkernel.messaging.IsSendingAsyncMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.MessageDataKeys;
import com.mercedesbenz.sechub.sharedkernel.messaging.MessageID;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.schedule.UseCaseAdminDisablesSchedulerJobProcessing;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.schedule.UseCaseAdminEnablesSchedulerJobProcessing;

@Service
public class SchedulerConfigService {

    @Autowired
    SchedulerConfigRepository repository;

    @Autowired
    SecHubEnvironment environmentData;

    @Autowired
    @Lazy
    DomainMessageService domainMessageService;

    @UseCaseAdminEnablesSchedulerJobProcessing(@Step(number = 3, name = "Enable processing", description = "Enables job processing inside scheduler database"))
    @IsSendingAsyncMessage(MessageID.SCHEDULER_JOB_PROCESSING_ENABLED)
    public boolean enableJobProcessing() {
        return setJobProcessingEnabled(true);
    }

    @UseCaseAdminDisablesSchedulerJobProcessing(@Step(number = 3, name = "Disable processing", description = "Disables job processing inside scheduler database"))
    @IsSendingAsyncMessage(MessageID.SCHEDULER_JOB_PROCESSING_DISABLED)
    public boolean disableJobProcessing() {
        return setJobProcessingEnabled(false);
    }

    /**
     * Enables or disables job processing
     *
     * @param enableJobProcessing
     * @return <code>true</code> when processing has been changed and a event was
     *         sent. <code>false</code> when already in wanted state
     */
    boolean setJobProcessingEnabled(boolean enableJobProcessing) {
        SchedulerConfig config = getOrCreateConfig();
        if (enableJobProcessing == config.isJobProcessingEnabled()) {
            return false;
        }
        config.setJobProcessingEnabled(enableJobProcessing);

        repository.save(config);

        DomainMessage domainMessage = null;
        if (enableJobProcessing) {
            domainMessage = DomainMessageFactory.createEmptyRequest(MessageID.SCHEDULER_JOB_PROCESSING_ENABLED);
        } else {
            domainMessage = DomainMessageFactory.createEmptyRequest(MessageID.SCHEDULER_JOB_PROCESSING_DISABLED);
        }
        domainMessage.set(MessageDataKeys.ENVIRONMENT_BASE_URL, environmentData.getServerBaseUrl());
        domainMessageService.sendAsynchron(domainMessage);
        return true;
    }

    private SchedulerConfig getOrCreateConfig() {
        Optional<SchedulerConfig> config = repository.findById(SchedulerConfig.ID);
        if (config.isPresent()) {
            return config.get();
        }
        SchedulerConfig newConfig = new SchedulerConfig();
        return repository.save(newConfig);
    }

    public boolean isJobProcessingEnabled() {
        SchedulerConfig config = getOrCreateConfig();
        return config.isJobProcessingEnabled();
    }
}
