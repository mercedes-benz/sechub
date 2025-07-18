// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.security.clustercache;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.NullSource;
import org.mockito.ArgumentCaptor;
import org.springframework.dao.DuplicateKeyException;

import com.mercedesbenz.sechub.commons.core.cache.CacheData;
import com.mercedesbenz.sechub.commons.core.security.CryptoAccess;
import com.mercedesbenz.sechub.commons.core.security.CryptoAccessProvider;
import com.mercedesbenz.sechub.commons.model.JSONConverter;
import com.mercedesbenz.sechub.spring.security.OAuth2OpaqueTokenIntrospectionResponse;

class OAuth2OpaqueTokenClusterCachePersistenceTest {

    private static final String TEST_OPAQUE_TOKEN_ID = "opaque-token-id";
    private OAuth2OpaqueTokenClusterCachePersistence persistenceToTest;
    private OAuth2OpaqueTokenClusterCacheRepository repository;

    @BeforeEach
    void beforeEach() {
        repository = mock();

        CryptoAccessProvider<OAuth2OpaqueTokenIntrospectionResponse> mock = mock();
        when(mock.getCryptoAccess()).thenReturn(new CryptoAccess<>());
        persistenceToTest = new OAuth2OpaqueTokenClusterCachePersistence(repository, mock);
    }

    @ParameterizedTest
    @NullSource
    @ArgumentsSource(CryptoAccessArgumentsProvider.class)
    void simple_resilience_test__when_repository_save_fails_2_times_a_third_one_will_be_tried_successful_stores_data(
            CryptoAccessProvider<OAuth2OpaqueTokenIntrospectionResponse> cryptoProvider) {
        /* prepare */
        OAuth2OpaqueTokenIntrospectionResponse value = createFakedIDPOpaqueTokenResponse("user1");

        Instant now = Instant.now();
        Duration duration = Duration.ofSeconds(10).plusHours(1);
        CacheData<OAuth2OpaqueTokenIntrospectionResponse> data = new CacheData<OAuth2OpaqueTokenIntrospectionResponse>(value, duration, cryptoProvider, now);

        /* @formatter:off*/
        when(repository.findById(TEST_OPAQUE_TOKEN_ID)).thenReturn(Optional.empty()); // not found in database
        when(repository.save(any())).
            thenThrow(new DuplicateKeyException("failed-attempt-1")). // first save fails
            thenThrow(new RuntimeException("failed-attempt-2")). // second save fails
            thenReturn(null); // third save - okay (but we just return null for easier testing)
        /* @formatter:on*/

        /* execute */
        persistenceToTest.put(TEST_OPAQUE_TOKEN_ID, data);

        /* test */
        ArgumentCaptor<OAuth2OpaqueTokenClusterCache> entityCaptor = ArgumentCaptor.forClass(OAuth2OpaqueTokenClusterCache.class);
        verify(repository, times(3)).save(entityCaptor.capture());

        OAuth2OpaqueTokenClusterCache entityValue = entityCaptor.getValue();
        assertThat(entityValue.getOpaqueToken()).isEqualTo(TEST_OPAQUE_TOKEN_ID);
        assertThat(entityValue.getCreatedAt()).isEqualTo(now);
        assertThat(entityValue.getDuration()).isEqualTo(duration);
        assertThat(entityValue.getExpiresAt()).isEqualTo(now.plusSeconds(3610));
    }

    @ParameterizedTest
    @NullSource
    @ArgumentsSource(CryptoAccessArgumentsProvider.class)
    void put_creates_new_entry_in_databse_having_expected_content(CryptoAccessProvider<OAuth2OpaqueTokenIntrospectionResponse> cryptoProvider) {
        /* prepare */
        OAuth2OpaqueTokenIntrospectionResponse value = createFakedIDPOpaqueTokenResponse("user1");

        Instant now = Instant.now();
        Duration duration = Duration.ofSeconds(10).plusHours(1);
        CacheData<OAuth2OpaqueTokenIntrospectionResponse> data = new CacheData<OAuth2OpaqueTokenIntrospectionResponse>(value, duration, cryptoProvider, now);

        when(repository.findById(TEST_OPAQUE_TOKEN_ID)).thenReturn(Optional.empty()); // not found in database

        /* execute */
        persistenceToTest.put(TEST_OPAQUE_TOKEN_ID, data);

        /* test */
        ArgumentCaptor<OAuth2OpaqueTokenClusterCache> entityCaptor = ArgumentCaptor.forClass(OAuth2OpaqueTokenClusterCache.class);
        verify(repository).save(entityCaptor.capture());

        OAuth2OpaqueTokenClusterCache entityValue = entityCaptor.getValue();
        assertThat(entityValue.getOpaqueToken()).isEqualTo(TEST_OPAQUE_TOKEN_ID);
        assertThat(entityValue.getCreatedAt()).isEqualTo(now);
        assertThat(entityValue.getDuration()).isEqualTo(duration);
        assertThat(entityValue.getExpiresAt()).isEqualTo(now.plusSeconds(3610));
    }

