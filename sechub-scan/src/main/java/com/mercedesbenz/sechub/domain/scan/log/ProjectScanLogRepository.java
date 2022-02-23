// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.log;

import static com.mercedesbenz.sechub.domain.scan.log.ProjectScanLog.*;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface ProjectScanLogRepository extends JpaRepository<ProjectScanLog, UUID>, ProjectScanLogRepositoryCustom {

    @Modifying
    @Query(value = "DELETE FROM " + TABLE_NAME + " where " + COLUMN_PROJECT_ID + " = ?1", nativeQuery = true)
    void deleteAllLogDataForProject(String projectId);

    @Transactional
    @Modifying
    @Query(ProjectScanLog.QUERY_DELETE_LOGS_OLDER_THAN)
    void deleteLogsOlderThan(@Param("cleanTimeStamp") LocalDateTime cleanTimeStamp);

}
