// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.spring.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

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
    private static final OpaqueTokenIntrospector introspectorToTest = new OAuth2OpaqueTokenIntrospector(restTemplate, INTROSPECTION_URI, CLIENT_ID,
            CLIENT_SECRET, userDetailsService);
    private static final String OPAQUE_TOKEN = "opaque-token";
    private static final String SUBJECT = "sub";
    private static final int DEFAULT_EXPIRES_IN_SECONDS = 60 * 60 * 24;

    @BeforeEach
    void beforeEach() {
        reset(restTemplate, userDetailsService);
    }

    /* @formatter:off */
    @ParameterizedTest
    @ArgumentsSource(Base64OAuth2OpaqueTokenIntrospectorSingleNullArgumentProvider.class)
    void construct_base64_o_auth_2_opaque_token_introspector_with_null_argument_fails(RestTemplate restTemplate,
                                                                                      String introspectionUri,
                                                                                      String clientId,
                                                                                      String clientSecret,
                                                                                      UserDetailsService userDetailsService,
                                                                                      String errMsg) {
        assertThatThrownBy(() -> new OAuth2OpaqueTokenIntrospector(restTemplate, introspectionUri, clientId, clientSecret, userDetailsService))
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
        OAuth2OpaqueTokenIntrospectionResponse OAuth2OpaqueTokenIntrospectionResponse = createOpaqueTokenResponse(Boolean.FALSE, null);
        when(restTemplate.postForObject(eq(INTROSPECTION_URI), any(), eq(OAuth2OpaqueTokenIntrospectionResponse.class))).thenReturn(OAuth2OpaqueTokenIntrospectionResponse);

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
        long expiresAt = 3600L;
        OAuth2OpaqueTokenIntrospectionResponse OAuth2OpaqueTokenIntrospectionResponse = createOpaqueTokenResponse(Boolean.TRUE, expiresAt);
        when(restTemplate.postForObject(eq(INTROSPECTION_URI), any(), eq(OAuth2OpaqueTokenIntrospectionResponse.class))).thenReturn(OAuth2OpaqueTokenIntrospectionResponse);
        Collection<? extends GrantedAuthority> authorities = Set.of(new SimpleGrantedAuthority(TestRoles.USER));
        when(userDetailsService.loadUserByUsername(SUBJECT)).thenReturn(new TestUserDetails(authorities, SUBJECT));

        /* execute */
        OAuth2AuthenticatedPrincipal principal = introspectorToTest.introspect(OPAQUE_TOKEN);

        /* assert */
        assertThat(principal.getName()).isEqualTo(SUBJECT);
        Map<String, Object> attributes = principal.getAttributes();
        assertThat(attributes.get(OAuth2TokenIntrospectionClaimNames.ACTIVE)).isEqualTo(OAuth2OpaqueTokenIntrospectionResponse.isActive());
        assertThat(attributes.get(OAuth2TokenIntrospectionClaimNames.SCOPE)).isEqualTo(OAuth2OpaqueTokenIntrospectionResponse.getScope());
        assertThat(attributes.get(OAuth2TokenIntrospectionClaimNames.CLIENT_ID)).isEqualTo(OAuth2OpaqueTokenIntrospectionResponse.getClientId());
        assertThat(attributes.get(OAuth2TokenIntrospectionClaimNames.USERNAME)).isEqualTo(OAuth2OpaqueTokenIntrospectionResponse.getUsername());
        assertThat(attributes.get(OAuth2TokenIntrospectionClaimNames.TOKEN_TYPE)).isEqualTo(OAuth2OpaqueTokenIntrospectionResponse.getTokenType());
        assertThat((Instant) attributes.get(OAuth2TokenIntrospectionClaimNames.IAT)).isAfter(Instant.EPOCH);
        assertThat((Instant) attributes.get(OAuth2TokenIntrospectionClaimNames.EXP)).isEqualTo(Instant.ofEpochSecond(expiresAt));
        assertThat(attributes.get(OAuth2TokenIntrospectionClaimNames.SUB)).isEqualTo(OAuth2OpaqueTokenIntrospectionResponse.getSubject());
        assertThat(attributes.get(OAuth2TokenIntrospectionClaimNames.AUD)).isEqualTo(OAuth2OpaqueTokenIntrospectionResponse.getAudience());
    }

    @Test
    void introspect_with_null_expires_at_constructs_principal_with_default_expires_at() {
        /* prepare */
        Instant now = Instant.now();
        OAuth2OpaqueTokenIntrospectionResponse OAuth2OpaqueTokenIntrospectionResponse = createOpaqueTokenResponse(Boolean.TRUE, null);
        when(restTemplate.postForObject(eq(INTROSPECTION_URI), any(), eq(OAuth2OpaqueTokenIntrospectionResponse.class))).thenReturn(OAuth2OpaqueTokenIntrospectionResponse);
        Collection<? extends GrantedAuthority> authorities = Set.of(new SimpleGrantedAuthority(TestRoles.USER));
        when(userDetailsService.loadUserByUsername(SUBJECT)).thenReturn(new TestUserDetails(authorities, SUBJECT));

        /* execute */
        OAuth2AuthenticatedPrincipal principal = introspectorToTest.introspect(OPAQUE_TOKEN);

        /* assert */
        Instant actual = (Instant) principal.getAttributes().get(OAuth2TokenIntrospectionClaimNames.EXP);
        assertThat(actual).isNotNull();
        assertThat(actual).isAfterOrEqualTo(now.plusSeconds(DEFAULT_EXPIRES_IN_SECONDS));
    }

    /* @formatter:off */
    private static OAuth2OpaqueTokenIntrospectionResponse createOpaqueTokenResponse(Boolean isActive, Long expiresAt) {
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

    private static class Base64OAuth2OpaqueTokenIntrospectorSingleNullArgumentProvider implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext) throws Exception {
            return Stream.of(Arguments.of(null, INTROSPECTION_URI, CLIENT_ID, CLIENT_SECRET, userDetailsService, "Parameter restTemplate must not be null"),
                    Arguments.of(restTemplate, null, CLIENT_ID, CLIENT_SECRET, userDetailsService, "Parameter introspectionUri must not be null"),
                    Arguments.of(restTemplate, INTROSPECTION_URI, null, CLIENT_SECRET, userDetailsService, "Parameter clientId must not be null"),
                    Arguments.of(restTemplate, INTROSPECTION_URI, CLIENT_ID, null, userDetailsService, "Parameter clientSecret must not be null"),
                    Arguments.of(restTemplate, INTROSPECTION_URI, CLIENT_ID, CLIENT_SECRET, null, "Parameter userDetailsService must not be null"));
        }
    }
}