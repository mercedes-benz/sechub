// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.report;

import static com.mercedesbenz.sechub.domain.scan.report.ScanReport.*;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface ScanReportRepository extends JpaRepository<ScanReport, UUID> {

    public ScanReport findBySecHubJobUUID(UUID secHubJobUUID);

    Optional<ScanReport> findTopByProjectIdOrderByStartedDesc(String projectId);

    @Modifying
    @Query(value = "DELETE FROM " + TABLE_NAME + " where " + COLUMN_PROJECT_ID + " = ?1", nativeQuery = true)
    void deleteAllReportsForProject(String projectId);

    @Modifying
    @Query(value = "DELETE FROM " + TABLE_NAME + " where " + COLUMN_SECHUB_JOB_UUID + " = ?1", nativeQuery = true)
    void deleteAllReportsForSecHubJobUUID(UUID sechubJobUUID);

    @Transactional
    @Modifying
    @Query(ScanReport.QUERY_DELETE_REPORTS_OLDER_THAN)
    int deleteReportsOlderThan(@Param("cleanTimeStamp") LocalDateTime cleanTimeStamp);
}
