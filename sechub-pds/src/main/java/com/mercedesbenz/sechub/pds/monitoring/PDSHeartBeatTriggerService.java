// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.monitoring;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mercedesbenz.sechub.pds.PDSMustBeDocumented;
import com.mercedesbenz.sechub.pds.config.PDSServerConfigurationService;
import com.mercedesbenz.sechub.pds.execution.PDSExecutionService;
import com.mercedesbenz.sechub.pds.execution.PDSExecutionStatus;
import com.mercedesbenz.sechub.pds.usecase.PDSStep;
import com.mercedesbenz.sechub.pds.usecase.UseCaseAdminFetchesMonitoringStatus;
import com.mercedesbenz.sechub.pds.util.PDSLocalhostDataBuilder;

@Service
public class PDSHeartBeatTriggerService {

    private static final Logger LOG = LoggerFactory.getLogger(PDSHeartBeatTriggerService.class);

    private static final int DEFAULT_INITIAL_DELAY_MILLIS = 1000; // after one second
    private static final int DEFAULT_FIXED_DELAY_MILLIS = 1000 * 60; // every minute trigger hearbeat per default

    private static final boolean DEFAULT_SCHEDULING_ENABLED = true;
    private static final boolean DEFAULT_VERBOSE_LOGGING_ENABLED = true;

    private UUID uuidForThisServerHeartBeat;

    @Autowired
    PDSExecutionService executionService;

    @Autowired
    PDSLocalhostDataBuilder localhostDataBuilder;

    @Autowired
    PDSHeartBeatRepository repository;

    @Autowired
    PDSServerConfigurationService serverConfigService;

    @PDSMustBeDocumented(value = "Initial delay for heartbeat checks", scope = "monitoring")
    @Value("${pds.config.trigger.heartbeat.initialdelay:" + DEFAULT_INITIAL_DELAY_MILLIS + "}")
    private String infoInitialDelay; // here only for logging - used in scheduler annotation as well!

    @PDSMustBeDocumented(value = "Delay for heartbeat checks", scope = "monitoring")
    @Value("${pds.config.trigger.heartbeat.delay:" + DEFAULT_FIXED_DELAY_MILLIS + "}")
    private String infoFixedDelay; // here only for logging - used in scheduler annotation as well!

    @PDSMustBeDocumented(value = "Configure if heartbeat checks are enabled", scope = "monitoring")
    @Value("${pds.config.heartbeat.enabled:" + DEFAULT_SCHEDULING_ENABLED + "}")
    boolean heartbeatEnabled = DEFAULT_SCHEDULING_ENABLED;

    @PDSMustBeDocumented(value = "Configure if heartbeat verbose logging is enabled", scope = "monitoring")
    @Value("${pds.config.heartbeat.verbose.logging.enabled:" + DEFAULT_VERBOSE_LOGGING_ENABLED + "}")
    boolean verboseHeartBeatLogging = DEFAULT_VERBOSE_LOGGING_ENABLED;

    @PostConstruct
    protected void postConstruct() {
        // show info about delay values in log (once)
        LOG.info("Heartbeat service created with {} millisecondss initial delay and {} millisecondss as fixed delay", infoInitialDelay, infoFixedDelay);
        if (!verboseHeartBeatLogging) {
            LOG.info("Verbose heartbeat logging has been disabled");
        }
    }

    // default 10 seconds delay and 5 seconds initial
    @Scheduled(initialDelayString = "${pds.config.trigger.heartbeat.initialdelay:" + DEFAULT_INITIAL_DELAY_MILLIS
            + "}", fixedDelayString = "${pds.config.trigger.heartbeat.delay:" + DEFAULT_FIXED_DELAY_MILLIS + "}")
    @Transactional
    @UseCaseAdminFetchesMonitoringStatus(@PDSStep(name = "heartbeat update", description = "a scheduled heartbeat update is done by PDS server - will persist hearbeat information of server instance to database and also into logs", number = 1))
    public void triggerNextHearbeat() {
        if (!heartbeatEnabled) {
            LOG.trace("Trigger execution of next hearbeat canceled, because hearbeat disabled.");
            return;
        }
        executeHeartBeatUpdate();

    }

    private void executeHeartBeatUpdate() {
        LOG.trace("Trigger next heartbeat started.");

        /* delete older hearbeats */
        repository.removeOlderThan(LocalDateTime.now().minusHours(2));

        PDSHeartBeat heartBeat = null;
        if (uuidForThisServerHeartBeat != null) {
            Optional<PDSHeartBeat> heartBeatOpt = repository.findById(uuidForThisServerHeartBeat);
            if (heartBeatOpt.isPresent()) {
                heartBeat = heartBeatOpt.get();
            } else {
                LOG.info("No heartbeat found for this server - so will create new one");
            }
        } else {
            LOG.info("Heartbeat will be initialized");
        }
        if (heartBeat == null) {
            /* either never started or dropped from db */
            heartBeat = new PDSHeartBeat();
            LOG.info("Create new server hearbeat");
        }
        PDSExecutionStatus status = executionService.getExecutionStatus();
        PDSClusterMember member = new PDSClusterMember();
        member.setExecutionState(status);
        member.setHostname(localhostDataBuilder.buildHostname());
        member.setIp(localhostDataBuilder.buildIP());
        member.setPort(localhostDataBuilder.buildPort());
        member.setExecutionState(status);

        heartBeat.setClusterMemberData(member.toJSON());
        heartBeat.setServerId(serverConfigService.getServerId());
        heartBeat.setUpdated(LocalDateTime.now());

        /* update/create heartbeat */
        heartBeat = repository.saveAndFlush(heartBeat);

        if (verboseHeartBeatLogging) {
            LOG.info("heartbeat update - serverid:{}, heartbeatuuid:{}, cluster-member-data:{}", heartBeat.getServerId(), heartBeat.getUUID(),
                    heartBeat.getClusterMemberData());
        }

        /* update uuid - either for new, or recreated */
        uuidForThisServerHeartBeat = heartBeat.getUUID();
    }

}