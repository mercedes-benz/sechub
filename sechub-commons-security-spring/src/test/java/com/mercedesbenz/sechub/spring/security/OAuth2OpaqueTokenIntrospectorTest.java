// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.spring.security;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedConstruction;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.core.OAuth2TokenIntrospectionClaimNames;
import org.springframework.security.oauth2.server.resource.introspection.BadOpaqueTokenException;

import com.mercedesbenz.sechub.commons.core.cache.CacheData;
import com.mercedesbenz.sechub.commons.core.cache.CachePersistence;
import com.mercedesbenz.sechub.commons.core.cache.InMemoryCachePersistence;
import com.mercedesbenz.sechub.commons.core.cache.SelfCleaningCache;
import com.mercedesbenz.sechub.commons.core.security.CryptoAccess;
import com.mercedesbenz.sechub.commons.core.security.CryptoAccessProvider;
import com.mercedesbenz.sechub.commons.core.shutdown.ApplicationShutdownHandler;
import com.mercedesbenz.sechub.commons.core.shutdown.ShutdownListener;

class OAuth2OpaqueTokenIntrospectorTest {

    private RestTemplateOAuth2OpaqueTokenIDPIntrospectionResponseFetcher fetcher;
    private UserDetailsService userDetailsService;
    private TestApplicationShutdownHandler applicationTestShutdownHandler;

    private OAuth2TokenExpirationCalculator expirationCalculator;
    private static final Duration DEFAULT_TOKEN_EXPIRES_IN = Duration.ofDays(1);
    private static final Duration MAX_CACHE_DURATION = Duration.ofDays(30);
    private static final Duration PRE_CACHE_DURATION = Duration.ofSeconds(30);

    private static final Duration IN_MEMORY_CACHE_CLEAR_PERIOD = Duration.ofSeconds(10);
    private static final Duration CLUSTER_CACHE_CLEAR_PERIOD = Duration.ofSeconds(60 * 5);

    private static final String OPAQUE_TOKEN = "opaque-token";
    private static final String SUBJECT = "sub";

    @BeforeEach
    void beforeEach() {
        applicationTestShutdownHandler = new TestApplicationShutdownHandler();

        userDetailsService = mock();
        fetcher = mock();
        expirationCalculator = mock();

        mockUserDetailsService();
    }

    @AfterEach
    void afterEach() {
        /*
         * simulate application shutdown - this will end the self cleaning cache threads
         */
        applicationTestShutdownHandler.simulateShutdown();
    }

    /* @formatter:off */
    @ParameterizedTest
    @ArgumentsSource(OAuth2OpaqueTokenIntrospectorSingleNullArgumentProvider.class)
    void construct_o_auth_2_opaque_token_introspector_with_null_argument_fails(String variant,
                                                                               RestTemplateOAuth2OpaqueTokenIDPIntrospectionResponseFetcher fetcher,
                                                                               Duration defaultTokenExpiresIn,
                                                                               Duration maxCacheDuration,
                                                                               UserDetailsService userDetailsService,
                                                                               String errMsg) {
        assertThatThrownBy(() -> OAuth2OpaqueTokenIntrospector.builder().
                    setIntrospectionResponseFetcher(fetcher).
                    setDefaultTokenExpiresIn(defaultTokenExpiresIn).
                    setMaxCacheDuration(maxCacheDuration).
                    setUserDetailsService(userDetailsService).
                    setApplicationShutdownHandler(applicationTestShutdownHandler).
                    setExpirationCalculator(expirationCalculator).
                    setPreCacheDuration(Duration.ofSeconds(1)).
                    setInMemoryCacheClearPeriod(Duration.ofSeconds(3)).
                    setClusterCacheClearPeriod(Duration.ofSeconds(10))
                .build())
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining(errMsg);
    }
    /* @formatter:on */

    @ParameterizedTest
    @NullAndEmptySource
    void introspect_with_null_or_empty_opaque_token_fails(String opaqueToken) {
        /* prepare */
        OAuth2OpaqueTokenIntrospector introspectorToTest = createOAuth2OpaqueTokenIntrospectorNoMinimumTokenValidityAndNoClusterCachePersistence();

        /* @formatter:off */
        assertThatThrownBy(() -> introspectorToTest.introspect(opaqueToken))
                .isInstanceOf(BadOpaqueTokenException.class)
                .hasMessageContaining("Token is null or empty");
        /* @formatter:on */
    }

