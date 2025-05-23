// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.core.cache;

import static java.util.Objects.*;

import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mercedesbenz.sechub.commons.core.security.CryptoAccessProvider;
import com.mercedesbenz.sechub.commons.core.shutdown.ApplicationShutdownHandler;
import com.mercedesbenz.sechub.commons.core.shutdown.ShutdownListener;

/**
 * The <code>InMemoryCache</code> class provides a thread-safe, in-memory
 * caching mechanism for generic data.
 *
 * <p>
 * It uses a {@link CachePersistence} to store data and a scheduled task to
 * clear expired entries periodically. By default, the cache cleanup runs every
 * minute with an initial delay of 1 minute. These values can be customized via
 * the constructor.
 * </p>
 *
 * @param <T> the type of data stored in the cache (must be of type
 *            {@link Serializable})
 *
 * @author hamidonos, de-jcup
 */
public class SelfCleaningCache<T extends Serializable> implements ShutdownListener {

    private static final Logger logger = LoggerFactory.getLogger(SelfCleaningCache.class);

    private final String cacheName;
    private final ScheduledExecutorService scheduledExecutorService;
    private final ScheduledFuture<?> cacheClearJob;
    private final Duration cacheClearJobPeriod;
    private final CachePersistence<T> cachePersistence;
    private final CryptoAccessProvider<T> cryptoAccessProvider;

    /* @formatter:off */
    public SelfCleaningCache(String cacheName, CachePersistence<T> cachePersistence, Duration cacheClearJobPeriod,
                         ScheduledExecutorService scheduledExecutorService,
                         ApplicationShutdownHandler applicationShutdownHandler, CryptoAccessProvider<T> cryptoAccessProvider) {
        /* @formatter:on */
        this.cacheName = requireNonNull(cacheName, "Parameter 'cacheName' must not be null");
        this.cachePersistence = requireNonNull(cachePersistence, "Parameter 'cachePersistence' must not be null");
        this.scheduledExecutorService = requireNonNull(scheduledExecutorService, "Property 'scheduledExecutorService' must not be null");
        this.cacheClearJobPeriod = requireNonNull(cacheClearJobPeriod, "Property 'cacheClearJobPeriod' must not be null");
        this.cryptoAccessProvider = cryptoAccessProvider;

        cacheClearJob = scheduleClearCacheJob();

        requireNonNull(applicationShutdownHandler, "Property 'applicationShutdownHandler' must not be null");
        applicationShutdownHandler.register(this);

    }

    /**
     * Puts the specified value into the cache with the specified key and duration.
     *
     * <p>
     * <b>Note:</b> this will override any existing cache data with the same key.
     * </p>
     *
     * @param key      the key under which the cache data is stored
     * @param value    the generic data to be stored in the cache
     * @param duration the duration for which the data is stored in the cache
     *
     * @throws NullPointerException if the specified key, value or duration is null
     */
    public void put(String key, T value, Duration duration) {
        requireNonNull(key, "Argument 'key' must not be null");
        if (logger.isTraceEnabled()) {
            logger.trace("Cache:{} - Put to persistence ({}): key={}, duration={}, value= {}", cacheName, cachePersistence.getClass().getSimpleName(), key,
                    duration, value);
        }
        cachePersistence.put(key, new CacheData<T>(value, duration, cryptoAccessProvider, Instant.now()));
    }

    /**
     * Retrieves the value from the cache with the specified key.
     *
     * @param key the key under which the cache data is stored
     *
     * @return an {@link Optional} containing the cached value of type
     *         <code>T</code> or an empty {@link Optional} if the value is not
     *         present
     *
     * @throws NullPointerException if the specified key is null
     */
    public Optional<T> get(String key) {
        requireNonNull(key, "Argument 'key' must not be null");

        CacheData<T> cacheData = cachePersistence.get(key);

        if (cacheData == null) {
            if (logger.isTraceEnabled()) {
                logger.trace("Cache:{} - Get: key={} - not found", cacheName, key);
            }
            return Optional.empty();
        }

        if (logger.isTraceEnabled()) {
            logger.trace("Cache:{} - Get: key={} - found", cacheName, key);
        }
        return Optional.of(cacheData.getValue());
    }

    /**
     * Removes cached value from cache. If key does not exist it will be just
     * ignored
     *
     * @param key the key under which the cache data is stored
     * @throws NullPointerException if the specified key is null
     */
    public void remove(String key) {
        requireNonNull(key, "key must not be null!");
        if (logger.isTraceEnabled()) {
            logger.trace("Cache:{} - Remove: key={}", cacheName, key);
        }
        cachePersistence.remove(key);
    }

    public Duration getCacheClearJobPeriod() {
        return cacheClearJobPeriod;
    }

    @Override
    public void onShutdown() {
        cacheClearJob.cancel(true);
        scheduledExecutorService.shutdownNow();
    }

    private ScheduledFuture<?> scheduleClearCacheJob() {
        /* @formatter:off */
        return scheduledExecutorService.scheduleAtFixedRate(
                this::clearCache,
                Duration.ZERO.toMillis(),
                cacheClearJobPeriod.toMillis(),
                TimeUnit.MILLISECONDS);
        /* @formatter:on */
    }

    private void clearCache() {
        if (logger.isTraceEnabled()) {
            logger.trace("Cache:{} - clear cache", cacheName);
        }
        cachePersistence.removeOutdated(Instant.now());
    }

}
