// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.spring.security;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedConstruction;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.core.OAuth2TokenIntrospectionClaimNames;
import org.springframework.security.oauth2.server.resource.introspection.BadOpaqueTokenException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import com.mercedesbenz.sechub.commons.core.cache.InMemoryCache;
import com.mercedesbenz.sechub.commons.core.shutdown.ApplicationShutdownHandler;

class OAuth2OpaqueTokenIntrospectorTest {

    private static final String INTROSPECTION_URI = "https://example.org/introspection-uri";
    private static final RestTemplate restTemplate = mock();
    private static final String CLIENT_ID = "example-client-id";
    private static final String CLIENT_SECRET = "example-client-secret";
    private static final UserDetailsService userDetailsService = mock();
    private static final ApplicationShutdownHandler applicationShutdownHandler = mock();
    private static final OAuth2OpaqueTokenExpirationCalculator expirationCalculator = mock();
    private static final Duration DEFAULT_TOKEN_EXPIRES_IN = Duration.ofDays(1);
    private static final Duration MAX_CACHE_DURATION = Duration.ofDays(30);
    private static final String OPAQUE_TOKEN = "opaque-token";
    private static final String SUBJECT = "sub";

    private static OAuth2OpaqueTokenIntrospector introspectorToTest;

    @BeforeEach
    void beforeEach() {
        reset(restTemplate, userDetailsService, expirationCalculator, applicationShutdownHandler);

        /* reset the cache which is associated with each individual instance */
        introspectorToTest = createOAuth2OpaqueTokenIntrospectorNoMinimumTokenValidity();

        mockUserDetailsService();
    }

