// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.access;

import static com.daimler.sechub.domain.scan.access.ScanAccess.*;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.daimler.sechub.domain.scan.access.ScanAccess.ProjectAccessCompositeKey;

public interface ScanAccessRepository extends JpaRepository<ScanAccess, ProjectAccessCompositeKey> {

	@Modifying
	@Query(value="DELETE FROM "+TABLE_NAME+" where "+COLUMN_USER_ID+" = ?1",nativeQuery=true)
	public void deleteAcessForUserAtAll(String userId);

	@Modifying
	@Query(value="DELETE FROM "+TABLE_NAME+" where "+COLUMN_PROJECT_ID+" = ?1",nativeQuery=true)
	public void deleteAnyAccessForProject(String projectId);
}
