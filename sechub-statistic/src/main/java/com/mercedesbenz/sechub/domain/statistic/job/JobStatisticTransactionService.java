package com.mercedesbenz.sechub.domain.statistic.job;

import static com.mercedesbenz.sechub.sharedkernel.util.Assert.*;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for job statistics. Will span new transactions, so data is always
 * written to DB directly
 *
 * @author Albert Tregnaghi
 *
 */
@Service
@Transactional(propagation = Propagation.REQUIRES_NEW)
public class JobStatisticTransactionService {

    private static final Logger LOG = LoggerFactory.getLogger(JobStatisticTransactionService.class);

    @Autowired
    JobStatisticRepository jobStatisticRepository;

    @Autowired
    JobStatisticDataRepository jobStatisticDataRepository;

    /**
     * Creates job statistic entry
     *
     * @param jobUUID   may not be <code>null</code>
     * @param created   represents the timestamp when the job was created, may not
     *                  be <code>null</code>
     * @param projectId may not be <code>null</code>
     */
    public void createJobStatistic(UUID jobUUID, LocalDateTime created, String projectId) {
        notNull(jobUUID, "job uuid may not be null!");

        notNull(created, "create timestamp may not be null!");
        notNull(projectId, "projectId may not be null!");

        JobStatistic jobStatistic = new JobStatistic();

        jobStatistic.setSechubJobUUID(jobUUID);

        jobStatistic.setProjectId(projectId);
        jobStatistic.setCreated(created);

        jobStatisticRepository.save(jobStatistic);
    }

    /**
     * Inserts job statistic data - will create always a new data entry
     *
     * @param jobUUID may not be <code>null</code>
     * @param type    may not be <code>null</code>
     * @param key     may not be <code>null</code>
     * @param value   may not be <code>null</code>
     */
    public void insertJobStatisticData(UUID jobUUID, JobStatisticDataType type, JobStatisticDataKey key, long value) {
        insertJobStatisticData(jobUUID, type, key, BigInteger.valueOf(value));
    }

    /**
     * Inserts job statistic data - will create always a new data entry
     *
     * @param jobUUID may not be <code>null</code>
     * @param type    may not be <code>null</code>
     * @param key     may not be <code>null</code>
     * @param value
     */
    public void insertJobStatisticData(UUID jobUUID, JobStatisticDataType type, JobStatisticDataKey key, BigInteger value) {
        JobStatisticData data = new JobStatisticData();
        validateAndSafe(type, key, value, data);
    }

    /**
     * Update existing job statistic data - or create a new one
     *
     * @param jobUUID may not be <code>null</code>
     * @param type    may not be <code>null</code>
     * @param key     may not be <code>null</code>
     * @param value   may not be <code>null</code>
     */
    public void updateJobStatisticData(UUID jobUUID, JobStatisticDataType type, JobStatisticDataKey key, long value) {
        updateJobStatisticData(jobUUID, type, key, BigInteger.valueOf(value));
    }

    /**
     * Update existing job statistic data - or create a new one
     *
     * @param jobUUID may not be <code>null</code>
     * @param type    may not be <code>null</code>
     * @param key     may not be <code>null</code>
     * @param value
     */
    public void updateJobStatisticData(UUID jobUUID, JobStatisticDataType type, JobStatisticDataKey key, BigInteger value) {
        Optional<JobStatisticData> result = jobStatisticDataRepository.findBySechubJobUUID(jobUUID);
        if (result.isEmpty()) {
            insertJobStatisticData(jobUUID, type, key, value);
        } else {
            JobStatisticData data = result.get();
            validateAndSafe(type, key, value, data);
        }
    }

    private void validateAndSafe(JobStatisticDataType type, JobStatisticDataKey key, BigInteger value, JobStatisticData data) {
        notNull(data, "Entity may not be null!");
        notNull(type, "Data type may not be null!");
        notNull(key, "Key may not be null!");
        notNull(value, "Value may not be null!");

        String keyName = key.getKeyValue();
        notNull(keyName, "Key may not be null for key object");

        if (!type.isKeyAccepted(key)) {
            LOG.error("Cannot safe, type:{} does not allow key:{}", type, key);
            return;
        }

        data.setType(type);
        data.setKey(keyName);
        data.setValue(value);

        jobStatisticDataRepository.save(data);

        LOG.debug("Statistic data saved. type={}, keyName={}, value={}, jobUUID={}, uuid={}", type, keyName, value, data.sechubJobUUID, data.uUID);
    }

}