    @ParameterizedTest
    @NullSource
    @ArgumentsSource(CryptoAccessArgumentsProvider.class)
    void put_updates_existing_entry_in_database_having_expected_content(CryptoAccessProvider<OAuth2OpaqueTokenIntrospectionResponse> cryptoProvider) {
        /* prepare */
        OAuth2OpaqueTokenIntrospectionResponse value = createFakedIDPOpaqueTokenResponse("user1");

        Instant now = Instant.now();
        Duration duration = Duration.ofSeconds(10).plusHours(1);
        CacheData<OAuth2OpaqueTokenIntrospectionResponse> data = new CacheData<OAuth2OpaqueTokenIntrospectionResponse>(value, duration, cryptoProvider, now);

        OAuth2OpaqueTokenClusterCache existingCacheEntry = new OAuth2OpaqueTokenClusterCache(TEST_OPAQUE_TOKEN_ID, "old", Duration.ofSeconds(2),
                Instant.ofEpochMilli(0));
        when(repository.findById(TEST_OPAQUE_TOKEN_ID)).thenReturn(Optional.of(existingCacheEntry));

        /* execute */
        persistenceToTest.put(TEST_OPAQUE_TOKEN_ID, data);

        /* test */
        ArgumentCaptor<OAuth2OpaqueTokenClusterCache> entityCaptor = ArgumentCaptor.forClass(OAuth2OpaqueTokenClusterCache.class);
        verify(repository).save(entityCaptor.capture());

        OAuth2OpaqueTokenClusterCache entityValue = entityCaptor.getValue();
        assertThat(entityValue.getOpaqueToken()).isEqualTo(TEST_OPAQUE_TOKEN_ID);
        assertThat(entityValue.getCreatedAt()).isEqualTo(now);
        assertThat(entityValue.getDuration()).isEqualTo(duration);
        assertThat(entityValue.getExpiresAt()).isEqualTo(now.plusSeconds(3610));

        assertThat(entityValue == existingCacheEntry); // given value was existing one -> update statement will be used by JPA

    }

    @Test
    void remove_calls_repository_delete_by_id() {

        /* execute */
        String opaqueToken = "test-" + System.nanoTime();
        persistenceToTest.remove(opaqueToken);

        /* test */
        verify(repository).deleteById(opaqueToken);
    }

    @Test
    void removeOutdated_calls_repository_removeOutdated() {

        /* execute */
        Instant now = mock();
        persistenceToTest.removeOutdated(now);

        /* test */
        verify(repository).removeOutdated(now);
    }

    @Test
    void get_calls_repository_find_by_id_not_found() {

        /* execute */
        String opaqueToken = "test-" + System.nanoTime();
        CacheData<OAuth2OpaqueTokenIntrospectionResponse> result = persistenceToTest.get(opaqueToken);

        /* test */
        verify(repository).findById(opaqueToken);
        assertThat(result).isNull();
    }

    @Test
    void get_calls_repository_find_by_id_found() {

        /* execute */
        String opaqueToken = "test-" + System.nanoTime();

        OAuth2OpaqueTokenIntrospectionResponse idpResponse = createFakedIDPOpaqueTokenResponse("userXYZ");
        String json = JSONConverter.get().toJSON(idpResponse);

        OAuth2OpaqueTokenClusterCache cache = new OAuth2OpaqueTokenClusterCache(opaqueToken, json, Duration.ofHours(1), Instant.now());
        Optional<OAuth2OpaqueTokenClusterCache> cacheValue = Optional.of(cache);
        when(repository.findById(opaqueToken)).thenReturn(cacheValue);

        /* execute */
        CacheData<OAuth2OpaqueTokenIntrospectionResponse> result = persistenceToTest.get(opaqueToken);

        /* test */
        verify(repository).findById(opaqueToken);
        assertThat(result).isNotNull();
    }

    private static class CryptoAccessArgumentsProvider implements ArgumentsProvider {
        CryptoAccess<OAuth2OpaqueTokenIntrospectionResponse> access = new CryptoAccess<OAuth2OpaqueTokenIntrospectionResponse>();

        CryptoAccessProvider<OAuth2OpaqueTokenIntrospectionResponse> provider = new CryptoAccessProvider<OAuth2OpaqueTokenIntrospectionResponse>() {

            @Override
            public CryptoAccess<OAuth2OpaqueTokenIntrospectionResponse> getCryptoAccess() {
                return access;
            }
        };

        /* @formatter:off */
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext) throws Exception {
            return Stream.of(
		      Arguments.of(provider));
        }
        /* @formatter:on*/
    }

    private static OAuth2OpaqueTokenIntrospectionResponse createFakedIDPOpaqueTokenResponse(String username) {
        return new OAuth2OpaqueTokenIntrospectionResponse(true, "scope", "client-id", "client-type", username, "token-type",
                Instant.now().plusSeconds(60).getEpochSecond(), "subject", "aud", "group-type");
    }

}