    /* @formatter:off */
    @ParameterizedTest
    @ArgumentsSource(OAuth2OpaqueTokenIntrospectorSingleNullArgumentProvider.class)
    void construct_o_auth_2_opaque_token_introspector_with_null_argument_fails(RestTemplate restTemplate,
                                                                               String introspectionUri,
                                                                               String clientId,
                                                                               String clientSecret,
                                                                               Duration defaultTokenExpiresIn,
                                                                               Duration maxCacheDuration,
                                                                               UserDetailsService userDetailsService,
                                                                               String errMsg) {
        assertThatThrownBy(() -> new OAuth2OpaqueTokenIntrospector(restTemplate, introspectionUri, clientId, clientSecret, defaultTokenExpiresIn, maxCacheDuration, userDetailsService, applicationShutdownHandler, expirationCalculator, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining(errMsg);
    }
    /* @formatter:on */

    @ParameterizedTest
    @NullAndEmptySource
    void introspect_with_null_or_empty_opaque_token_fails(String opaqueToken) {
        /* @formatter:off */
        assertThatThrownBy(() -> introspectorToTest.introspect(opaqueToken))
                .isInstanceOf(BadOpaqueTokenException.class)
                .hasMessageContaining("Token is null or empty");
        /* @formatter:on */
    }

    @Test
    void introspect_with_no_cached_opaque_calls_introspection_endpoint() {
        OAuth2OpaqueTokenIntrospectionResponse introspectionResponse = createIntrospectionResponse(Boolean.TRUE);
        mockIntrospectionResponse(introspectionResponse);
        verifyNoInteractions(restTemplate);

        /* execute */
        introspectorToTest.introspect(OPAQUE_TOKEN);

        /* test */
        verify(restTemplate).postForObject(eq(INTROSPECTION_URI), any(), eq(OAuth2OpaqueTokenIntrospectionResponse.class));
    }

    @Test
    void introspect_with_cached_opaque_token_returns_cached_opaque_token_introspection_response() {
        /* prepare */
        OAuth2OpaqueTokenIntrospectionResponse introspectionResponse = createIntrospectionResponse(Boolean.TRUE);
        mockIntrospectionResponse(introspectionResponse);
        /* first introspection should cache the result */
        introspectorToTest.introspect(OPAQUE_TOKEN);
        verify(restTemplate).postForObject(eq(INTROSPECTION_URI), any(), eq(OAuth2OpaqueTokenIntrospectionResponse.class));
        reset(restTemplate);
        verifyNoInteractions(restTemplate);

        /* execute */
        introspectorToTest.introspect(OPAQUE_TOKEN);

        /* test */
        verifyNoInteractions(restTemplate);
    }

    @Test
    void introspect_with_null_response_fails() {
        /* prepare */
        mockIntrospectionResponse(null);

        /* execute & assert */
        /* @formatter:off */
        assertThatThrownBy(() -> introspectorToTest.introspect(OPAQUE_TOKEN))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Failed to perform token introspection");
        /* @formatter:on */
    }

    @Test
    void introspect_with_inactive_token_fails() {
        /* prepare */
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
        assertThat(attributes.get(OAuth2TokenIntrospectionClaimNames.EXP)).isEqualTo(introspectionResponse.getExpiresAt());
        assertThat(attributes.get(OAuth2TokenIntrospectionClaimNames.SUB)).isEqualTo(introspectionResponse.getSubject());
        assertThat(attributes.get(OAuth2TokenIntrospectionClaimNames.AUD)).isEqualTo(introspectionResponse.getAudience());
    }

    @Test
    @SuppressWarnings({ "rawtypes", "unchecked" })
    void introspect_with_token_expires_at_null_uses_default_token_expires_at_for_cache() {
        try (MockedConstruction<InMemoryCache> cacheConstruction = mockConstruction(InMemoryCache.class, (mock, context) -> {
        })) {
            /* prepare */
            OAuth2OpaqueTokenIntrospector introspectorToTest = createOAuth2OpaqueTokenIntrospectorNoMinimumTokenValidity();
            Instant testStartTime = Instant.now();
            OAuth2OpaqueTokenIntrospectionResponse introspectionResponse = createIntrospectionResponse(Boolean.TRUE, null);
            mockIntrospectionResponse(introspectionResponse);

            /* execute */
            OAuth2AuthenticatedPrincipal principal = introspectorToTest.introspect(OPAQUE_TOKEN);

            /* assert */
            InMemoryCache cache = cacheConstruction.constructed().get(0);
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
        OAuth2OpaqueTokenIntrospector introspectorToTest = createOAuth2OpaqueTokenIntrospectorNoMinimumTokenValidity();
        OAuth2OpaqueTokenIntrospectionResponse introspectionResponse = createIntrospectionResponse(Boolean.TRUE, null);
        mockIntrospectionResponse(introspectionResponse);

        /* execute */
        introspectorToTest.introspect(OPAQUE_TOKEN);

        /* test */
        ArgumentCaptor<OAuth2OpaqueTokenIntrospectionResponse> responseCaptor = ArgumentCaptor.forClass(OAuth2OpaqueTokenIntrospectionResponse.class);
        ArgumentCaptor<Instant> nowCaptor = ArgumentCaptor.forClass(Instant.class);

        verify(expirationCalculator).isExpired(responseCaptor.capture(), nowCaptor.capture());

        Instant now = nowCaptor.getValue();
        Instant calculated = now.plus(DEFAULT_TOKEN_EXPIRES_IN);

        OAuth2OpaqueTokenIntrospectionResponse response = responseCaptor.getValue();

        // check expiration calculator got same value for calculation as response value
        assertThat(introspectionResponse.getExpiresAt()).isEqualTo(response.getExpiresAt());

        // check that response calculation used the calculated value
        assertThat(response.getExpiresAt()).isEqualTo(calculated);

    }

    @Test
    void when_calculator_says_expired_than_a_introspect_will_throw_bad_opaque_token_excpetion() {
        /* prepare */
        OAuth2OpaqueTokenIntrospectionResponse introspectionResponse = createIntrospectionResponse(Boolean.TRUE, Long.valueOf(4711));
        mockIntrospectionResponse(introspectionResponse);

        when(expirationCalculator.isExpired(eq(introspectionResponse), any())).thenReturn(true); // this response is always expired for this test

        /* execute */
        assertThatThrownBy(() -> introspectorToTest.introspect(OPAQUE_TOKEN)).isInstanceOf(BadOpaqueTokenException.class).hasMessageContaining("expired");

    }

    @Test
    @SuppressWarnings({ "rawtypes", "unchecked" })
    void introspect_with_token_expires_at_not_null() {
        try (MockedConstruction<InMemoryCache> cacheConstruction = mockConstruction(InMemoryCache.class, (mock, context) -> {
        })) {
            /* prepare */
            OAuth2OpaqueTokenIntrospector introspectorToTest = createOAuth2OpaqueTokenIntrospectorNoMinimumTokenValidity();
            Instant testStartTime = Instant.now();
            Duration expiresIn = Duration.ofDays(15);
            Instant expiresAt = testStartTime.plus(expiresIn);
            OAuth2OpaqueTokenIntrospectionResponse introspectionResponse = createIntrospectionResponse(Boolean.TRUE, expiresAt.getEpochSecond());
            mockIntrospectionResponse(introspectionResponse);

            /* execute */
            OAuth2AuthenticatedPrincipal principal = introspectorToTest.introspect(OPAQUE_TOKEN);

            /* assert */
            InMemoryCache cache = cacheConstruction.constructed().get(0);
            /* @formatter:off */
            verify(cache).put(
                    eq(OPAQUE_TOKEN),
                    eq(introspectionResponse),
                    assertArg(arg -> {
                        /* subtract a buffer of 1 sec for program execution */
                        Duration expectedExpiresIn = expiresIn.minus(Duration.ofSeconds(1));
                        assertThat(truncate(arg)).isEqualTo(truncate(expectedExpiresIn));
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
        try (MockedConstruction<InMemoryCache> cacheConstruction = mockConstruction(InMemoryCache.class, (mock, context) -> {
        })) {
            /* prepare */
            OAuth2OpaqueTokenIntrospector introspectorToTest = createOAuth2OpaqueTokenIntrospectorNoMinimumTokenValidity();
            Instant expiresAt = Instant.MAX;
            OAuth2OpaqueTokenIntrospectionResponse introspectionResponse = createIntrospectionResponse(Boolean.TRUE, expiresAt.getEpochSecond());
            mockIntrospectionResponse(introspectionResponse);

            /* execute */
            introspectorToTest.introspect(OPAQUE_TOKEN);

            /* assert */
            InMemoryCache cache = cacheConstruction.constructed().get(0);
            /* @formatter:off */
            verify(cache).put(
                    eq(OPAQUE_TOKEN),
                    eq(introspectionResponse),
                    assertArg(arg -> assertThat(arg).isEqualTo(MAX_CACHE_DURATION))
            );
            /* @formatter:on */
        }
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
        Instant minimumTokenValidityIntant = now.plus(minimumTokenValidity);

        OAuth2OpaqueTokenIntrospectionResponse response = responseCaptor.getValue();

        // check expiration calculator got same value for calculation as response value
        assertThat(introspectionResponse.getExpiresAt()).isEqualTo(response.getExpiresAt());

        // check that response calculation used the minimum token validity value
        assertThat(response.getExpiresAt()).isEqualTo(minimumTokenValidityIntant);

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
        Instant calculated = now.plus(DEFAULT_TOKEN_EXPIRES_IN);

        OAuth2OpaqueTokenIntrospectionResponse response = responseCaptor.getValue();

        // check expiration calculator got same value for calculation as response value
        assertThat(introspectionResponse.getExpiresAt()).isEqualTo(response.getExpiresAt());

        // check that response calculation used the calculated value
        assertThat(response.getExpiresAt()).isEqualTo(calculated);

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
        assertThat(introspectionResponse.getExpiresAt()).isEqualTo(response.getExpiresAt());

        // check that token value is used here
        assertThat(response.getExpiresAt()).isEqualTo(expiresAtInstant);

    }

    private static void mockIntrospectionResponse(OAuth2OpaqueTokenIntrospectionResponse introspectionResponse) {
        when(restTemplate.postForObject(eq(INTROSPECTION_URI), any(), eq(OAuth2OpaqueTokenIntrospectionResponse.class))).thenReturn(introspectionResponse);
    }

    private static void mockUserDetailsService() {
        Collection<? extends GrantedAuthority> authorities = Set.of(new SimpleGrantedAuthority(TestRoles.USER));
        when(userDetailsService.loadUserByUsername(SUBJECT)).thenReturn(new TestUserDetails(authorities, SUBJECT));
    }

    private static Duration truncate(Duration duration) {
        return duration.truncatedTo(ChronoUnit.SECONDS);
    }

    private static Instant truncate(Instant instant) {
        return instant.truncatedTo(ChronoUnit.SECONDS);
    }

    private static OAuth2OpaqueTokenIntrospector createOAuth2OpaqueTokenIntrospectorNoMinimumTokenValidity() {
        return new OAuth2OpaqueTokenIntrospector(restTemplate, INTROSPECTION_URI, CLIENT_ID, CLIENT_SECRET, DEFAULT_TOKEN_EXPIRES_IN, MAX_CACHE_DURATION,
                userDetailsService, applicationShutdownHandler, expirationCalculator, null);
    }

    private static OAuth2OpaqueTokenIntrospector createOAuth2OpaqueTokenIntrospectorWithMinimumTokenValidity(Duration minimumTokenValidity) {
        return new OAuth2OpaqueTokenIntrospector(restTemplate, INTROSPECTION_URI, CLIENT_ID, CLIENT_SECRET, DEFAULT_TOKEN_EXPIRES_IN, MAX_CACHE_DURATION,
                userDetailsService, applicationShutdownHandler, expirationCalculator, minimumTokenValidity);
    }

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
                    Arguments.of(null, INTROSPECTION_URI, CLIENT_ID, CLIENT_SECRET, DEFAULT_TOKEN_EXPIRES_IN, MAX_CACHE_DURATION, userDetailsService, "Parameter restTemplate must not be null"),
                    Arguments.of(restTemplate, null, CLIENT_ID, CLIENT_SECRET, DEFAULT_TOKEN_EXPIRES_IN, MAX_CACHE_DURATION, userDetailsService, "Parameter introspectionUri must not be null"),
                    Arguments.of(restTemplate, INTROSPECTION_URI, null, CLIENT_SECRET, DEFAULT_TOKEN_EXPIRES_IN, MAX_CACHE_DURATION, userDetailsService, "Parameter clientId must not be null"),
                    Arguments.of(restTemplate, INTROSPECTION_URI, CLIENT_ID, null, DEFAULT_TOKEN_EXPIRES_IN, MAX_CACHE_DURATION, userDetailsService, "Parameter clientSecret must not be null"),
                    Arguments.of(restTemplate, INTROSPECTION_URI, CLIENT_ID, CLIENT_SECRET, null, MAX_CACHE_DURATION, userDetailsService, "Parameter defaultTokenExpiresIn must not be null"),
                    Arguments.of(restTemplate, INTROSPECTION_URI, CLIENT_ID, CLIENT_SECRET, DEFAULT_TOKEN_EXPIRES_IN, null, userDetailsService, "Parameter maxCacheDuration must not be null"),
                    Arguments.of(restTemplate, INTROSPECTION_URI, CLIENT_ID, CLIENT_SECRET, MAX_CACHE_DURATION, MAX_CACHE_DURATION, null, "Parameter userDetailsService must not be null"));
            /* @formatter:on */
        }
    }
}