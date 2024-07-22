// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.schedule.job;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.commons.encryption.InitializationVector;
import com.mercedesbenz.sechub.domain.schedule.encryption.ScheduleEncryptionException;
import com.mercedesbenz.sechub.domain.schedule.encryption.ScheduleEncryptionResult;
import com.mercedesbenz.sechub.domain.schedule.encryption.ScheduleEncryptionService;
import com.mercedesbenz.sechub.sharedkernel.Step;
import com.mercedesbenz.sechub.sharedkernel.usecases.encryption.UseCaseAdminStartsEncryptionRotation;
import com.mercedesbenz.sechub.sharedkernel.usecases.encryption.UseCaseScheduleEncryptionPoolRefresh;
import com.mercedesbenz.sechub.sharedkernel.usecases.encryption.UseCaseScheduleRotateDataEncryption;

@Service
public class ScheduleSecHubJobEncryptionUpdateService {

    private static final Logger LOG = LoggerFactory.getLogger(ScheduleSecHubJobEncryptionUpdateService.class);

    @Autowired
    ScheduleEncryptionService encryptionService;

    @Autowired
    SecHubJobTransactionService jobTransactionService;

    @Value("${sechub.schedule.job.encryption.update.blocksize:50}") // 50 per default
    int updateBlockSize;

    @Value("${sechub.schedule.job.encryption.update.statuslog.milliseconds:10000}") // every 10 seconds per default
    long milliSecondsForNextStatusLog;

    @UseCaseAdminStartsEncryptionRotation(@Step(number = 6, name = "Update encrypted data", description = "Encrypted data is updated (a direct pool refresh was triggered by admin action)"))
    @UseCaseScheduleEncryptionPoolRefresh(@Step(number = 3, name = "Update encrypted data", description = "Encrypted data is updated (all other cluster members)"))
    @UseCaseScheduleRotateDataEncryption(@Step(number = 1, name = "Update encrypted data", description = "Final update of encrypted job data. Will update all SecHub jobs having a pool id which is lower than latest from encryption pool"))
    public void updateEncryptedDataIfNecessary() {
        LOG.debug("Start update of encrypted data");

        try {

            long lastStatusLogTime = 0;

            do {

                Long latestPoolid = encryptionService.getLatestCipherPoolId();

                long statusLogTimeDifferenceInMilliseconds = System.currentTimeMillis() - lastStatusLogTime;

                if (statusLogTimeDifferenceInMilliseconds > milliSecondsForNextStatusLog) {
                    long count = jobTransactionService.countCanceledOrEndedJobsWithEncryptionPoolIdLowerThan(latestPoolid);
                    LOG.info("Found {} jobs which are encrypted with a cipher pool entry lower than: {}", count, latestPoolid);

                    lastStatusLogTime = System.currentTimeMillis();
                }

                /* 1. fetch next job (s) which shall be updated */
                List<ScheduleSecHubJob> list = null;
                try {
                    list = jobTransactionService.nextCanceledOrEndedJobsWithEncryptionPoolIdLowerThan(latestPoolid, updateBlockSize);
                    if (list.isEmpty()) {
                        LOG.debug("No jobs found which must be updated.");
                        break;
                    }

                    /* 2. for every entry, rotate and save the job afterwards */
                    int updatedJobs = 0;
                    for (ScheduleSecHubJob job : list) {
                        if (rotateJobDataEncryptionAndStoreFailSafe(job)) {
                            updatedJobs++;
                        }
                    }
                    LOG.debug("Tried re-encryption of {} jobs, {} were succesful. Block size was: {}", list.size(), updatedJobs, updateBlockSize);
                } catch (ObjectOptimisticLockingFailureException e) {
                    LOG.info("Optmistic lock problem detected - will just retry");
                }

            } while (true);

        } catch (ScheduleEncryptionException e) {
            LOG.error("Was not able toupdate encrypted data because of encrpytion problem - will stop complete update. Check cipher setup!", e);
        }
        LOG.debug("Encrypted data update done");

    }

    private boolean rotateJobDataEncryptionAndStoreFailSafe(ScheduleSecHubJob job) throws ScheduleEncryptionException {
        LOG.trace("rotate job with uuid: {}", job.getUUID());

        try {
            ScheduleEncryptionResult result = encryptionService.rotateEncryption(job.getEncryptedConfiguration(), job.getEncryptionCipherPoolId(),
                    new InitializationVector(job.getEncryptionInitialVectorData()));

            job.setEncryptedConfiguration(result.getEncryptedData());
            job.setEncryptionCipherPoolId(result.getCipherPoolId());
            job.setEncryptionInitialVectorData(result.getInitialVector().getInitializationBytes());

            jobTransactionService.saveInOwnTransaction(job);

            return true;

        } catch (OptimisticLockingFailureException lockException) {
            LOG.debug("Job encryption for job: {} was not possible because row updated in mean time - either no longer necessary or will be done later again.",
                    job.getUUID());
            return false;
        }

    }

}
