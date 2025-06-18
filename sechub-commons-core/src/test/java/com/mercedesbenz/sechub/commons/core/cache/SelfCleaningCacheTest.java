// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.core.cache;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import com.mercedesbenz.sechub.commons.core.security.CryptoAccess;
import com.mercedesbenz.sechub.commons.core.security.CryptoAccessProvider;
import com.mercedesbenz.sechub.commons.core.shutdown.ApplicationShutdownHandler;

class SelfCleaningCacheTest {

    private static final String TEST_CACHE_NAME = "cache-name";
    private static final Duration TEST_CACHE_CLEAR_JOB_PERIOD = Duration.ofMinutes(1);
    private static final ScheduledExecutorService scheduledExecutorService = mock();
    @SuppressWarnings("rawtypes")
    private static final ScheduledFuture scheduledFuture = mock();
    private static final ApplicationShutdownHandler applicationShutdownHandler = mock();
    private static final CryptoAccessProvider<String> cryptoAccessProvider = mock();
    private static final TestCachePersistence testCachePersistence = new TestCachePersistence();

    @SuppressWarnings("unchecked")
    @BeforeEach
    void beforeEach() {
        reset(scheduledExecutorService, applicationShutdownHandler, cryptoAccessProvider);
        when(scheduledExecutorService.scheduleAtFixedRate(any(), anyLong(), anyLong(), any())).thenReturn(scheduledFuture);
        when(cryptoAccessProvider.getCryptoAccess()).thenReturn(CryptoAccess.CRYPTO_STRING);
    }

    @Test
    void construct_with_custom_cache_clear_job_period() {
        /* prepare */
        Duration cacheClearJobPeriod = Duration.ofSeconds(1);
        SelfCleaningCache<String> inMemoryCacheToTest = new SelfCleaningCache<>(TEST_CACHE_NAME, testCachePersistence, cacheClearJobPeriod,
                scheduledExecutorService, applicationShutdownHandler, cryptoAccessProvider);

        /* test */
        assertThat(inMemoryCacheToTest.getCacheClearJobPeriod()).isEqualTo(cacheClearJobPeriod);
        verify(applicationShutdownHandler).register(inMemoryCacheToTest);
    }

    @Test
    void construct_with_null_scheduled_executor_service_throws_exception() {
        /* test */
        /* @formatter:off */
        assertThatThrownBy(() -> new SelfCleaningCache<>(TEST_CACHE_NAME,testCachePersistence,TEST_CACHE_CLEAR_JOB_PERIOD, null, applicationShutdownHandler, cryptoAccessProvider ))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Property 'scheduledExecutorService' must not be null");
        /* @formatter:on */
    }

    @Test
    void construct_with_null_cache_clear_job_period_throws_exception() {
        /* test */
        /* @formatter:off */
        assertThatThrownBy(() -> new SelfCleaningCache<>(TEST_CACHE_NAME,testCachePersistence,null, scheduledExecutorService, applicationShutdownHandler, cryptoAccessProvider))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Property 'cacheClearJobPeriod' must not be null");
        /* @formatter:on */
    }

    @Test
    void construct_with_null_application_shutdown_handler_throws_exception() {
        /* test */
        /* @formatter:off */
        assertThatThrownBy(() -> new SelfCleaningCache<>(TEST_CACHE_NAME,testCachePersistence,TEST_CACHE_CLEAR_JOB_PERIOD, scheduledExecutorService, null, cryptoAccessProvider))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Property 'applicationShutdownHandler' must not be null");
        /* @formatter:on */
    }

    @Test
    void remove_key_removes_value_from_cache() {
        /* prepare */
        SelfCleaningCache<String> inMemoryCacheToTest = new SelfCleaningCache<>(TEST_CACHE_NAME, testCachePersistence, TEST_CACHE_CLEAR_JOB_PERIOD,
                scheduledExecutorService, applicationShutdownHandler, cryptoAccessProvider);

        inMemoryCacheToTest.put("key1", "value1", Duration.ofSeconds(10));

        /* check-precondition */
        assertThat(inMemoryCacheToTest.get("key1")).isNotEmpty().isEqualTo(Optional.of("value1"));

        /* execute */
        inMemoryCacheToTest.remove("key1");

        /* test */
        assertThat(inMemoryCacheToTest.get("key1")).isEmpty();
    }

