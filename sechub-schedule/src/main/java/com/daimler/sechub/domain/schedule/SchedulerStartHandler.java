// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.schedule;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.daimler.sechub.domain.schedule.job.ScheduleSecHubJob;
import com.daimler.sechub.domain.schedule.job.SecHubJobRepository;
import com.daimler.sechub.sharedkernel.SecHubEnvironment;
import com.daimler.sechub.sharedkernel.Step;
import com.daimler.sechub.sharedkernel.messaging.ClusterMemberMessage;
import com.daimler.sechub.sharedkernel.messaging.DomainMessage;
import com.daimler.sechub.sharedkernel.messaging.DomainMessageService;
import com.daimler.sechub.sharedkernel.messaging.IsSendingAsyncMessage;
import com.daimler.sechub.sharedkernel.messaging.MessageDataKeys;
import com.daimler.sechub.sharedkernel.messaging.MessageID;
import com.daimler.sechub.sharedkernel.usecases.admin.status.UseCaseAdministratorReceivesNotificationAboutNewchedulerInstanceStart;
import com.daimler.sechub.sharedkernel.util.HostnameBuilder;

@Component
public class SchedulerStartHandler {

    private static final Logger LOG = LoggerFactory.getLogger(SchedulerStartHandler.class);

    @Autowired
    SecHubEnvironment environment;

    @Autowired
    HostnameBuilder hostnameBuilder;

    @Autowired
    DomainMessageService eventBus;

    @Autowired
    SecHubJobRepository repository;

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE) // want this at the very beginning of scheduler start
    @IsSendingAsyncMessage(MessageID.SCHEDULER_STARTED)
    @UseCaseAdministratorReceivesNotificationAboutNewchedulerInstanceStart(@Step(number = 1, name = "send domain message that new scheduler instance has been started and information about potential zombie jobs"))
    public CommandLineRunner schedulerHasBeenStarted() {
        return args -> {
            LOG.info("scheduler has been started - inspect zombie job status and send event");
            List<ScheduleSecHubJob> jobsRunningButStartedBefore = repository.findAllRunningJobsStartedBefore(LocalDateTime.now());
            String information = buildZombieInformation(jobsRunningButStartedBefore);

            DomainMessage request = new DomainMessage(MessageID.SCHEDULER_STARTED);

            ClusterMemberMessage status = new ClusterMemberMessage();
            status.setHostName(hostnameBuilder.buildHostname());
            status.setServiceName("scheduler");
            status.setServiceStatus("starting");
            status.setInformation(information);

            request.set(MessageDataKeys.ENVIRONMENT_CLUSTER_MEMBER_STATUS, status);
            request.set(MessageDataKeys.ENVIRONMENT_BASE_URL, environment.getServerBaseUrl());
            eventBus.sendAsynchron(request);
        };
    }

    String buildZombieInformation(List<ScheduleSecHubJob> jobsRunningButStartedBefore) {
        if (jobsRunningButStartedBefore == null || jobsRunningButStartedBefore.isEmpty()) {
            return "OK: No zombie jobs found";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("---------------------------------------\n");
        sb.append("ATTENTION: Potential zombie jobs found:\n");
        sb.append("---------------------------------------\n");

        for (ScheduleSecHubJob potentialZombieJob : jobsRunningButStartedBefore) {
            sb.append("- job:").append(potentialZombieJob.getUUID());
            sb.append(", started:").append(potentialZombieJob.getStarted());
            sb.append(", execution-state:").append(potentialZombieJob.getExecutionState());
            sb.append("\n");
        }
        sb.append(
                "\nPlease check if they have been already started by another before started (and still running) scheduler instance. If identified as zombie job you have to restart the job!");
        return sb.toString();
    }
}
