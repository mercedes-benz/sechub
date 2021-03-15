package com.daimler.sechub.domain.schedule.strategy;

import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;

import com.daimler.sechub.domain.schedule.job.SecHubJobRepository;

public class FirstComeFirstServeSchedulerStrategy implements SchedulerStrategy {
    
    @Autowired
    public SecHubJobRepository jobRepository;
    
    @Override
    public SchedulerStrategyId getSchedulerId() {
        return SchedulerStrategyId.FirstComeFirstServe;
    }

    @Override
    public UUID nextJobId() {
        
        Optional<UUID> nextJob = jobRepository.nextJobIdToExecuteFirstInFirstOut();
        if (!nextJob.isPresent()) {
            return null;
        }
        
        return nextJob.get();
    }
    
}