    @Test
    void get_returns_empty_optional_when_cache_does_not_contain_key() {
        /* prepare */
        SelfCleaningCache<String> inMemoryCacheToTest = new SelfCleaningCache<>(TEST_CACHE_NAME, testCachePersistence, TEST_CACHE_CLEAR_JOB_PERIOD,
                scheduledExecutorService, applicationShutdownHandler, cryptoAccessProvider);

        /* execute + test */
        assertThat(inMemoryCacheToTest.get("not-existing-key")).isEmpty();
    }

    @Test
    void get_throws_null_pointer_exception_when_key_is_null() {
        /* prepare */
        SelfCleaningCache<String> inMemoryCacheToTest = new SelfCleaningCache<>(TEST_CACHE_NAME, testCachePersistence, TEST_CACHE_CLEAR_JOB_PERIOD,
                scheduledExecutorService, applicationShutdownHandler, cryptoAccessProvider);

        /* execute + test */
        assertThatThrownBy(() -> inMemoryCacheToTest.get(null)).isInstanceOf(NullPointerException.class).hasMessage("Argument 'key' must not be null");
    }

    @Test
    void remove_throws_null_pointer_exception_when_key_is_null() {
        /* prepare */
        SelfCleaningCache<String> inMemoryCacheToTest = new SelfCleaningCache<>(TEST_CACHE_NAME, testCachePersistence, TEST_CACHE_CLEAR_JOB_PERIOD,
                scheduledExecutorService, applicationShutdownHandler, cryptoAccessProvider);

        /* execute + test */
        assertThatThrownBy(() -> inMemoryCacheToTest.remove(null)).isInstanceOf(NullPointerException.class).hasMessageContaining("key");
    }

