// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.product.config;

import static com.daimler.sechub.domain.scan.product.config.ProductExecutionProfile.*;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductExecutionProfileRepository extends JpaRepository<ProductExecutionProfile, String> {

    @Modifying
    @Query(value="DELETE FROM "+TABLE_NAME_PROFILE_TO_PROJECT+" where "+COLUMN_PROJECT_IDS+" =:projectId",nativeQuery=true)
    void deleteAllProfileRelationsForProject(@Param("projectId")String projectId);


}
