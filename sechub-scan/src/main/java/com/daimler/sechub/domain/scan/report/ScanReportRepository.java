// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.report;

import static com.daimler.sechub.domain.scan.report.ScanReport.*;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface ScanReportRepository extends JpaRepository<ScanReport, UUID> {

    public ScanReport findBySecHubJobUUID(UUID secHubJobUUID);

    @Modifying
    @Query(value = "DELETE FROM " + TABLE_NAME + " where " + COLUMN_PROJECT_ID + " = ?1", nativeQuery = true)
    void deleteAllReportsForProject(String projectId);

    @Modifying
    @Query(value = "DELETE FROM " + TABLE_NAME + " where " + COLUMN_SECHUB_JOB_UUID + " = ?1", nativeQuery = true)
    void deleteAllReportsForSecHubJobUUID(UUID sechubJobUUID);
}
