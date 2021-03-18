// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.schedule.job;

import static com.daimler.sechub.domain.schedule.job.ScheduleSecHubJob.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Component;

@Component
public interface SecHubJobRepository extends JpaRepository<ScheduleSecHubJob, UUID>, SecHubJobRepositoryCustom {

	@Query(value = "SELECT * FROM " + TABLE_NAME + " where " + COLUMN_PROJECT_ID + " = ?1 and " + COLUMN_UUID + " = ?2", nativeQuery = true)
	public Optional<ScheduleSecHubJob> findForProject(String projectId, UUID jobUUID);

	@Query(value = "SELECT COUNT(t) FROM " + ScheduleSecHubJob.CLASS_NAME + " t where t." + PROPERTY_STARTED + " is NOT NULL and t."+ PROPERTY_ENDED+ " is NULL", nativeQuery = false)
	public long countRunningJobs();

	
	@Query(value = "SELECT t from " + ScheduleSecHubJob.CLASS_NAME + " t where t." + PROPERTY_STARTED + " <= :untilLocalDateTime and t."+ PROPERTY_ENDED+ " is NULL", nativeQuery = false)
    public List<ScheduleSecHubJob> findAllRunningJobsStartedBefore(@Param("untilLocalDateTime")LocalDateTime untilLocalDateTime);

	
	@Query(value = "SELECT COUNT(t) FROM " + ScheduleSecHubJob.CLASS_NAME + " t where t." + PROPERTY_STARTED + " is NULL", nativeQuery = false)
	public long countWaitingJobs();
	
	@Modifying
	@Query(value = "DELETE FROM " + ScheduleSecHubJob.CLASS_NAME + " t where t." + PROPERTY_STARTED + " is NULL", nativeQuery = false)
    public void deleteWaitingJobs();

}
