package com.mercedesbenz.sechub.commons.core.cache;

import static org.assertj.core.api.Assertions.*;

import java.time.Duration;
import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.mercedesbenz.sechub.commons.core.security.CryptoAccess;

class CacheDataTest {

    @ParameterizedTest
    @ValueSource(strings = { "", "hello world", "😀", "{}" })
    void created_cache_data_without_crypto_access_provider_stores_value_not_sealed_and_value_can_be_retrieved(String origin) {
        /* execute */
        CacheData<String> data = new CacheData<String>(origin, Duration.ofMinutes(20), null, Instant.now());

        /* test */
        assertThat(data.isSealed()).isFalse();
        assertThat(data.getValue()).isEqualTo(origin);
    }

    @ParameterizedTest
    @ValueSource(strings = { "", "hello world", "😀", "{}" })
    void created_cache_data_with_crypto_access_provider_stores_value_sealed_and_value_can_be_retrieved(String origin) {
        /* execute */
        CacheData<String> data = new CacheData<String>(origin, Duration.ofMinutes(20), () -> CryptoAccess.CRYPTO_STRING, Instant.now());

        /* test */
        assertThat(data.isSealed()).isTrue();
        assertThat(data.getValue()).isEqualTo(origin);
    }

    @Test
    void given_duration_is_stored_inside() {
        /* execute */
        CacheData<String> data = new CacheData<String>("opaquekeyTestKey", Duration.ofMinutes(20), () -> CryptoAccess.CRYPTO_STRING, Instant.now());

        /* test */
        assertThat(data.getDuration()).isEqualTo(Duration.ofMinutes(20));
    }

    @Test
    void given_now_is_stored_as_createdAtinside() {
        /* execute */
        Instant now = Instant.now();
        CacheData<String> data = new CacheData<String>("opaquekeyTestKey", Duration.ofMinutes(20), () -> CryptoAccess.CRYPTO_STRING, now);

        /* test */
        assertThat(data.getCreatedAt()).isEqualTo(now);
    }

    @Test
    void null_value_throws_npe() {
        assertThatThrownBy(() -> new CacheData<String>(null, Duration.ofMinutes(20), null, Instant.now())).isInstanceOf(NullPointerException.class)
                .hasMessageContaining("'value' must not be null");
    }
}