    @Test
    void introspect_with_no_cached_opaque_calls_introspection_endpoint() {
        /* prepare */
        OAuth2OpaqueTokenIntrospector introspectorToTest = createOAuth2OpaqueTokenIntrospectorNoMinimumTokenValidityAndNoClusterCachePersistence();

        OAuth2OpaqueTokenIntrospectionResponse introspectionResponse = createIntrospectionResponse(Boolean.TRUE);
        mockIntrospectionResponse(introspectionResponse);
        verifyNoInteractions(fetcher);

        /* execute */
        introspectorToTest.introspect(OPAQUE_TOKEN);

        /* test */
        verify(fetcher).fetchOpaqueTokenIntrospectionFromIDP(OPAQUE_TOKEN);
    }

    @Test
    void introspect_with_cached_opaque_token_returns_cached_opaque_token_introspection_response() {
        /* prepare */
        OAuth2OpaqueTokenIntrospector introspectorToTest = createOAuth2OpaqueTokenIntrospectorNoMinimumTokenValidityAndNoClusterCachePersistence();

        OAuth2OpaqueTokenIntrospectionResponse introspectionResponse = createIntrospectionResponse(Boolean.TRUE);
        mockIntrospectionResponse(introspectionResponse);
        /* first introspection should cache the result */
        introspectorToTest.introspect(OPAQUE_TOKEN);
        verify(fetcher).fetchOpaqueTokenIntrospectionFromIDP(OPAQUE_TOKEN);
        reset(fetcher);
        verifyNoInteractions(fetcher);

        /* execute */
        OAuth2AuthenticatedPrincipal result = introspectorToTest.introspect(OPAQUE_TOKEN);

        /* test */
        assertThat(result).isNotNull(); // a result is returned
        assertThat(result.getName()).isEqualTo(introspectionResponse.getUsername());
        verifyNoInteractions(fetcher); // fetcher may not be called... the cache must be used
    }

    @Test
    void introspect_with_null_response_fails() {
        /* prepare */
        OAuth2OpaqueTokenIntrospector introspectorToTest = createOAuth2OpaqueTokenIntrospectorNoMinimumTokenValidityAndNoClusterCachePersistence();

        mockIntrospectionResponse(null);

        /* execute & assert */
        /* @formatter:off */
        assertThatThrownBy(() -> introspectorToTest.introspect(OPAQUE_TOKEN))
                .isInstanceOf(BadOpaqueTokenException.class)
                .hasMessageContaining("Token introspection response from IDP is null");
        /* @formatter:on */
    }

    @Test
    void introspect_with_inactive_token_fails() {
        /* prepare */
        OAuth2OpaqueTokenIntrospector introspectorToTest = createOAuth2OpaqueTokenIntrospectorNoMinimumTokenValidityAndNoClusterCachePersistence();

        OAuth2OpaqueTokenIntrospectionResponse introspectionResponse = createIntrospectionResponse(Boolean.FALSE);
        mockIntrospectionResponse(introspectionResponse);

        /* execute & assert */
        /* @formatter:off */
        assertThatThrownBy(() -> introspectorToTest.introspect(OPAQUE_TOKEN))
                .isInstanceOf(BadOpaqueTokenException.class)
                .hasMessageContaining("Token is not active");
        /* @formatter:on */
    }

    @Test
    void introspect_with_valid_token_succeeds() {
        /* prepare */
        OAuth2OpaqueTokenIntrospector introspectorToTest = createOAuth2OpaqueTokenIntrospectorNoMinimumTokenValidityAndNoClusterCachePersistence();

        OAuth2OpaqueTokenIntrospectionResponse introspectionResponse = createIntrospectionResponse(Boolean.TRUE);
        mockIntrospectionResponse(introspectionResponse);

        /* execute */
        OAuth2AuthenticatedPrincipal principal = introspectorToTest.introspect(OPAQUE_TOKEN);

        /* assert */
        assertThat(principal.getName()).isEqualTo(SUBJECT);
        Map<String, Object> attributes = principal.getAttributes();
        assertThat(attributes.get(OAuth2TokenIntrospectionClaimNames.ACTIVE)).isEqualTo(introspectionResponse.isActive());
        assertThat(attributes.get(OAuth2TokenIntrospectionClaimNames.SCOPE)).isEqualTo(introspectionResponse.getScope());
        assertThat(attributes.get(OAuth2TokenIntrospectionClaimNames.CLIENT_ID)).isEqualTo(introspectionResponse.getClientId());
        assertThat(attributes.get(OAuth2TokenIntrospectionClaimNames.USERNAME)).isEqualTo(introspectionResponse.getUsername());
        assertThat(attributes.get(OAuth2TokenIntrospectionClaimNames.TOKEN_TYPE)).isEqualTo(introspectionResponse.getTokenType());
        assertThat(attributes.get(OAuth2TokenIntrospectionClaimNames.IAT)).isEqualTo(introspectionResponse.getIssuedAt());
        assertThat(attributes.get(OAuth2TokenIntrospectionClaimNames.EXP)).isEqualTo(introspectionResponse.getExpiresAtAsInstant());
        assertThat(attributes.get(OAuth2TokenIntrospectionClaimNames.SUB)).isEqualTo(introspectionResponse.getSubject());
        assertThat(attributes.get(OAuth2TokenIntrospectionClaimNames.AUD)).isEqualTo(introspectionResponse.getAudience());
    }

