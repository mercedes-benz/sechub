// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.core.cache;

import static java.util.Objects.requireNonNull;

import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.crypto.SealedObject;

import com.mercedesbenz.sechub.commons.core.security.CryptoAccess;
import com.mercedesbenz.sechub.commons.core.shutdown.ApplicationShutdownHandler;
import com.mercedesbenz.sechub.commons.core.shutdown.ShutdownListener;

/**
 * The <code>InMemoryCache</code> class provides a thread-safe, in-memory
 * caching mechanism for generic data.
 *
 * <p>
 * It uses a {@link ConcurrentHashMap} to store data and a scheduled task to
 * clear expired entries periodically. By default, the cache cleanup runs every
 * minute with an initial delay of 1 minute. These values can be customized via
 * the constructor.
 * </p>
 *
 * <p>
 * <b>Note:</b> This cache is local to a single application instance and is not
 * shared across multiple instances. Avoid using it in scenarios where a
 * distributed caching mechanism is required.
 * </p>
 *
 * @param <T> the type of data stored in the cache (must be of type
 *            {@link Serializable})
 *
 * @author hamidonos
 */
public class InMemoryCache<T extends Serializable> implements ShutdownListener {

    private static final Duration CACHE_CLEAR_JOB_PERIOD_DEFAULT = Duration.ofMinutes(1);

    private final ConcurrentHashMap<String, CacheData> cache = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    private final ScheduledFuture<?> cacheClearJob;
    private final Duration cacheClearJobPeriod;

    public InMemoryCache(ApplicationShutdownHandler applicationShutdownHandler) {
        this(CACHE_CLEAR_JOB_PERIOD_DEFAULT, applicationShutdownHandler);
    }

    public InMemoryCache(Duration cacheClearJobPeriod, ApplicationShutdownHandler applicationShutdownHandler) {
        this.cacheClearJobPeriod = requireNonNull(cacheClearJobPeriod, "Property 'cacheClearJobPeriod' must not be null");
        cacheClearJob = scheduleClearCacheJob();
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
        cache.put(key, new CacheData(value, duration));
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
        CacheData cacheData = cache.get(key);

        if (cacheData == null) {
            return Optional.empty();
        }

        return Optional.of(cacheData.getValue());
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
        Instant now = Instant.now();

        cache.forEach((key, value) -> {
            Instant cacheCreatedAt = value.getCreatedAt();
            Duration cacheDuration = value.getDuration();

            if (cacheCreatedAt.plus(cacheDuration).isBefore(now)) {
                cache.remove(key);
            }
        });
    }

    /**
     * Represents the data stored in the cache under a specific key.
     *
     * <p>
     * The cached data can be any serializable object. It is securely sealed using a
     * {@link CryptoAccess} instance of type <code>T</code>.
     * </p>
     */
    private class CacheData {

        // TODO: static constant not possible because of T ?
        private final CryptoAccess<T> cryptoAccess = new CryptoAccess<>();
        private final SealedObject sealedValue;
        private final Duration duration;
        private final Instant createdAt = Instant.now();

        public CacheData(T value, Duration duration) {
            requireNonNull(value, "Property 'value' must not be null");
            this.sealedValue = cryptoAccess.seal(value);
            this.duration = requireNonNull(duration, "Property 'duration' must not be null");
        }

        public T getValue() {
            return cryptoAccess.unseal(sealedValue);
        }

        public Duration getDuration() {
            return duration;
        }

        public Instant getCreatedAt() {
            return createdAt;
        }
    }
}
