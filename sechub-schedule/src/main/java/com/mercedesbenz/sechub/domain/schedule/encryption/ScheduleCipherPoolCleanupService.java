// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.schedule.encryption;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.domain.schedule.job.SecHubJobRepository;
import com.mercedesbenz.sechub.sharedkernel.Step;
import com.mercedesbenz.sechub.sharedkernel.usecases.autocleanup.UseCaseScheduleAutoCleanExecution;
import com.mercedesbenz.sechub.sharedkernel.usecases.encryption.UseCaseEncryptionCleanup;

@Service
public class ScheduleCipherPoolCleanupService {

    private static final String DESCRIPTION = "Removes cipher pool data entries from database which are no longer used by any job";

    private static final Logger LOG = LoggerFactory.getLogger(ScheduleCipherPoolCleanupService.class);

    @Autowired
    ScheduleEncryptionService encryptionService;

    @Autowired
    ScheduleLatestCipherPoolDataCalculator latestCipherPoolDataCalculator;

    @Autowired
    ScheduleCipherPoolDataRepository poolDataRepository;

    @Autowired
    SecHubJobRepository jobRepository;

    @Autowired
    SecHubOutdatedEncryptionPoolSupport outdatedEncryptionPoolSupport;

    @UseCaseEncryptionCleanup(@Step(number = 1, name = "Schedule cipher pool data cleanup", description = DESCRIPTION))
    @UseCaseScheduleAutoCleanExecution(@Step(number = 3, name = "Schedule cipher pool data cleanup", description = DESCRIPTION))
    public void cleanupCipherPoolDataIfNecessaryAndPossible() {
        LOG.debug("Encryption pool cleanup check");

        /* check clean up possible */
        if (outdatedEncryptionPoolSupport.isOutdatedEncryptionPoolPossibleInCluster()) {
            LOG.debug("It is stil possible to have outdated encryption pools, cannot cleanup");
            return;
        }

        /* resolve data */
        List<ScheduleCipherPoolData> allPoolData = poolDataRepository.findAll();
        if (allPoolData.isEmpty()) {
            LOG.warn("No pool data found in database, cannot do encryption cleanup");
            return;
        }
        ScheduleCipherPoolData latestPoolDataFromDatabase = latestCipherPoolDataCalculator.calculateLatestPoolData(allPoolData);
        if (latestPoolDataFromDatabase == null) {
            LOG.error("latestPoolDataFromDatabase is null - should never happen at this point! Cannot do encryption cleanup");
            return;
        }

        /*
         * Skip if this instance is outdated itself (because this instance could still
         * create jobs with old ciphers)
         */
        if (!Objects.equals(encryptionService.getLatestCipherPoolId(), latestPoolDataFromDatabase.getId())) {
            LOG.debug("Encryption pool of this instance is outdated, cannot cleanup");
            return;
        }

        startEncryptionCleanup(allPoolData, latestPoolDataFromDatabase);

    }

    private void startEncryptionCleanup(List<ScheduleCipherPoolData> allPoolData, ScheduleCipherPoolData latestPoolDataFromDatabase) {
        LOG.debug("Encryption pool cleanup start");

        List<ScheduleCipherPoolData> poolDataToRemove = calculatePoolDataToRemove(allPoolData, latestPoolDataFromDatabase);
        if (poolDataToRemove.isEmpty()) {
            LOG.debug("Found no pool data to remove");
            return;
        }

        LOG.info("Found {} pool entries to remove - start deletetion process", poolDataToRemove.size());

        for (ScheduleCipherPoolData poolData : poolDataToRemove) {

            LOG.info("Start delete of encryption pool entry: id='{}', algorithm='{}', pwdSourceType='{}', pwdSourceData='{}',  created='{}', createdFrom='{}' ",
                    poolData.getId(), poolData.getAlgorithm(), poolData.getPasswordSourceType(), poolData.getPasswordSourceData(), poolData.getCreated(),
                    poolData.getCreatedFrom());

            poolDataRepository.delete(poolData);
        }
    }

    private List<ScheduleCipherPoolData> calculatePoolDataToRemove(List<ScheduleCipherPoolData> allPoolData,
            ScheduleCipherPoolData latestPoolDataFromDatabase) {

        List<Long> allUsedEncryptionPoolIds = jobRepository.collectAllUsedEncryptionPoolIdsInsideJobs();

        List<ScheduleCipherPoolData> poolDataToRemove = new ArrayList<>();

        for (ScheduleCipherPoolData poolData : allPoolData) {
            boolean mustBeKept = latestPoolDataFromDatabase.equals(poolData);
            mustBeKept = mustBeKept || allUsedEncryptionPoolIds.contains(poolData.getId());

            if (mustBeKept) {
                continue;
            }
            poolDataToRemove.add(poolData);
        }
        return poolDataToRemove;
    }

}
