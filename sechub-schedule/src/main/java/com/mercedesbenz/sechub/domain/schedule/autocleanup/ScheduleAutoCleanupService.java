// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.schedule.autocleanup;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.commons.core.util.SecHubStorageUtil;
import com.mercedesbenz.sechub.domain.schedule.config.SchedulerConfigService;
import com.mercedesbenz.sechub.domain.schedule.encryption.ScheduleCipherPoolCleanupService;
import com.mercedesbenz.sechub.domain.schedule.job.SecHubJobDataRepository;
import com.mercedesbenz.sechub.domain.schedule.job.SecHubJobRepository;
import com.mercedesbenz.sechub.sharedkernel.Step;
import com.mercedesbenz.sechub.sharedkernel.TimeCalculationService;
import com.mercedesbenz.sechub.sharedkernel.autocleanup.AutoCleanupResult;
import com.mercedesbenz.sechub.sharedkernel.autocleanup.AutoCleanupResultInspector;
import com.mercedesbenz.sechub.sharedkernel.usecases.autocleanup.UseCaseScheduleAutoCleanExecution;
import com.mercedesbenz.sechub.storage.core.JobStorage;
import com.mercedesbenz.sechub.storage.core.StorageService;

@Service
public class ScheduleAutoCleanupService {

    private static final Logger LOG = LoggerFactory.getLogger(ScheduleAutoCleanupService.class);

    @Autowired
    TimeCalculationService timeCalculationService;

    @Autowired
    SchedulerConfigService configService;

    @Autowired
    SecHubJobRepository jobRepository;

    @Autowired
    SecHubJobDataRepository jobDataRepository;

    @Autowired
    AutoCleanupResultInspector inspector;

    @Autowired
    ScheduleCipherPoolCleanupService encryptionPoolCleanupService;

    @Autowired
    StorageService storageService;

    @UseCaseScheduleAutoCleanExecution(@Step(number = 2, name = "Delete old data", description = "deletes old job information"))
    public void cleanup() {
        /* calculate */
        long days = configService.getAutoCleanupInDays();
        if (days == 0) {
            LOG.trace("Cancel schedule auto cleanup because disabled.");
            return;
        }
        LocalDateTime cleanTimeStamp = timeCalculationService.calculateNowMinusDays(days);

        /* delete */
        deleteOldJobStorage(cleanTimeStamp);

        int amount = jobRepository.deleteJobsOlderThan(cleanTimeStamp);
        jobDataRepository.deleteJobDataOlderThan(cleanTimeStamp);

        /* @formatter:off */
        inspector.inspect(AutoCleanupResult.builder().
                    autoCleanup("sechub-jobs",getClass()).
                    forDays(days).
                    hasDeleted(amount).
                    byTimeStamp(cleanTimeStamp).
                    build()
                    );
        /* @formatter:on */

        /* cleanup encryption */
        encryptionPoolCleanupService.cleanupCipherPoolDataIfNecessaryAndPossible();

    }

    private void deleteOldJobStorage(LocalDateTime cleanTimeStamp) {
        try {
            List<Object[]> jobUUIDAndProjectIds = jobRepository.findJobUUIDsAndProjectIdsForJobsOlderThan(cleanTimeStamp);

            tryToDeleteOldJobStorage(jobUUIDAndProjectIds);

        } catch (Exception e) {
            LOG.error("Delete of old job storage failed", e);
        }
    }

    private void tryToDeleteOldJobStorage(List<Object[]> jobUUIDAndProjectIds) {
        int amount = jobUUIDAndProjectIds.size();
        if (jobUUIDAndProjectIds.size() == 0) {
            return;
        }

        LOG.info("Starting delete of any leftover storage elements from {} outdated jobs.", amount);

        for (Object[] jobUUIDAndProjectId : jobUUIDAndProjectIds) {

            UUID jobUUID = (UUID) jobUUIDAndProjectId[0];
            String projectId = (String) jobUUIDAndProjectId[1];

            String path = SecHubStorageUtil.createStoragePath(projectId);
            JobStorage jobStorage = storageService.createJobStorage(path, jobUUID);
            try {
                jobStorage.deleteAll();
            } catch (IOException e) {
                LOG.error("Was not able to delete job storage for outdated job: {}", jobUUID, e);
            } finally {
                jobStorage.close();
            }
        }
    }

}
