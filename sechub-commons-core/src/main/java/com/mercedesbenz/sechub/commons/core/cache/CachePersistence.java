// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.core.cache;

import java.io.Serializable;
import java.time.Instant;

public interface CachePersistence<T extends Serializable> {

    /**
     * Updates or creates a cache entry
     *
     * @param key       the key for the cache entry
     * @param cacheData cache data containing
     */
    void put(String key, CacheData<T> cacheData);

    /**
     * Fetch cache entry for given key
     *
     * @param key key to identify value
     * @return cache data or <code>null</code> if not available
     */
    CacheData<T> get(String key);

    /**
     * Remove entry from cache
     *
     * @param key key to identify value
     */
    void remove(String key);

    /**
     * Removes all outdated entries from cache
     *
     * @param now an instant representing now
     */
    void removeOutdated(Instant now);

}