    @Test
    @SuppressWarnings({ "rawtypes", "unchecked" })
    void introspect_with_token_expires_at_null_uses_default_token_expires_at_for_cache() {
        try (MockedConstruction<SelfCleaningCache> cacheConstruction = mockConstruction(SelfCleaningCache.class, (mock, context) -> {
        })) {

            /* prepare */
            OAuth2OpaqueTokenIntrospector introspectorToTest = createOAuth2OpaqueTokenIntrospectorNoMinimumTokenValidityAndNoClusterCachePersistence();
            Instant testStartTime = Instant.now();
            OAuth2OpaqueTokenIntrospectionResponse introspectionResponse = createIntrospectionResponse(Boolean.TRUE, null); // null-> no expiesAt defined by IDP
            mockIntrospectionResponse(introspectionResponse);

            /* execute */
            OAuth2AuthenticatedPrincipal principal = introspectorToTest.introspect(OPAQUE_TOKEN);

            /* assert */
            SelfCleaningCache cache = cacheConstruction.constructed().get(0);
            /* @formatter:off */
            verify(cache).put(
                    eq(OPAQUE_TOKEN),
                    eq(introspectionResponse),
                    assertArg(arg -> assertThat(truncate(arg)).isEqualTo(truncate(DEFAULT_TOKEN_EXPIRES_IN)))
            );
            /* @formatter:on */
            Map<String, Object> attributes = principal.getAttributes();
            Instant expiresAtActual = truncate((Instant) attributes.get(OAuth2TokenIntrospectionClaimNames.EXP));
            Instant calculated = testStartTime.plus(DEFAULT_TOKEN_EXPIRES_IN);
            Instant expiresAtExpected = truncate(calculated);
            assertThat(expiresAtActual).isEqualTo(expiresAtExpected);

        }
    }

    @Test
    void introspect_with_token_expires_at_null_uses_default_token_to_calculate_and_set_response_fallback_expiration() {
        /* prepare */
        OAuth2OpaqueTokenIntrospector introspectorToTest = createOAuth2OpaqueTokenIntrospectorNoMinimumTokenValidityAndNoClusterCachePersistence();
        OAuth2OpaqueTokenIntrospectionResponse introspectionResponse = createIntrospectionResponse(Boolean.TRUE, null);
        mockIntrospectionResponse(introspectionResponse);

        /* execute */
        introspectorToTest.introspect(OPAQUE_TOKEN);

        /* test */
        ArgumentCaptor<OAuth2OpaqueTokenIntrospectionResponse> responseCaptor = ArgumentCaptor.forClass(OAuth2OpaqueTokenIntrospectionResponse.class);
        ArgumentCaptor<Instant> nowCaptor = ArgumentCaptor.forClass(Instant.class);

        verify(expirationCalculator).isExpired(responseCaptor.capture(), nowCaptor.capture());

        Instant now = nowCaptor.getValue();
        Instant calculated = now.plus(DEFAULT_TOKEN_EXPIRES_IN).truncatedTo(ChronoUnit.SECONDS);
        ;

        OAuth2OpaqueTokenIntrospectionResponse response = responseCaptor.getValue();

        // check expiration calculator got same value for calculation as response value
        assertThat(introspectionResponse.getExpiresAtAsInstant()).isEqualTo(response.getExpiresAtAsInstant());

        // check that response calculation used the calculated value
        assertThat(response.getExpiresAtAsInstant()).isEqualTo(calculated);

    }

    @Test
    void when_calculator_says_expired_than_a_introspect_will_throw_bad_opaque_token_excpetion() {
        /* prepare */
        OAuth2OpaqueTokenIntrospector introspectorToTest = createOAuth2OpaqueTokenIntrospectorNoMinimumTokenValidityAndNoClusterCachePersistence();

        OAuth2OpaqueTokenIntrospectionResponse introspectionResponse = createIntrospectionResponse(Boolean.TRUE, Long.valueOf(4711));
        mockIntrospectionResponse(introspectionResponse);

        when(expirationCalculator.isExpired(eq(introspectionResponse), any())).thenReturn(true); // this response is always expired for this test

        /* execute */
        assertThatThrownBy(() -> introspectorToTest.introspect(OPAQUE_TOKEN)).isInstanceOf(BadOpaqueTokenException.class).hasMessageContaining("expired");

    }

