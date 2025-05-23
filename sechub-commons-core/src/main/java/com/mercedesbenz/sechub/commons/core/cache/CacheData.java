package com.mercedesbenz.sechub.commons.core.cache;

import static java.util.Objects.*;

import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;

import javax.crypto.SealedObject;

import com.mercedesbenz.sechub.commons.core.security.CryptoAccess;
import com.mercedesbenz.sechub.commons.core.security.CryptoAccessProvider;

/**
 * Represents the data stored in the cache under a specific key.
 *
 * <p>
 * The cached data can be any serializable object. If cryptoAccess provider is
 * nut null, the value is securely sealed using a {@link CryptoAccess} instance
 * of type otherwise the value be stored directly. <code>T</code>.
 * </p>
 */
public class CacheData<T extends Serializable> {

    private final CryptoAccessProvider<T> cryptoAccessProvider;
    private final SealedObject sealedValue;
    private final Duration duration;
    private final T value;
    private final Instant createdAt;

    /**
     * Creates a cache data object. If crypto access provider is not null, a sealed
     * value will be used, otherwise the plain value will be stored directly in
     * memory.
     *
     * @param value                value
     * @param duration             duration
     * @param cryptoAccessProvider crypto access provider
     * @param createdAt            creation time of cache entry, normally
     *                             Instant.now() except when the cache element is
     *                             restored from somewhere else.
     */
    public CacheData(T value, Duration duration, CryptoAccessProvider<T> cryptoAccessProvider, Instant createdAt) {

        requireNonNull(value, "Property 'value' must not be null");

        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.cryptoAccessProvider = cryptoAccessProvider;

        if (cryptoAccessProvider == null) {
            this.value = value;
            this.sealedValue = null;
        } else {
            this.value = null;
            this.sealedValue = cryptoAccessProvider.getCryptoAccess().seal(value);
        }
        this.duration = requireNonNull(duration, "Property 'duration' must not be null");
    }

    public boolean isSealed() {
        return sealedValue != null;
    }

    public T getValue() {
        if (cryptoAccessProvider == null) {
            return value;
        }
        return cryptoAccessProvider.getCryptoAccess().unseal(sealedValue);
    }

    public Duration getDuration() {
        return duration;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}