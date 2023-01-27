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

import com.mercedesbenz.sechub.commons.model.TrafficLight;

/**
 * Service for job run statistics. Will span new transactions, so data is always
 * written to DB directly
 *
 * @author Albert Tregnaghi
 *
 */
@Service
@Transactional(propagation = Propagation.REQUIRES_NEW)
public class JobRunStatisticTransactionService {

    private static final Logger LOG = LoggerFactory.getLogger(JobRunStatisticTransactionService.class);

    @Autowired
    JobStatisticRepository jobStatisticRepository;

    @Autowired
    JobRunStatisticRepository jobRunStatisticRepository;

    @Autowired
    JobRunStatisticDataRepository jobRunStatisticDataRepository;

    /**
     * Creates job run statistic entry inside database and marks the job as started.
     * Will reuse information from job statistic (creation time, project id) and
     * store this information redundant inside the entity again (to make SQL queries
     * simpler for static analysis tools)
     *
     * @param executionUUID may not be <code>null</code>
     * @param started       may not be <code>null</code>
     * @param jobUUID       may not be <code>null</code>
     */
    public void createJobRunStatistic(UUID executionUUID, UUID jobUUID, LocalDateTime started) {
        notNull(executionUUID, "execution uuid may not be null!");

        notNull(jobUUID, "job uuid may not be null!");
        notNull(started, "start timestamp may not be null!");

        JobRunStatistic jobRunStatistic = new JobRunStatistic();

        jobRunStatistic.setExecutionUUID(executionUUID);

        jobRunStatistic.setSechubJobUUID(jobUUID);
        jobRunStatistic.setStarted(started);

        /* handle redundant information from existing job statistic */
        Optional<JobStatistic> jobStatisticOpt = jobStatisticRepository.findById(jobUUID);
        if (jobStatisticOpt.isEmpty()) {
            LOG.error("Was not able to find a job statistic for job:{}. Cannot create job run statistic entity because of missing data.", jobUUID);
            return;
        }
        JobStatistic jobStatistic = jobStatisticOpt.get();
        jobRunStatistic.setCreated(jobStatistic.getCreated());
        jobRunStatistic.setProjectId(jobStatistic.getProjectId());

        jobRunStatisticRepository.save(jobRunStatistic);
    }

    public void markJobRunEnded(UUID executionUUID, TrafficLight trafficLight, LocalDateTime ended, boolean failed) {
        notNull(executionUUID, "execution uuid may not be null!");
        notNull(ended, "ended may not be null!");

        Optional<JobRunStatistic> result = jobRunStatisticRepository.findById(executionUUID);
        if (result.isEmpty()) {
            LOG.error("Cannot mark job as ended, because no job run statistic object found for execution uuid:{} !", executionUUID);
            return;
        }
        JobRunStatistic jobRunStatistic = result.get();

        jobRunStatistic.trafficLight = trafficLight;
        jobRunStatistic.ended = ended;
        jobRunStatistic.failed = failed;

        jobRunStatisticRepository.save(jobRunStatistic);
    }

    /**
     * Inserts job run statistic data - will create always a new data entry
     *
     * @param executionUUID may not be <code>null</code>
     * @param type          may not be <code>null</code>
     * @param key           may not be <code>null</code>
     * @param value         may not be <code>null</code>
     */
    public void insertJobRunStatisticData(UUID executionUUID, JobRunStatisticDataType type, JobRunStatisticDataKey key, long value) {
        insertJobRunStatisticData(executionUUID, type, key, BigInteger.valueOf(value));
    }

    /**
     * Inserts job run statistic data - will create always a new data entry
     *
     * @param executionUUID may not be <code>null</code>
     * @param type          may not be <code>null</code>
     * @param key           may not be <code>null</code>
     * @param value
     */
    public void insertJobRunStatisticData(UUID executionUUID, JobRunStatisticDataType type, JobRunStatisticDataKey key, BigInteger value) {
        JobRunStatisticData data = new JobRunStatisticData();
        data.setExecutionUUID(executionUUID);
        validateAndSave(type, key, value, data);
    }

    /**
     * Update existing job statistic data - or create a new one
     *
     * @param executionUUID may not be <code>null</code>
     * @param type          may not be <code>null</code>
     * @param key           may not be <code>null</code>
     * @param value
     */
    public void updateJobRunStatisticData(UUID executionUUID, JobRunStatisticDataType type, JobRunStatisticDataKey key, long value) {
        updateJobRunStatisticData(executionUUID, type, key, BigInteger.valueOf(value));
    }

    /**
     * Update existing job statistic data - or create a new one
     *
     * @param executionUUID may not be <code>null</code>
     * @param type          may not be <code>null</code>
     * @param key           may not be <code>null</code>
     * @param value         may not be <code>null</code>
     */
    public void updateJobRunStatisticData(UUID executionUUID, JobRunStatisticDataType type, JobRunStatisticDataKey key, BigInteger value) {
        Optional<JobRunStatisticData> result = jobRunStatisticDataRepository.findByExecutionUUID(executionUUID);
        if (result.isEmpty()) {
            insertJobRunStatisticData(executionUUID, type, key, value);
        } else {
            JobRunStatisticData data = result.get();
            validateAndSave(type, key, value, data);
        }
    }

    private void validateAndSave(JobRunStatisticDataType type, JobRunStatisticDataKey key, BigInteger value, JobRunStatisticData data) {
        notNull(type, "Data type may not be null!");
        notNull(key, "Key may not be null!");
        notNull(value, "Value may not be null!");

        notNull(data, "Entity may not be null!");
        notNull(data.executionUUID, "Execution uuid inside entity may not be null!");

        String keyName = key.getKeyValue();
        notNull(keyName, "Key may not be null for key object");

        if (!type.isKeyAccepted(key)) {
            LOG.error("Cannot safe, type:{} does not allow key:{}", type, key);
            return;
        }

        data.setType(type);
        data.setKey(key.getKeyValue());
        data.setValue(value);

        jobRunStatisticDataRepository.save(data);

        LOG.debug("Statistic data saved. type={}, keyName={}, value={}, executionUUID={}, uuid={}", type, keyName, value, data.executionUUID, data.uUID);
    }
}
