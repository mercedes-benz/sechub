package com.daimler.sechub.pds.monitoring;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.daimler.sechub.pds.config.PDSServerConfigurationService;
import com.daimler.sechub.pds.job.PDSJobRepository;
import com.daimler.sechub.pds.job.PDSJobStatusState;

@Service
public class PDSMonitoringStatusService {


private static final Logger LOG = LoggerFactory.getLogger(PDSMonitoringStatusService.class);

    @Autowired
    PDSServerConfigurationService serverConfiguratonService;
    
    @Autowired
    PDSJobRepository jobRepository;
    
    @Autowired
    PDSHeartBeatRepository heartBeatRepository;
    
    public PDSMonitoring getMonitoringStatus() {
        String serverId = serverConfiguratonService.getServerId();
        
        PDSMonitoring monitoring = new PDSMonitoring();
        addStateCounts(serverId, monitoring);
        addHeartBeatDataAsMember(serverId, monitoring);
        
        return monitoring;
    }

    private void addHeartBeatDataAsMember(String serverId, PDSMonitoring monitoring) {
        List<PDSHeartBeat> heartBeats = heartBeatRepository.findAllByServerId(serverId);
        for (PDSHeartBeat heartBeat: heartBeats) {
            PDSClusterMember member = PDSClusterMember.fromJSON(heartBeat.getClusterMemberData());
            if (member==null) {
                LOG.error("server:{}, heartbeat:{} did contain data not being deserializable!",serverId,heartBeat.getUUID());
                continue;
            }
            monitoring.getMembers().add(member);
        }
    }

    private void addStateCounts(String serverId, PDSMonitoring monitoring) {
        
        for (PDSJobStatusState state: PDSJobStatusState.values()) {
            long stateValue= jobRepository.countJobsOfServerInState(serverId, state);
            monitoring.getJobs().put(state, stateValue);
        }
    }

    
    
}
