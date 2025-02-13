// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.core.cache;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.time.Duration;
import java.util.Optional;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.commons.core.shutdown.ApplicationShutdownHandler;

class InMemoryCacheTest {

    private static final Duration CACHE_CLEAR_JOB_PERIOD_DEFAULT = Duration.ofMinutes(1);
    private static final ApplicationShutdownHandler applicationShutdownHandler = mock();

    @Test
    void construct_with_default_cache_clear_job_period() {
        /* test */
        InMemoryCache<String> inMemoryCacheToTest = new InMemoryCache<>(applicationShutdownHandler);

        /* test */
        assertThat(inMemoryCacheToTest.getCacheClearJobPeriod()).isEqualTo(CACHE_CLEAR_JOB_PERIOD_DEFAULT);
    }

    @Test
    void construct_with_custom_cache_clear_job_period() {
        /* prepare */
        Duration cacheClearJobPeriod = Duration.ofSeconds(1);

        /* test */
        InMemoryCache<String> inMemoryCacheToTest = new InMemoryCache<>(cacheClearJobPeriod, applicationShutdownHandler);

        /* test */
        assertThat(inMemoryCacheToTest.getCacheClearJobPeriod()).isEqualTo(cacheClearJobPeriod);
    }

    @Test
    void get_returns_empty_optional_when_cache_does_not_contain_key() {
        /* test */
        InMemoryCache<String> inMemoryCacheToTest = new InMemoryCache<>(applicationShutdownHandler);

        /* test */
        assertThat(inMemoryCacheToTest.get("key")).isEmpty();
    }

    @Test
    void get_returns_value_when_cache_contains_key() {
        /* prepare */
        InMemoryCache<String> inMemoryCacheToTest = new InMemoryCache<>(applicationShutdownHandler);
        String key = "key";
        String value = "result";
        Duration duration = Duration.ofSeconds(1);
        inMemoryCacheToTest.put(key, value, duration);

        /* execute */
        Optional<String> result = inMemoryCacheToTest.get(key);

        /* test */
        assertThat(result).isPresent().hasValue(value);
    }

    @Test
    void put_overrides_existing_cache_data_with_same_key() {
        /* prepare */
        InMemoryCache<String> inMemoryCacheToTest = new InMemoryCache<>(applicationShutdownHandler);
        String key = "key";
        String oldValue = "old value";
        Duration duration = Duration.ofSeconds(1);
        inMemoryCacheToTest.put(key, oldValue, duration);
        Optional<String> oldResult = inMemoryCacheToTest.get(key);

        /* execute */
        String newValue = "new value";
        inMemoryCacheToTest.put(key, newValue, duration);
        Optional<String> newResult = inMemoryCacheToTest.get(key);

        /* test */
        assertThat(oldResult).isPresent().hasValue(oldValue);
        assertThat(newResult).isPresent().hasValue(newValue);
    }

    @Test
    void clearCache_removes_cache_data_after_is_has_expired() {
        /* prepare */
        Duration cacheClearJobPeriod = Duration.ofSeconds(1);
        /* the cache clear job will run right away (point in time = 0s) */
        InMemoryCache<String> inMemoryCacheToTest = new InMemoryCache<>(cacheClearJobPeriod, applicationShutdownHandler);
        String key = "key";
        String value = "value";
        Duration cacheDataDuration = Duration.ofSeconds(1);
        inMemoryCacheToTest.put(key, value, cacheDataDuration);

        /* execute & test */
        /* @formatter:off */
        assertThat(inMemoryCacheToTest.get(key)).isPresent();
        Awaitility.await()
                /*
                    Given a cache data duration of 1 second, the cache data should expire shortly after 2 seconds
                    including a small buffer for program execution.

                    Explanation:
                        - The first run (point in time = 0s) will not remove the cache data, as it occurs before the cache object is put.
                        - The second run (point in time = 1s) will not remove the cache data, as the expiration time slightly exceeds this point in time.
                        - The third run (point in time = 2s) will remove the expired cache data, as the expiration threshold has passed.

                 */
                .atMost(Duration.ofMillis(2200))
                .pollInterval(Duration.ofMillis(100))
                .untilAsserted(() -> assertThat(inMemoryCacheToTest.get(key)).isEmpty());
        /* @formatter:on */
    }

    @Test
    void clearCache_does_not_remove_cache_data_until_it_has_expired() {
        /* prepare */
        InMemoryCache<String> inMemoryCacheToTest = new InMemoryCache<>(applicationShutdownHandler);
        String key = "key";
        String value = "value";
        /* the cache is valid for 2 seconds */
        Duration cacheDataDuration = Duration.ofSeconds(2);
        inMemoryCacheToTest.put(key, value, cacheDataDuration);

        /* execute & test */
        /* @formatter:off */
        assertThat(inMemoryCacheToTest.get(key)).isPresent();
        Awaitility.await()
                /* Ensure the cache remains valid for it's duration */
                .pollDelay(cacheDataDuration)
                .pollInterval(Duration.ofMillis(10))
                .atMost(Duration.ofSeconds(3))
                /*
                    Even with a very often running cache clear job period there should always be a small window of time when the
                    cache data is still valid after it's duration, because the clear job has a delay.
                */
                .untilAsserted(() -> assertThat(inMemoryCacheToTest.get(key)).isPresent());
        /* @formatter:on */
    }

}
