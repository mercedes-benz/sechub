package com.mercedesbenz.sechub.domain.schedule.job;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.commons.encryption.InitializationVector;
import com.mercedesbenz.sechub.domain.schedule.encryption.ScheduleEncryptionException;
import com.mercedesbenz.sechub.domain.schedule.encryption.ScheduleEncryptionResult;
import com.mercedesbenz.sechub.domain.schedule.encryption.ScheduleEncryptionService;

@Service
public class ScheduleSecHubJobEncryptionUpdateService {

    private static final Logger LOG = LoggerFactory.getLogger(ScheduleSecHubJobEncryptionUpdateService.class);

    @Autowired
    SecHubConfigurationModelAccess modelAccess;

    @Autowired
    ScheduleEncryptionService encryptionService;

    @Autowired
    SecHubJobTransactionService jobTransactionService;

    @Autowired
    SecHubJobRepository repository;

    public void updateEncryptionData(int blockSizeToUpdate) {
        Long latestPoolid = encryptionService.getLatestCipherPoolId();

        /* 1. fetch next job (s) which shall be updated */
        List<ScheduleSecHubJob> list = repository.nextCanceledOrEndedJobsWithEncryptionPoolIdLowerThan(latestPoolid, blockSizeToUpdate);
        if (list.isEmpty()) {
            return;
        }

        /* 2. for every entry, rotate and save the job afterwards */
        for (ScheduleSecHubJob job : list) {
            rotateJobDataEncryptionAndStoreFailSafe(job);
        }

    }

    private void rotateJobDataEncryptionAndStoreFailSafe(ScheduleSecHubJob job) {

        try {
            ScheduleEncryptionResult result = encryptionService.rotateEncryption(job.getEncryptedConfiguration(), job.getEncryptionCipherPoolId(),
                    new InitializationVector(job.getEncryptionInitialVectorData()));

            job.setEncryptedConfiguration(result.getEncryptedData());
            job.setEncryptionCipherPoolId(result.getCipherPoolId());
            job.setEncryptionInitialVectorData(result.getInitialVector().getInitializationBytes());

            jobTransactionService.saveInOwnTransaction(job);
        } catch (ScheduleEncryptionException e) {
            LOG.error("Was not able to rotate encryption!", e);
        } catch (OptimisticLockingFailureException lockException) {
            LOG.info("Job encryption for job: {} was not possible because row updated in mean time - either no longer necessary or will be done later again.",
                    job.getUUID());
        }

    }

}
