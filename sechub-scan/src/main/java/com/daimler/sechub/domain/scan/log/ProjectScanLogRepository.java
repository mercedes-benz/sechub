// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.log;

import static com.daimler.sechub.domain.scan.log.ProjectScanLog.*;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface ProjectScanLogRepository extends JpaRepository<ProjectScanLog, UUID>, ProjectScanLogRepositoryCustom {

    @Modifying
    @Query(value = "DELETE FROM " + TABLE_NAME + " where " + COLUMN_PROJECT_ID + " = ?1", nativeQuery = true)
    void deleteAllLogDataForProject(String projectId);

}
