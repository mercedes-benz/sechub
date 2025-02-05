// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.product.config;

import static com.mercedesbenz.sechub.domain.scan.product.config.ProductExecutionProfile.CLASS_NAME;
import static com.mercedesbenz.sechub.domain.scan.product.config.ProductExecutionProfile.PROFILE_TO_PROJECT__COLUMN_PROFILE_ID;
import static com.mercedesbenz.sechub.domain.scan.product.config.ProductExecutionProfile.PROFILE_TO_PROJECT__COLUMN_PROJECT_ID;
import static com.mercedesbenz.sechub.domain.scan.product.config.ProductExecutionProfile.PROPERTY_ID;
import static com.mercedesbenz.sechub.domain.scan.product.config.ProductExecutionProfile.PROPERTY_PROJECT_IDS;
import static com.mercedesbenz.sechub.domain.scan.product.config.ProductExecutionProfile.TABLE_NAME_PROFILE_TO_PROJECT;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductExecutionProfileRepository extends JpaRepository<ProductExecutionProfile, String> {

    @Modifying
    @Query(value = "DELETE FROM " + TABLE_NAME_PROFILE_TO_PROJECT + " where " + PROFILE_TO_PROJECT__COLUMN_PROJECT_ID + " =:projectId", nativeQuery = true)
    void deleteAllProfileRelationsToProject(@Param("projectId") String projectId);

    @Modifying
    @Query(value = "INSERT INTO " + TABLE_NAME_PROFILE_TO_PROJECT + " ( " + PROFILE_TO_PROJECT__COLUMN_PROFILE_ID + "," + PROFILE_TO_PROJECT__COLUMN_PROJECT_ID
            + ") VALUES (:profileId,:projectId)", nativeQuery = true)
    void createProfileRelationToProject(@Param("profileId") String profileId, @Param("projectId") String projectId);

    @Modifying
    @Query(value = "DELETE FROM " + TABLE_NAME_PROFILE_TO_PROJECT + " where " + PROFILE_TO_PROJECT__COLUMN_PROFILE_ID + " =:profileId AND "
            + PROFILE_TO_PROJECT__COLUMN_PROJECT_ID + " =:projectId", nativeQuery = true)
    void deleteProfileRelationToProject(@Param("profileId") String profileId, @Param("projectId") String projectId);

    @Query(value = "select count (p) from " + CLASS_NAME + " p where p." + PROPERTY_ID + " =:profileId and :projectId member of p." + PROPERTY_PROJECT_IDS)
    int countRelationShipEntries(@Param("profileId") String profileId, @Param("projectId") String projectId);

    @Query(value = "select p from " + CLASS_NAME + " p where :projectId member of p." + PROPERTY_PROJECT_IDS)
    public List<ProductExecutionProfile> findExecutionProfilesForProject(String projectId);
}
