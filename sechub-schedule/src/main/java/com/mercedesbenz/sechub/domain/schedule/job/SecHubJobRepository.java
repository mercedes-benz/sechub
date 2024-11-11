// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.schedule.job;

import static com.mercedesbenz.sechub.domain.schedule.job.ScheduleSecHubJob.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.mercedesbenz.sechub.commons.model.job.ExecutionState;

public interface SecHubJobRepository extends JpaRepository<ScheduleSecHubJob, UUID>, SecHubJobRepositoryCustom, JpaSpecificationExecutor<ScheduleSecHubJob> {

    @Query(value = "SELECT * FROM " + TABLE_NAME + " where " + COLUMN_PROJECT_ID + " = ?1 and " + COLUMN_UUID + " = ?2", nativeQuery = true)
    public Optional<ScheduleSecHubJob> findForProject(String projectId, UUID jobUUID);

    @Query(value = "SELECT t from " + ScheduleSecHubJob.CLASS_NAME + " t where t." + PROPERTY_STARTED + " <= :untilLocalDateTime and t." + PROPERTY_ENDED
            + " is NULL", nativeQuery = false)
    public List<ScheduleSecHubJob> findAllRunningJobsStartedBefore(@Param("untilLocalDateTime") LocalDateTime untilLocalDateTime);

    @Query(value = "SELECT COUNT(t) FROM " + ScheduleSecHubJob.CLASS_NAME + " t where t." + PROPERTY_EXECUTION_STATE
            + " = :executionState", nativeQuery = false)
    public long countJobsInExecutionState(@Param("executionState") ExecutionState state);

    @Query(value = "SELECT COUNT(t) FROM " + ScheduleSecHubJob.CLASS_NAME + " t where t." + PROPERTY_EXECUTION_STATE + " = :executionState and t."
            + PROPERTY_ENCRYPTION_POOL_ID + " = :encryptionPoolId", nativeQuery = false)
    public long countJobsInExecutionStateAndEncryptedWithPoolId(@Param("executionState") ExecutionState state,
            @Param("encryptionPoolId") Long encryptionPoolId);

    /**
     * Resolve job uuids together with project ids of jobs which are older than
     * given time stamp. <br>
     * <br>
     * Example usage:
     *
     * <pre>
     * <code>
     *  for (Object part: result) {
     *
     *      UUID jobUUID = part[0];
     *      String projectId = part[1];
     *
     *      doSomethingForJobAndProject(jobUUID,projectId);
     *  }
     *
     * </code>
     * </pre>
     *
     *
     * @param cleanTimeStamp
     * @return list containing object arrays of size 2. Format is described in
     *         javadoc before.
     */
    @Query(ScheduleSecHubJob.QUERY_SELECT_JOB_UUID_AND_PROJECT_ID_FOR_JOBS_OLDER_THAN)
    public List<Object[]> findJobUUIDsAndProjectIdsForJobsOlderThan(@Param("cleanTimeStamp") LocalDateTime cleanTimeStamp);

    @Transactional
    @Modifying
    @Query(ScheduleSecHubJob.QUERY_DELETE_JOB_OLDER_THAN)
    public int deleteJobsOlderThan(@Param("cleanTimeStamp") LocalDateTime cleanTimeStamp);

    /**
     * Marks all jobs for given uuids as SUSPENDED and also set ended date time in
     * one transaction
     *
     * @param sechubJobUUIDs
     * @param ended
     */
    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(ScheduleSecHubJob.QUERY_MARK_JOBS_AS_SUSPENDED)
    public void markJobsAsSuspended(@Param("sechubJobUUIDs") Set<UUID> sechubJobUUIDs, @Param("endTime") LocalDateTime ended);

}
