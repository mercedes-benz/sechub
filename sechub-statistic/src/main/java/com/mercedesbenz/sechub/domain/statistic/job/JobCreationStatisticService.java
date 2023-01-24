package com.mercedesbenz.sechub.domain.statistic.job;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;

@Service
public class JobCreationStatisticService {

    public void storeJobCreationStatistic(UUID jobUUID, LocalDateTime created, String projectId) {
        
    }

}
