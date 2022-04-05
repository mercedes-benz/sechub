// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.product;

import static com.mercedesbenz.sechub.domain.scan.product.ProductResult.*;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface ProductResultRepository extends JpaRepository<ProductResult, UUID>, ProductResultRepositoryCustom {

    @Modifying
    @Query(value = "DELETE FROM " + TABLE_NAME + " where " + COLUMN_PROJECT_ID + " = ?1", nativeQuery = true)
    void deleteAllResultsForProject(String projectId);

    @Transactional
    @Modifying
    @Query(ProductResult.QUERY_DELETE_RESULT_OLDER_THAN)
    int deleteResultsOlderThan(@Param("cleanTimeStamp") LocalDateTime cleanTimeStamp);

}