    @Test
    @SuppressWarnings({ "rawtypes", "unchecked" })
    void introspect_with_token_expires_at_not_null() {
        try (MockedConstruction<SelfCleaningCache> cacheConstruction = mockConstruction(SelfCleaningCache.class, (mock, context) -> {
        })) {
            /* prepare */
            OAuth2OpaqueTokenIntrospector introspectorToTest = createOAuth2OpaqueTokenIntrospectorNoMinimumTokenValidityAndNoClusterCachePersistence();
            Instant testStartTime = Instant.now().truncatedTo(ChronoUnit.SECONDS);
            Duration expiresIn = Duration.ofDays(15);
            Instant expiresAt = testStartTime.plus(expiresIn);
            OAuth2OpaqueTokenIntrospectionResponse introspectionResponse = createIntrospectionResponse(Boolean.TRUE, expiresAt.getEpochSecond());
            mockIntrospectionResponse(introspectionResponse);

            /* execute */
            OAuth2AuthenticatedPrincipal principal = introspectorToTest.introspect(OPAQUE_TOKEN);

            /* assert */
            SelfCleaningCache cache = cacheConstruction.constructed().get(0);
            /* @formatter:off */
            verify(cache).put(
                    eq(OPAQUE_TOKEN),
                    eq(introspectionResponse),
                    assertArg(arg -> {
                        assertThat(truncate(arg)).isEqualTo(truncate(expiresIn));
                    })
            );
            /* @formatter:on */
            Map<String, Object> attributes = principal.getAttributes();
            Instant expiresAtActual = truncate((Instant) attributes.get(OAuth2TokenIntrospectionClaimNames.EXP));
            Instant expiresAtExpected = truncate(expiresAt);
            assertThat(expiresAtActual).isEqualTo(expiresAtExpected);
        }
    }

    @Test
    @SuppressWarnings({ "rawtypes", "unchecked" })
    void introspect_with_expires_at_exceeding_max_cache_duration_uses_max_cache_duration() {
        try (MockedConstruction<SelfCleaningCache> cacheConstruction = mockConstruction(SelfCleaningCache.class, (mock, context) -> {
        })) {
            /* prepare */
            OAuth2OpaqueTokenIntrospector introspectorToTest = createOAuth2OpaqueTokenIntrospectorNoMinimumTokenValidityAndNoClusterCachePersistence();
            Instant expiresAt = Instant.MAX;
            OAuth2OpaqueTokenIntrospectionResponse introspectionResponse = createIntrospectionResponse(Boolean.TRUE, expiresAt.getEpochSecond());
            mockIntrospectionResponse(introspectionResponse);

            /* execute */
            introspectorToTest.introspect(OPAQUE_TOKEN);

            /* assert */
            SelfCleaningCache cache = cacheConstruction.constructed().get(0);
            /* @formatter:off */
            verify(cache).put(
                    eq(OPAQUE_TOKEN),
                    eq(introspectionResponse),
                    assertArg(arg -> assertThat(arg).isEqualTo(MAX_CACHE_DURATION))
            );
            /* @formatter:on */
        }
    }

    @ParameterizedTest
    @SuppressWarnings("unchecked")
    @ArgumentsSource(CryptoAccessArgumentsProvider.class)
    @NullSource
    void introspect_with_cluster_cache__in_memory_cache_has_no_entry_and_cluster_cache__has_no_entry__calls_idp_and_sets_cache_values(
            CryptoAccessProvider<OAuth2OpaqueTokenIntrospectionResponse> cryptoAccessProvider) {
        /* prepare */
        Long expiresAt = Instant.now().plus(2, ChronoUnit.DAYS).getEpochSecond();
        CachePersistence<OAuth2OpaqueTokenIntrospectionResponse> clusterCachePersistence = mock();

        RestTemplateOAuth2OpaqueTokenIDPIntrospectionResponseFetcher fetcher = mock();
        InMemoryCachePersistence<OAuth2OpaqueTokenIntrospectionResponse> inMemoryCachePersistence = mock();

        OAuth2OpaqueTokenIntrospectionResponse introspectionResponse = createIntrospectionResponse(Boolean.TRUE, expiresAt);

        when(inMemoryCachePersistence.get(OPAQUE_TOKEN)).thenReturn(null);
        when(clusterCachePersistence.get(OPAQUE_TOKEN)).thenReturn(null);
        when(fetcher.fetchOpaqueTokenIntrospectionFromIDP(OPAQUE_TOKEN)).thenReturn(introspectionResponse);

        OAuth2OpaqueTokenIntrospector introspectorToTest = createOAuth2OpaqueTokenIntrospectorNoMinimumTokenValidityButClusterCachePersistence(
                inMemoryCachePersistence, clusterCachePersistence, fetcher, cryptoAccessProvider);

        /* execute 1 */
        introspectorToTest.introspect(OPAQUE_TOKEN);

        /* test */
        // IDP must be called
        verify(fetcher, times(1)).fetchOpaqueTokenIntrospectionFromIDP(OPAQUE_TOKEN);

        // cluster cache must be set with IDP value
        ArgumentCaptor<CacheData<OAuth2OpaqueTokenIntrospectionResponse>> clusterCacheDataCaptor = ArgumentCaptor.forClass(CacheData.class);
        verify(clusterCachePersistence, times(1)).put(eq(OPAQUE_TOKEN), clusterCacheDataCaptor.capture());

        // in memory cache must be set with IDP value
        ArgumentCaptor<CacheData<OAuth2OpaqueTokenIntrospectionResponse>> inMemoryCacheDataCaptor = ArgumentCaptor.forClass(CacheData.class);
        verify(inMemoryCachePersistence, times(1)).put(eq(OPAQUE_TOKEN), inMemoryCacheDataCaptor.capture());

    }

