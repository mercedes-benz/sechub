package com.daimler.sechub.pds.monitoring;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;

import com.daimler.sechub.pds.config.PDSServerConfigurationService;
import com.daimler.sechub.pds.job.PDSJob;
import com.daimler.sechub.pds.job.PDSJobRepository;
import com.daimler.sechub.pds.job.PDSJobStatusState;

@Service
public class PDSMonitoringStatusService {

    @Autowired
    PDSServerConfigurationService serverConfiguratonService;
    
    @Autowired
    PDSJobRepository jobRepository;
    
    public PDSMonitoring getMonitoringStatus() {
        PDSMonitoring monitoring = new PDSMonitoring();
        countStateValues(monitoring);
        
        
        
        return monitoring;
    }

    private void countStateValues(PDSMonitoring monitoring) {
        PDSJob job = new PDSJob();
        job.setServerId(serverConfiguratonService.getServerId());
        
        
        for (PDSJobStatusState state: PDSJobStatusState.values()) {
            long stateValue= count(job, PDSJobStatusState.CREATED);
            monitoring.getJobs().put(state, stateValue);
        }
    }

    private long count(PDSJob job, PDSJobStatusState state) {
        job.setState(state);
        return jobRepository.count(Example.of(job));
    }
    
    
}
