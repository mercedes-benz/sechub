// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.project;

import static com.mercedesbenz.sechub.domain.scan.project.ScanProjectConfig.*;

import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mercedesbenz.sechub.domain.scan.project.ScanProjectConfig.ScanProjectConfigCompositeKey;

public interface ScanProjectConfigRepository extends JpaRepository<ScanProjectConfig, ScanProjectConfigCompositeKey> {

    @Modifying
    @Query(value = "DELETE FROM " + TABLE_NAME + " where " + COLUMN_PROJECT_ID + " = ?1", nativeQuery = true)
    void deleteAllConfigurationsForProject(String projectId);

    @Modifying
    @Query(value = "DELETE FROM " + TABLE_NAME + " where " + COLUMN_CONFIG_ID + " in ?1 and " + COLUMN_DATA + " =?2", nativeQuery = true)
    void deleteAllConfigurationsOfGivenConfigIdsAndValue(Set<String> configIds, String value);

    @Query(value = ScanProjectConfig.QUERY_FIND_ALL_CONFIGURATIONS_FOR_PROJECT)
    List<ScanProjectConfig> findAllForProject(@Param("projectId") String projectId);

    @Query(value = ScanProjectConfig.QUERY_FIND_ALL_DATA_FOR_CONFIG_ID)
    List<String> findAllDataForConfigId(@Param("configId") String configId);

    @Query(value = ScanProjectConfig.QUERY_FIND_ALL_PROJECT_IDS_FOR_SET_OF_CONFIG_IDS_AND_DATA)
    Set<String> findAllProjectsWhereConfigurationHasGivenData(@Param("configIds") Set<String> configIds, @Param("data") String data);

}
