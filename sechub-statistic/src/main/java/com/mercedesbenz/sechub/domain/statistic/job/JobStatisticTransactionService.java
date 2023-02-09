package com.mercedesbenz.sechub.domain.statistic.job;

import static com.mercedesbenz.sechub.sharedkernel.util.Assert.*;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.mercedesbenz.sechub.domain.statistic.StatisticDataContainer;
import com.mercedesbenz.sechub.domain.statistic.StatisticDataKey;
import com.mercedesbenz.sechub.domain.statistic.StatisticDataKeyValue;

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
     * @param value
     */
    public void insertJobStatisticData(UUID jobUUID, JobStatisticDataType type, StatisticDataKey key, BigInteger value) {
        JobStatisticData data = new JobStatisticData();
        data.setSechubJobUUID(jobUUID);

        validateAndSafe(type, key, value, data);
    }

    /**
     * Inserts job statistic data - will create always new data entries
     *
     * @param jobUUID       may not be <code>null</code>
     * @param dataContainer may not be <code>null</code>
     */
    public void insertJobStatisticData(UUID jobUUID, StatisticDataContainer<JobStatisticDataType> dataContainer) {
        notNull(jobUUID, "jobUUID may not be null");
        notNull(dataContainer, "data container may not be null");

        Set<JobStatisticDataType> types = dataContainer.getTypes();
        for (JobStatisticDataType type : types) {
            List<StatisticDataKeyValue> keyValues = dataContainer.getKeyValues(type);
            for (StatisticDataKeyValue keyValue : keyValues) {
                insertJobStatisticData(jobUUID, type, keyValue.getKey(), keyValue.getValue());
            }
        }

    }

    private void validateAndSafe(JobStatisticDataType type, StatisticDataKey key, BigInteger value, JobStatisticData data) {
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
        data.setId(keyName);
        data.setValue(value);

        jobStatisticDataRepository.save(data);

        LOG.debug("Statistic data saved. type={}, keyName={}, value={}, jobUUID={}, uuid={}", type, keyName, value, data.sechubJobUUID, data.uUID);
    }

}
