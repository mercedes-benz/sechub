package com.mercedesbenz.sechub.commons.core.cache;

import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <b>Note:</b> This cache persistence is local to a single application instance
 * and is not shared across multiple instances. Avoid using it in scenarios
 * where a distributed caching mechanism is required.
 *
 * @param <T>
 */
public class InMemoryCachePersistence<T extends Serializable> implements CachePersistence<T> {

    private final Map<String, CacheData<T>> cacheMap = new ConcurrentHashMap<>();

    @Override
    public void remove(String key) {
        cacheMap.remove(key);
    }

    @Override
    public void put(String key, CacheData<T> cacheData) {
        cacheMap.put(key, cacheData);
    }

    @Override
    public CacheData<T> get(String key) {
        return cacheMap.get(key);
    }

    @Override
    public void removeOutdated(Instant now) {
        cacheMap.forEach((key, value) -> {
            Instant cacheDataCreatedAt = value.getCreatedAt();
            Duration cacheDataDuration = value.getDuration();

            if (cacheDataCreatedAt.plus(cacheDataDuration).isBefore(now)) {
                cacheMap.remove(key);
            }
        });

    }

}