    @ParameterizedTest
    @ArgumentsSource(CryptoAccessArgumentsProvider.class)
    @NullSource
    void introspect_with_cluster_cache__in_memory_cache_has_entry_but_cluster_cache_has_no_entry__calls_NOT_idp_and_sets_NOT_cache_values(
            CryptoAccessProvider<OAuth2OpaqueTokenIntrospectionResponse> cryptoAccessProvider) {
        /* prepare */

        Long expiresAt = Instant.now().plus(2, ChronoUnit.DAYS).getEpochSecond();
        CachePersistence<OAuth2OpaqueTokenIntrospectionResponse> clusterCachePersistence = mock();

        RestTemplateOAuth2OpaqueTokenIDPIntrospectionResponseFetcher fetcher = mock();
        InMemoryCachePersistence<OAuth2OpaqueTokenIntrospectionResponse> inMemoryCachePersistence = mock();

        OAuth2OpaqueTokenIntrospectionResponse introspectionResponse = createIntrospectionResponse(Boolean.TRUE, expiresAt);

        CacheData<OAuth2OpaqueTokenIntrospectionResponse> cache0 = new CacheData<OAuth2OpaqueTokenIntrospectionResponse>(introspectionResponse,
                Duration.ofDays(2), cryptoAccessProvider, Instant.now());

        when(inMemoryCachePersistence.get(OPAQUE_TOKEN)).thenReturn(cache0);
        when(clusterCachePersistence.get(OPAQUE_TOKEN)).thenReturn(null);

        OAuth2OpaqueTokenIntrospector introspectorToTest = createOAuth2OpaqueTokenIntrospectorNoMinimumTokenValidityButClusterCachePersistence(
                inMemoryCachePersistence, clusterCachePersistence, fetcher, cryptoAccessProvider);

        /* execute 1 */
        introspectorToTest.introspect(OPAQUE_TOKEN);

        /* test */
        // IDP must NOT be called
        verify(fetcher, times(0)).fetchOpaqueTokenIntrospectionFromIDP(OPAQUE_TOKEN);

        // cluster cache must be set with IDP value
        verify(clusterCachePersistence, times(0)).put(eq(OPAQUE_TOKEN), any());

        // in memory cache must be set with IDP value
        verify(inMemoryCachePersistence, times(0)).put(eq(OPAQUE_TOKEN), any());

    }

