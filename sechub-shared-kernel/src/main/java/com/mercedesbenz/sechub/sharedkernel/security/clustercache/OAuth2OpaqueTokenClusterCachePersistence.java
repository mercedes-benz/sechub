// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.security.clustercache;

import static java.util.Objects.*;

import java.time.Instant;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.commons.core.cache.CacheData;
import com.mercedesbenz.sechub.commons.core.cache.CachePersistence;
import com.mercedesbenz.sechub.commons.core.security.CryptoAccessProvider;
import com.mercedesbenz.sechub.commons.model.JSONConverter;
import com.mercedesbenz.sechub.commons.model.JSONConverterException;
import com.mercedesbenz.sechub.spring.security.OAuth2OpaqueTokenIntrospectionResponse;

@Component
public class OAuth2OpaqueTokenClusterCachePersistence implements CachePersistence<OAuth2OpaqueTokenIntrospectionResponse> {

    private static final Logger logger = LoggerFactory.getLogger(OAuth2OpaqueTokenClusterCachePersistence.class);
    private final OAuth2OpaqueTokenClusterCacheRepository repository;
    private CryptoAccessProvider<OAuth2OpaqueTokenIntrospectionResponse> cryptoAccessProvider;

    OAuth2OpaqueTokenClusterCachePersistence(OAuth2OpaqueTokenClusterCacheRepository repository,
            CryptoAccessProvider<OAuth2OpaqueTokenIntrospectionResponse> cryptoAccessProvider) {
        this.repository = requireNonNull(repository, "Parameter 'repository' may not be null.");
        this.cryptoAccessProvider = requireNonNull(cryptoAccessProvider, "Parameter 'cryptoAccessProvider' may not be null.");
    }

    @Override
    public void put(String key, CacheData<OAuth2OpaqueTokenIntrospectionResponse> cacheData) {
        String introspectionResponse = JSONConverter.get().toJSON(cacheData.getValue());

        handleResilient("update opaque token cache entry", () -> {

            OAuth2OpaqueTokenClusterCache clusterCache;

            Optional<OAuth2OpaqueTokenClusterCache> fromDB = repository.findById(key);
            if (fromDB.isPresent()) {
                /* update existing entry */
                clusterCache = fromDB.get();
                clusterCache.update(introspectionResponse, cacheData.getCreatedAt(), cacheData.getDuration());

            } else {
                /* create new entry */
                clusterCache = new OAuth2OpaqueTokenClusterCache(key, introspectionResponse, cacheData.getDuration(), cacheData.getCreatedAt());
            }

            repository.save(clusterCache);
        }

        );

    }

    @Override
    public CacheData<OAuth2OpaqueTokenIntrospectionResponse> get(String opaqueToken) {

        Optional<OAuth2OpaqueTokenClusterCache> data = repository.findById(opaqueToken);
        if (data.isEmpty()) {
            return null;
        }

        OAuth2OpaqueTokenClusterCache clusterCache = data.get();
        String json = clusterCache.getIntroSpectionResponse();

        try {
            OAuth2OpaqueTokenIntrospectionResponse result = JSONConverter.get().fromJSON(OAuth2OpaqueTokenIntrospectionResponse.class, json);
            CacheData<OAuth2OpaqueTokenIntrospectionResponse> cacheData = new CacheData<>(result, clusterCache.getDuration(), cryptoAccessProvider,
                    clusterCache.getCreatedAt());
            return cacheData;

        } catch (JSONConverterException e) {
            logger.error("Not expected json, will drop old result and return null instead", e);
            handleResilient("remove opaque token from cache (json was invalid)", () -> remove(opaqueToken));
            return null;
        }

    }

    @Override
    public void remove(String opaqueToken) {
        handleResilient("remove opaque token from cache", () -> repository.deleteById(opaqueToken));
    }

    @Override
    public void removeOutdated(Instant now) {
        handleResilient("remove outdated token cache entries", () -> repository.removeOutdated(now));
    }

    private void handleResilient(String actionName, Runnable runnable) {
        handleResilient(actionName, runnable, 3, 300);
    }

    private void handleResilient(String actionName, Runnable runnable, int maxRetries, long millsecondsToWaitBeforeRetry) {
        int retries = 0;
        while (isRetryPossible(maxRetries, retries)) {
            try {

                runnable.run();

                break;

            } catch (Exception e) {
                retries++;
                if (!isRetryPossible(maxRetries, retries)) {
                    logger.error("{} failed {} time(s) - no more retries possible.", actionName, retries);
                    throw e;
                }
                logger.warn("{} failed {}. time. Message was: {}", actionName, retries, e.getMessage());
                try {
                    Thread.sleep(millsecondsToWaitBeforeRetry);
                } catch (InterruptedException e1) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        if (retries > 0) {
            logger.info("{} was successful on {}. retry", actionName, retries);
        }

    }

    private boolean isRetryPossible(int maxRetries, int count) {
        return count < maxRetries;
    }

}