    @Test
    void get_returns_value_when_cache_contains_key() {
        /* prepare */
        SelfCleaningCache<String> inMemoryCacheToTest = new SelfCleaningCache<>(TEST_CACHE_NAME, testCachePersistence, TEST_CACHE_CLEAR_JOB_PERIOD,
                scheduledExecutorService, applicationShutdownHandler, cryptoAccessProvider);
        String key = UUID.randomUUID().toString();
        String value = "value";
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
        SelfCleaningCache<String> inMemoryCacheToTest = new SelfCleaningCache<>(TEST_CACHE_NAME, testCachePersistence, TEST_CACHE_CLEAR_JOB_PERIOD,
                scheduledExecutorService, applicationShutdownHandler, cryptoAccessProvider);
        String key = UUID.randomUUID().toString();
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
    void put_throws_null_pointer_exception_when_key_is_null() {
        /* test */
        SelfCleaningCache<String> inMemoryCacheToTest = new SelfCleaningCache<>(TEST_CACHE_NAME, testCachePersistence, TEST_CACHE_CLEAR_JOB_PERIOD,
                scheduledExecutorService, applicationShutdownHandler, cryptoAccessProvider);

        /* test */
        /* @formatter:off */
        assertThatThrownBy(() -> inMemoryCacheToTest.put(null, "value", Duration.ofSeconds(1)))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Argument 'key' must not be null");
        /* @formatter:on */
    }

    @Test
    void put_throws_null_pointer_exception_when_value_is_null() {
        /* execute */
        SelfCleaningCache<String> inMemoryCacheToTest = new SelfCleaningCache<>(TEST_CACHE_NAME, testCachePersistence, TEST_CACHE_CLEAR_JOB_PERIOD,
                scheduledExecutorService, applicationShutdownHandler, cryptoAccessProvider);

        /* test */
        /* @formatter:off */
        assertThatThrownBy(() -> inMemoryCacheToTest.put(UUID.randomUUID().toString(), null, Duration.ofSeconds(1)))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Property 'value' must not be null");
        /* @formatter:on */
    }

    @Test
    void put_throws_null_pointer_exception_when_duration_is_null() {
        /* execute */
        SelfCleaningCache<String> inMemoryCacheToTest = new SelfCleaningCache<>(TEST_CACHE_NAME, testCachePersistence, TEST_CACHE_CLEAR_JOB_PERIOD,
                scheduledExecutorService, applicationShutdownHandler, cryptoAccessProvider);

        /* test */
        /* @formatter:off */
        assertThatThrownBy(() -> inMemoryCacheToTest.put(UUID.randomUUID().toString(), "value", null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Property 'duration' must not be null");
        /* @formatter:on */
    }

    @Test
    void clearCache_removes_cache_data_after_is_has_expired() {
        /* prepare */
        Duration cacheClearJobPeriod = Duration.ofMillis(10);
        /* the cache clear job will run right away (point in time = 0s) */
        SelfCleaningCache<String> inMemoryCacheToTest = new SelfCleaningCache<>(TEST_CACHE_NAME, testCachePersistence, cacheClearJobPeriod,
                Executors.newSingleThreadScheduledExecutor(), applicationShutdownHandler, cryptoAccessProvider);
        String key = UUID.randomUUID().toString();
        String value = "value";
        Duration cacheDataDuration = Duration.ofMillis(100);
        inMemoryCacheToTest.put(key, value, cacheDataDuration);
        Duration timeout = Duration.ofMillis(300);

        /* execute & test */
        assertThat(inMemoryCacheToTest.get(key)).isPresent();

        /* @formatter:off */
        Awaitility.await()
                .pollInSameThread()
                /*
                    Given a cache data duration of 100 milliseconds, the cache data should expire shortly after 100 milliseconds
                    including a small buffer for program execution.
                 */
                .pollDelay(cacheDataDuration)
                .pollInterval(Duration.ofMillis(10))
                .atMost(timeout)
                .untilAsserted(() -> assertThat(inMemoryCacheToTest.get(key)).isEmpty());
        /* @formatter:on */
    }

    @Test
    void clearCache_does_not_remove_cache_data_until_it_has_expired() throws InterruptedException {
        /* prepare */
        Duration cacheClearJobPeriod = Duration.ofMillis(10);
        SelfCleaningCache<String> inMemoryCacheToTest = new SelfCleaningCache<>(TEST_CACHE_NAME, testCachePersistence, cacheClearJobPeriod,
                Executors.newSingleThreadScheduledExecutor(), applicationShutdownHandler, cryptoAccessProvider);
        String key = UUID.randomUUID().toString();
        String value = "value";
        /* the cache is valid for 200 millis */
        Duration cacheDataDuration = Duration.ofMillis(200);
        inMemoryCacheToTest.put(key, value, cacheDataDuration);

        /* execute & test */
        assertThat(inMemoryCacheToTest.get(key)).isPresent();

        Duration programExecutionBuffer = Duration.ofMillis(30);
        Duration pollInterval = Duration.ofMillis(10);
        Duration timeout = Duration.ofMillis(500);

        /* @formatter:off */

        /* assert that the cache data is still present right before it's expiration */
        Thread.sleep(cacheDataDuration.toMillis() - programExecutionBuffer.toMillis());
        assertThat(inMemoryCacheToTest.get(key)).isPresent();

        /* after the cache data has expired, it should be removed */
        Awaitility.await()
                .pollInSameThread()
                .pollInterval(pollInterval)
                .atMost(timeout)
                .untilAsserted(() -> assertThat(inMemoryCacheToTest.get(key)).isEmpty());

        /* @formatter:on */
    }

    @Test
    void close_cancels_cache_clear_job_and_shuts_down_scheduled_executor_service() {
        /* prepare */
        SelfCleaningCache<String> inMemoryCacheToTest = new SelfCleaningCache<>(TEST_CACHE_NAME, testCachePersistence, TEST_CACHE_CLEAR_JOB_PERIOD,
                scheduledExecutorService, applicationShutdownHandler, cryptoAccessProvider);

        /* execute */
        inMemoryCacheToTest.onShutdown();

        /* test */
        InOrder inOrder = inOrder(scheduledFuture, scheduledExecutorService);
        inOrder.verify(scheduledFuture).cancel(true);
        inOrder.verify(scheduledExecutorService).shutdownNow();
    }

    private static class TestCachePersistence implements CachePersistence<String> {

        private final Map<String, CacheData<String>> cacheMap = new ConcurrentHashMap<>();

        @Override
        public void remove(String key) {
            cacheMap.remove(key);
        }

        @Override
        public void put(String key, CacheData<String> cacheData) {
            cacheMap.put(key, cacheData);
        }

        @Override
        public CacheData<String> get(String key) {
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

}
