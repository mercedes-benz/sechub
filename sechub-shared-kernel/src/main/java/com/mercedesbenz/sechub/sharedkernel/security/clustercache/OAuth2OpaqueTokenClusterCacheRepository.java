// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.security.clustercache;

import java.time.Instant;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface OAuth2OpaqueTokenClusterCacheRepository extends JpaRepository<OAuth2OpaqueTokenClusterCache, String> {

    /* @formatter:off */
    @Transactional
    @Modifying
    @Query("DELETE from "+OAuth2OpaqueTokenClusterCache.CLASS_NAME+" c "
            + "WHERE c."+OAuth2OpaqueTokenClusterCache.PROPERTY_EXPIRES_AT +" <= :now")
    /* @formatter:on */
    int removeOutdated(Instant now);

    Optional<OAuth2OpaqueTokenClusterCache> findFirstByOpaqueTokenOrderByCreatedAtDesc(String opaqueToken);

    /* @formatter:off */
    @Transactional
    @Modifying
    @Query("DELETE from "+OAuth2OpaqueTokenClusterCache.CLASS_NAME+" c "
            + "WHERE c."+OAuth2OpaqueTokenClusterCache.PROPERTY_OPAQUE_TOKEN +" = :opaqueToken")
    /* @formatter:on */
    void deleteAllByOpaqueToken(String opaqueToken);

}
