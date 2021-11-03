// SPDX-License-Identifier: MIT
package com.daimler.sechub.pds.monitoring;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.daimler.sechub.pds.config.PDSServerConfigurationService;
import com.daimler.sechub.pds.execution.PDSExecutionStatus;
import com.daimler.sechub.pds.job.PDSJobRepository;
import com.daimler.sechub.pds.job.PDSJobStatusState;
import com.daimler.sechub.pds.usecase.PDSStep;
import com.daimler.sechub.pds.usecase.UseCaseAdminFetchesMonitoringStatus;

@Service
public class PDSMonitoringStatusService {

    private static final Logger LOG = LoggerFactory.getLogger(PDSMonitoringStatusService.class);

    @Autowired
    PDSServerConfigurationService serverConfiguratonService;

    @Autowired
    PDSJobRepository jobRepository;

    @Autowired
    PDSHeartBeatRepository heartBeatRepository;

    @UseCaseAdminFetchesMonitoringStatus(@PDSStep(name = "service call", description = "service fetches job state counts and gathers hearbeats of cluster members by serverId", number = 4))
    public PDSMonitoring getMonitoringStatus() {
        String serverId = serverConfiguratonService.getServerId();

        PDSMonitoring monitoring = new PDSMonitoring();
        addStateCounts(serverId, monitoring);
        addHeartBeatDataAsMember(serverId, monitoring);

        return monitoring;
    }

    void addHeartBeatDataAsMember(String serverId, PDSMonitoring monitoring) {
        List<PDSHeartBeat> heartBeats = heartBeatRepository.findAllByServerId(serverId);
        for (PDSHeartBeat heartBeat : heartBeats) {
            PDSClusterMember member = PDSClusterMember.fromJSON(heartBeat.getClusterMemberData());
            if (member == null) {
                String hostName = "unknown:" + heartBeat.getUUID();
                /* was not able to fetch cluster member data by hearbeat JSON data! */
                LOG.error("server:{}, heartbeat:{} did contain data not being deserializable! Will provide fallback result with hostname :'{}'.", serverId,
                        heartBeat.getUUID(), hostName);

                member = new PDSClusterMember();
                member.setHostname(hostName);
                member.setHeartBeatTimestamp(heartBeat.getUpdated()); // set time stamp manually, so it's clear when last update happened
                member.setExecutionState(new PDSExecutionStatus());
            }
            monitoring.getMembers().add(member);
        }
    }

    private void addStateCounts(String serverId, PDSMonitoring monitoring) {

        for (PDSJobStatusState state : PDSJobStatusState.values()) {
            long stateValue = jobRepository.countJobsOfServerInState(serverId, state);
            monitoring.getJobs().put(state, stateValue);
        }
    }

}