    @ParameterizedTest
    @ArgumentsSource(CryptoAccessArgumentsProvider.class)
    @NullSource
    void introspect_with_cluster_cache__in_memory_cache_has_no_entry_but_cluster_cache_has_entry__calls_NOT_idp_and_sets_only_in_memory_cache_value(
            CryptoAccessProvider<OAuth2OpaqueTokenIntrospectionResponse> cryptoAccessProvider) {
        /* prepare */
        Long expiresAt = Instant.now().plus(2, ChronoUnit.DAYS).getEpochSecond();
        CachePersistence<OAuth2OpaqueTokenIntrospectionResponse> clusterCachePersistence = mock();

        RestTemplateOAuth2OpaqueTokenIDPIntrospectionResponseFetcher fetcher = mock();
        InMemoryCachePersistence<OAuth2OpaqueTokenIntrospectionResponse> inMemoryCachePersistence = mock();

        OAuth2OpaqueTokenIntrospectionResponse introspectionResponse = createIntrospectionResponse(Boolean.TRUE, expiresAt);

        CacheData<OAuth2OpaqueTokenIntrospectionResponse> cache0 = new CacheData<OAuth2OpaqueTokenIntrospectionResponse>(introspectionResponse,
                Duration.ofDays(2), cryptoAccessProvider, Instant.now());

        when(inMemoryCachePersistence.get(OPAQUE_TOKEN)).thenReturn(null);
        when(clusterCachePersistence.get(OPAQUE_TOKEN)).thenReturn(cache0);

        OAuth2OpaqueTokenIntrospector introspectorToTest = createOAuth2OpaqueTokenIntrospectorNoMinimumTokenValidityButClusterCachePersistence(
                inMemoryCachePersistence, clusterCachePersistence, fetcher, cryptoAccessProvider);

        /* execute 1 */
        introspectorToTest.introspect(OPAQUE_TOKEN);

        /* test */
        // IDP must NOT be called
        verify(fetcher, times(0)).fetchOpaqueTokenIntrospectionFromIDP(OPAQUE_TOKEN);

        // cluster cache must be never set with IDP value
        verify(clusterCachePersistence, never()).put(eq(OPAQUE_TOKEN), any());

        // in memory cache must be set with IDP value
        @SuppressWarnings("unchecked")
        ArgumentCaptor<CacheData<OAuth2OpaqueTokenIntrospectionResponse>> captor = ArgumentCaptor.forClass(CacheData.class);
        verify(inMemoryCachePersistence, times(1)).put(eq(OPAQUE_TOKEN), captor.capture());

        CacheData<OAuth2OpaqueTokenIntrospectionResponse> data = captor.getValue();
        assertThat(data.getCreatedAt()).isNotNull();
        assertThat(data.getDuration()).isNotNull();
        assertThat(data.getValue()).isEqualTo(introspectionResponse);

    }

    @Test
    void introspect_with_token_expires_at_null_when_minimum_token_validity_is_greater_than_default_token_expires_in_uses_minimum_token_validity() {
        /* prepare */
        // minimum token validity greater than the default
        Duration minimumTokenValidity = DEFAULT_TOKEN_EXPIRES_IN.plusDays(1);

        OAuth2OpaqueTokenIntrospector introspectorToTest = createOAuth2OpaqueTokenIntrospectorWithMinimumTokenValidity(minimumTokenValidity);
        OAuth2OpaqueTokenIntrospectionResponse introspectionResponse = createIntrospectionResponse(Boolean.TRUE, null);
        mockIntrospectionResponse(introspectionResponse);

        /* execute */
        introspectorToTest.introspect(OPAQUE_TOKEN);

        /* test */
        ArgumentCaptor<OAuth2OpaqueTokenIntrospectionResponse> responseCaptor = ArgumentCaptor.forClass(OAuth2OpaqueTokenIntrospectionResponse.class);
        ArgumentCaptor<Instant> nowCaptor = ArgumentCaptor.forClass(Instant.class);

        verify(expirationCalculator).isExpired(responseCaptor.capture(), nowCaptor.capture());

        Instant now = nowCaptor.getValue();
        Instant minimumTokenValidityInstant = now.plus(minimumTokenValidity).truncatedTo(ChronoUnit.SECONDS);

        OAuth2OpaqueTokenIntrospectionResponse response = responseCaptor.getValue();

        // check expiration calculator got same value for calculation as response value
        assertThat(introspectionResponse.getExpiresAtAsInstant()).isEqualTo(response.getExpiresAtAsInstant());

        // check that response calculation used the minimum token validity value
        assertThat(response.getExpiresAtAsInstant()).isEqualTo(minimumTokenValidityInstant);

    }

    @Test
    void introspect_with_token_expires_at_null_when_minimum_token_validity_is_less_than_default_token_expires_in_uses_minimum_token_validity() {
        /* prepare */
        // minimum token validity less than the default
        Duration minimumTokenValidity = DEFAULT_TOKEN_EXPIRES_IN.minusDays(1);

        OAuth2OpaqueTokenIntrospector introspectorToTest = createOAuth2OpaqueTokenIntrospectorWithMinimumTokenValidity(minimumTokenValidity);
        OAuth2OpaqueTokenIntrospectionResponse introspectionResponse = createIntrospectionResponse(Boolean.TRUE, null);
        mockIntrospectionResponse(introspectionResponse);

        /* execute */
        introspectorToTest.introspect(OPAQUE_TOKEN);

        /* test */
        ArgumentCaptor<OAuth2OpaqueTokenIntrospectionResponse> responseCaptor = ArgumentCaptor.forClass(OAuth2OpaqueTokenIntrospectionResponse.class);
        ArgumentCaptor<Instant> nowCaptor = ArgumentCaptor.forClass(Instant.class);

        verify(expirationCalculator).isExpired(responseCaptor.capture(), nowCaptor.capture());

        Instant now = nowCaptor.getValue();
        Instant calculated = now.plus(DEFAULT_TOKEN_EXPIRES_IN).truncatedTo(ChronoUnit.SECONDS);

        OAuth2OpaqueTokenIntrospectionResponse response = responseCaptor.getValue();

        // check expiration calculator got same value for calculation as response value
        assertThat(introspectionResponse.getExpiresAtAsInstant()).isEqualTo(response.getExpiresAtAsInstant());

        // check that response calculation used the calculated value
        assertThat(response.getExpiresAtAsInstant()).isEqualTo(calculated);

    }

