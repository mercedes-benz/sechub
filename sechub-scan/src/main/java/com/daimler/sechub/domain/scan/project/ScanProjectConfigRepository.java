// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.project;

import static com.daimler.sechub.domain.scan.project.ScanProjectConfig.*;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.daimler.sechub.domain.scan.project.ScanProjectConfig.ScanProjectConfigCompositeKey;

public interface ScanProjectConfigRepository extends JpaRepository<ScanProjectConfig, ScanProjectConfigCompositeKey> {

    @Modifying
    @Query(value = "DELETE FROM " + TABLE_NAME + " where " + COLUMN_PROJECT_ID + " = ?1", nativeQuery = true)
    void deleteAllConfigurationsForProject(String projectId);

}
