// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.schedule.job;

import static com.mercedesbenz.sechub.domain.schedule.job.ScheduleSecHubJob.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
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
            + " is :executionState", nativeQuery = false)
    public long countJobsInExecutionState(@Param("executionState") ExecutionState state);

    @Transactional
    @Modifying
    @Query(ScheduleSecHubJob.QUERY_DELETE_JOB_OLDER_THAN)
    public int deleteJobsOlderThan(@Param("cleanTimeStamp") LocalDateTime cleanTimeStamp);

}