    @Test
    void introspect_with_token_expires_at_greater_than_minimum_token_validity_and_greater_than_default_token_expires_in_uses_token_expires_at() {
        /* prepare */
        // minimum token validity greater than the default
        Duration minimumTokenValidity = DEFAULT_TOKEN_EXPIRES_IN.plusDays(1);

        Long expiresAt = Instant.now().plus(minimumTokenValidity.plusDays(2)).getEpochSecond();
        OAuth2OpaqueTokenIntrospector introspectorToTest = createOAuth2OpaqueTokenIntrospectorWithMinimumTokenValidity(minimumTokenValidity);
        OAuth2OpaqueTokenIntrospectionResponse introspectionResponse = createIntrospectionResponse(Boolean.TRUE, expiresAt);
        mockIntrospectionResponse(introspectionResponse);

        /* execute */
        introspectorToTest.introspect(OPAQUE_TOKEN);

        /* test */
        ArgumentCaptor<OAuth2OpaqueTokenIntrospectionResponse> responseCaptor = ArgumentCaptor.forClass(OAuth2OpaqueTokenIntrospectionResponse.class);
        ArgumentCaptor<Instant> nowCaptor = ArgumentCaptor.forClass(Instant.class);

        verify(expirationCalculator).isExpired(responseCaptor.capture(), nowCaptor.capture());

        Instant expiresAtInstant = Instant.ofEpochSecond(expiresAt);

        OAuth2OpaqueTokenIntrospectionResponse response = responseCaptor.getValue();

        // check expiration calculator got same value for calculation as response value
        assertThat(introspectionResponse.getExpiresAtAsInstant()).isEqualTo(response.getExpiresAtAsInstant());

        // check that token value is used here
        assertThat(response.getExpiresAtAsInstant()).isEqualTo(expiresAtInstant);

    }

    private void mockIntrospectionResponse(OAuth2OpaqueTokenIntrospectionResponse introspectionResponse) {
        when(fetcher.fetchOpaqueTokenIntrospectionFromIDP(OPAQUE_TOKEN)).thenReturn(introspectionResponse);
    }

    private void mockUserDetailsService() {
        Collection<? extends GrantedAuthority> authorities = Set.of(new SimpleGrantedAuthority(TestRoles.USER));
        when(userDetailsService.loadUserByUsername(SUBJECT)).thenReturn(new TestUserDetails(authorities, SUBJECT));
    }

    private static Duration truncate(Duration duration) {
        return duration.truncatedTo(ChronoUnit.SECONDS);
    }

    private static Instant truncate(Instant instant) {
        return instant.truncatedTo(ChronoUnit.SECONDS);
    }

/* @formatter:off */
    private OAuth2OpaqueTokenIntrospector createOAuth2OpaqueTokenIntrospectorNoMinimumTokenValidityAndNoClusterCachePersistence() {

        return OAuth2OpaqueTokenIntrospector.builder().
                setIntrospectionResponseFetcher(fetcher).
                setDefaultTokenExpiresIn(DEFAULT_TOKEN_EXPIRES_IN).
                setMaxCacheDuration(MAX_CACHE_DURATION).
                setPreCacheDuration(PRE_CACHE_DURATION).
                setInMemoryCacheClearPeriod(IN_MEMORY_CACHE_CLEAR_PERIOD).
                setClusterCacheClearPeriod(CLUSTER_CACHE_CLEAR_PERIOD).
                setUserDetailsService(userDetailsService).
                setApplicationShutdownHandler(applicationTestShutdownHandler).
                setExpirationCalculator(expirationCalculator).
                setTokenClusterCachePersistence(null).
                setMinimumTokenValidity(null).

                build();
    }

