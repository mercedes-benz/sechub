// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.spring.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.Instant;
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
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.core.OAuth2TokenIntrospectionClaimNames;
import org.springframework.security.oauth2.server.resource.introspection.BadOpaqueTokenException;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

class OAuth2OpaqueTokenIntrospectorTest {

    private static final String INTROSPECTION_URI = "https://example.org/introspection-uri";
    private static final RestTemplate restTemplate = mock();
    private static final String CLIENT_ID = "example-client-id";
    private static final String CLIENT_SECRET = "example-client-secret";
    private static final UserDetailsService userDetailsService = mock();
    private static final Duration MAX_CACHE_DURATION = Duration.ofDays(1);
    private static final OpaqueTokenIntrospector introspectorToTest = new OAuth2OpaqueTokenIntrospector(restTemplate, INTROSPECTION_URI, CLIENT_ID,
            CLIENT_SECRET, MAX_CACHE_DURATION, userDetailsService);
    private static final String OPAQUE_TOKEN = "opaque-token";
    private static final String SUBJECT = "sub";

    @BeforeEach
    void beforeEach() {
        reset(restTemplate, userDetailsService);
    }

    /* @formatter:off */
    @ParameterizedTest
    @ArgumentsSource(OAuth2OpaqueTokenIntrospectorSingleNullArgumentProvider.class)
    void construct_o_auth_2_opaque_token_introspector_with_null_argument_fails(RestTemplate restTemplate,
                                                                                      String introspectionUri,
                                                                                      String clientId,
                                                                                      String clientSecret,
                                                                                      Duration maxCacheDuration,
                                                                                      UserDetailsService userDetailsService,
                                                                                      String errMsg) {
        assertThatThrownBy(() -> new OAuth2OpaqueTokenIntrospector(restTemplate, introspectionUri, clientId, clientSecret, maxCacheDuration, userDetailsService))
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
    void introspect_with_cached_opaque_token_returns_cached_opaque_token_introspection_response() {
        /* prepare */
        OAuth2OpaqueTokenIntrospectionResponse OAuth2OpaqueTokenIntrospectionResponse = createOpaqueTokenResponse(Boolean.TRUE);
        when(restTemplate.postForObject(eq(INTROSPECTION_URI), any(), eq(OAuth2OpaqueTokenIntrospectionResponse.class)))
                .thenReturn(OAuth2OpaqueTokenIntrospectionResponse);
        Collection<? extends GrantedAuthority> authorities = Set.of(new SimpleGrantedAuthority(TestRoles.USER));
        when(userDetailsService.loadUserByUsername(SUBJECT)).thenReturn(new TestUserDetails(authorities, SUBJECT));
        introspectorToTest.introspect(OPAQUE_TOKEN);

        /* execute */
        introspectorToTest.introspect(OPAQUE_TOKEN);

        /* test */
        verifyNoInteractions(restTemplate);


    }

    @Test
    void introspect_with_null_response_fails() {
        /* prepare */
        when(restTemplate.postForObject(eq(INTROSPECTION_URI), any(), eq(OAuth2OpaqueTokenIntrospectionResponse.class))).thenReturn(null);

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
        OAuth2OpaqueTokenIntrospectionResponse OAuth2OpaqueTokenIntrospectionResponse = createOpaqueTokenResponse(Boolean.FALSE);
        when(restTemplate.postForObject(eq(INTROSPECTION_URI), any(), eq(OAuth2OpaqueTokenIntrospectionResponse.class)))
                .thenReturn(OAuth2OpaqueTokenIntrospectionResponse);

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
        OAuth2OpaqueTokenIntrospectionResponse introspectionResponse = createOpaqueTokenResponse(Boolean.TRUE);
        when(restTemplate.postForObject(eq(INTROSPECTION_URI), any(), eq(OAuth2OpaqueTokenIntrospectionResponse.class)))
                .thenReturn(introspectionResponse);
        Collection<? extends GrantedAuthority> authorities = Set.of(new SimpleGrantedAuthority(TestRoles.USER));
        when(userDetailsService.loadUserByUsername(SUBJECT)).thenReturn(new TestUserDetails(authorities, SUBJECT));

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
        assertThat((Instant) attributes.get(OAuth2TokenIntrospectionClaimNames.IAT)).isEqualTo(introspectionResponse.getIssuedAt());
        assertThat((Instant) attributes.get(OAuth2TokenIntrospectionClaimNames.EXP)).isEqualTo(introspectionResponse.getExpiresAt());
        assertThat(attributes.get(OAuth2TokenIntrospectionClaimNames.SUB)).isEqualTo(introspectionResponse.getSubject());
        assertThat(attributes.get(OAuth2TokenIntrospectionClaimNames.AUD)).isEqualTo(introspectionResponse.getAudience());
    }

    /* @formatter:off */
    private static OAuth2OpaqueTokenIntrospectionResponse createOpaqueTokenResponse(Boolean isActive) {
        return new OAuth2OpaqueTokenIntrospectionResponse(
                isActive,
                "scope",
                "client-id",
                "client-type",
                SUBJECT,
                "token-type",
                Instant.MAX.getEpochSecond(),
                SUBJECT,
                "aud",
                "group-type"
        );
    }
    /* @formatter:on */

    private static class OAuth2OpaqueTokenIntrospectorSingleNullArgumentProvider implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext) throws Exception {
            return Stream.of(Arguments.of(null, INTROSPECTION_URI, CLIENT_ID, CLIENT_SECRET, MAX_CACHE_DURATION, userDetailsService, "Parameter restTemplate must not be null"),
                    Arguments.of(restTemplate, null, CLIENT_ID, CLIENT_SECRET, MAX_CACHE_DURATION, userDetailsService, "Parameter introspectionUri must not be null"),
                    Arguments.of(restTemplate, INTROSPECTION_URI, null, CLIENT_SECRET, MAX_CACHE_DURATION, userDetailsService, "Parameter clientId must not be null"),
                    Arguments.of(restTemplate, INTROSPECTION_URI, CLIENT_ID, null, MAX_CACHE_DURATION, userDetailsService, "Parameter clientSecret must not be null"),
                    Arguments.of(restTemplate, INTROSPECTION_URI, CLIENT_ID, CLIENT_SECRET, null, userDetailsService, "Parameter maxCacheDuration must not be null"),
                    Arguments.of(restTemplate, INTROSPECTION_URI, CLIENT_ID, CLIENT_SECRET, MAX_CACHE_DURATION, null, "Parameter userDetailsService must not be null"));
        }
    }
}