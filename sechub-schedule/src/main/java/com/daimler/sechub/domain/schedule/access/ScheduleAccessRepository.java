// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.schedule.access;

import static com.daimler.sechub.domain.schedule.access.ScheduleAccess.*;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.daimler.sechub.domain.schedule.access.ScheduleAccess.ProjectAccessCompositeKey;

public interface ScheduleAccessRepository extends JpaRepository<ScheduleAccess, ProjectAccessCompositeKey> {

	@Modifying
	@Query(value="DELETE FROM "+TABLE_NAME+" where "+COLUMN_USER_ID+" = ?1",nativeQuery=true)
	public void deleteAcessForUserAtAll(String userId);
}
