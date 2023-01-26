package com.mercedesbenz.sechub.domain.statistic.job;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class JobStatisticService {

    @Autowired
    JobStatisticRepository repository;

    public void storeJobCreationStatistic(UUID jobUUID, LocalDateTime created, String projectId) {

        JobStatistic jobStatistic = new JobStatistic();
        jobStatistic.setSechubJobUUID(jobUUID);
        jobStatistic.setProjectId(projectId);
        jobStatistic.setCreated(created);

        repository.save(jobStatistic);
    }

}