    private OAuth2OpaqueTokenIntrospector createOAuth2OpaqueTokenIntrospectorNoMinimumTokenValidityButClusterCachePersistence(InMemoryCachePersistence<OAuth2OpaqueTokenIntrospectionResponse>inMemoryCachePersistence, CachePersistence<OAuth2OpaqueTokenIntrospectionResponse> clusterCachePersistence, RestTemplateOAuth2OpaqueTokenIDPIntrospectionResponseFetcher fetcher, CryptoAccessProvider<OAuth2OpaqueTokenIntrospectionResponse> cryptoAccessProvider) {

        return OAuth2OpaqueTokenIntrospector.builder().
                setIntrospectionResponseFetcher(fetcher).
                setDefaultTokenExpiresIn(DEFAULT_TOKEN_EXPIRES_IN).
                setMaxCacheDuration(MAX_CACHE_DURATION).
                setPreCacheDuration(PRE_CACHE_DURATION).
                setInMemoryCacheClearPeriod(IN_MEMORY_CACHE_CLEAR_PERIOD).
                setClusterCacheClearPeriod(CLUSTER_CACHE_CLEAR_PERIOD).
                setUserDetailsService(userDetailsService).
                setApplicationShutdownHandler(applicationTestShutdownHandler).
                setExpirationCalculator(expirationCalculator).
                setTokenClusterCachePersistence(clusterCachePersistence).
                setIntrospectionResponseFetcher(fetcher).
                setTokenInMemoryCachePersistence(inMemoryCachePersistence).
                setCryptoAccessProvider(cryptoAccessProvider).
                setMinimumTokenValidity(null).

                build();
    }

    private OAuth2OpaqueTokenIntrospector createOAuth2OpaqueTokenIntrospectorWithMinimumTokenValidity(Duration minimumTokenValidity) {

        return OAuth2OpaqueTokenIntrospector.builder().
                setIntrospectionResponseFetcher(fetcher).
                setDefaultTokenExpiresIn(DEFAULT_TOKEN_EXPIRES_IN).
                setMaxCacheDuration(MAX_CACHE_DURATION).
                setPreCacheDuration(PRE_CACHE_DURATION).
                setInMemoryCacheClearPeriod(IN_MEMORY_CACHE_CLEAR_PERIOD).
                setClusterCacheClearPeriod(CLUSTER_CACHE_CLEAR_PERIOD).
                setUserDetailsService(userDetailsService).
                setApplicationShutdownHandler(applicationTestShutdownHandler).
                setExpirationCalculator(expirationCalculator).
                setTokenClusterCachePersistence(null).
                setMinimumTokenValidity(minimumTokenValidity).

                build();

    }
    /* @formatter:on */

    private static OAuth2OpaqueTokenIntrospectionResponse createIntrospectionResponse(Boolean isActive) {
        return createIntrospectionResponse(isActive, Instant.MAX.getEpochSecond());
    }

    /* @formatter:off */
    private static OAuth2OpaqueTokenIntrospectionResponse createIntrospectionResponse(Boolean isActive, Long expiresAt) {
        return new OAuth2OpaqueTokenIntrospectionResponse(
                isActive,
                "scope",
                "client-id",
                "client-type",
                SUBJECT,
                "token-type",
                expiresAt,
                SUBJECT,
                "aud",
                "group-type"
        );
    }
    /* @formatter:on */

    private static class OAuth2OpaqueTokenIntrospectorSingleNullArgumentProvider implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext) throws Exception {
            /* @formatter:off */
            return Stream.of(
                    Arguments.of("a0", null, DEFAULT_TOKEN_EXPIRES_IN, MAX_CACHE_DURATION, mock(UserDetailsService.class), "Parameter fetcher must not be null"),
                    Arguments.of("a4", mock(RestTemplateOAuth2OpaqueTokenIDPIntrospectionResponseFetcher.class), null, MAX_CACHE_DURATION, mock(UserDetailsService.class), "Parameter defaultTokenExpiresIn must not be null"),
                    Arguments.of("a5", mock(RestTemplateOAuth2OpaqueTokenIDPIntrospectionResponseFetcher.class), DEFAULT_TOKEN_EXPIRES_IN, null, mock(UserDetailsService.class), "Parameter maxCacheDuration must not be null"),
                    Arguments.of("a6", mock(RestTemplateOAuth2OpaqueTokenIDPIntrospectionResponseFetcher.class), MAX_CACHE_DURATION, MAX_CACHE_DURATION, null, "Parameter userDetailsService must not be null"));
            /* @formatter:on */
        }
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

    private static class TestApplicationShutdownHandler implements ApplicationShutdownHandler {

        private List<ShutdownListener> shutdownListeners = new ArrayList<>();

        @Override
        public void register(ShutdownListener shutdownListener) {
            shutdownListeners.add(shutdownListener);
        }

        public void simulateShutdown() {
            for (ShutdownListener shutdownListener : shutdownListeners) {
                shutdownListener.onShutdown();
            }
        }
    }
}