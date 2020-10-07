// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.product.config;

import static com.daimler.sechub.domain.scan.product.config.ProductExecutorConfig.*;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.daimler.sechub.domain.scan.product.ProductIdentifier;

public interface ProductExecutorConfigRepository extends JpaRepository<ProductExecutorConfig, UUID> {
    /**
     * Fetches all configurations being enabled, having wished product identifier and executor version and related to a profile which is enabled and related to given project id
     * @param projectId
     * @param identifier
     * @param version
     * @return list of configurations
     */
    //PROPERTY_PROFILES
    /* @formatter:off */
    @Query(value="select distinct c from "+ProductExecutorConfig.CLASS_NAME+" c " +
             "inner join c."+PROPERTY_PROFILES+" p " +
             "where   c."+PROPERTY_ENABLED+"=true and " +
                     "c."+PROPERTY_EXECUTORVERSION+"=:executorVersion and " +
                     "c."+PROPERTY_PRODUCTIDENTIFIER+"=:productIdentifier and " + 
                     "p."+ProductExecutionProfile.PROPERTY_ENABLED +"=true and "+
                     ":projectId member of p."+ProductExecutionProfile.PROPERTY_PROJECT_IDS,
                  
                  nativeQuery=false)
    List<ProductExecutorConfig> findExecutableConfigurationsForProject(
            @Param("projectId") String projectId,
            @Param("productIdentifier") ProductIdentifier productIdentifier, 
            @Param("executorVersion") int executorVersion);
    /* @formatter:on */


}
