// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.schedule.access;

import static com.daimler.sechub.domain.schedule.access.ScheduleAccess.*;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.daimler.sechub.domain.schedule.access.ScheduleAccess.ProjectAccessCompositeKey;

public interface ScheduleAccessRepository extends JpaRepository<ScheduleAccess, ProjectAccessCompositeKey> {
	
	@Query(value="SELECT EXISTS(SELECT * FROM "+TABLE_NAME+" where "+COLUMN_PROJECT_ID+"=:projectId)",nativeQuery=true)
	public boolean hasProjectUserAccess(@Param("projectId") String projectId);
	
	@Modifying
	@Query(value="DELETE FROM "+TABLE_NAME+" where "+COLUMN_USER_ID+"=:userId",nativeQuery=true)
	public void deleteAccessForUserAtAll(@Param("userId") String userId);

	@Modifying
	@Query(value="DELETE FROM "+TABLE_NAME+" where "+COLUMN_PROJECT_ID+"=:projectId",nativeQuery=true)
	public void deleteAnyAccessForProject(@Param("projectId") String projectId);
}
