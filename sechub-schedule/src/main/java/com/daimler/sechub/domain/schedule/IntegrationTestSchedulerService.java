package com.daimler.sechub.domain.schedule;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.daimler.sechub.domain.schedule.job.SecHubJobRepository;
import com.daimler.sechub.sharedkernel.Profiles;

@Service
@Profile(Profiles.INTEGRATIONTEST)
public class IntegrationTestSchedulerService {

    @Autowired
    private SecHubJobRepository repository;
    
    @Transactional
    public void deleteWaitingJobs() {
        repository.deleteWaitingJobs();
    }
}
