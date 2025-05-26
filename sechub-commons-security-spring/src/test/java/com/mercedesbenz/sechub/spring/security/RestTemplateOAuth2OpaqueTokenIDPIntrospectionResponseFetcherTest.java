// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.spring.security;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.security.oauth2.server.resource.introspection.BadOpaqueTokenException;
import org.springframework.web.client.RestTemplate;

class RestTemplateOAuth2OpaqueTokenIDPIntrospectionResponseFetcherTest {

    private static final String INTROSPECTION_URI = "https://example.org/introspection-uri";
    private static final RestTemplate restTemplate = mock();
    private static final String CLIENT_ID = "example-client-id";
    private static final String CLIENT_SECRET = "example-client-secret";

    private static final String OPAQUE_TOKEN = "opaque-token";
    private static final String SUBJECT = "sub";

    private static RestTemplateOAuth2OpaqueTokenIDPIntrospectionResponseFetcher fetcherToTest;

    @BeforeEach
    void beforeEach() {
        reset(restTemplate);

        /* reset the cache which is associated with each individual instance */
        fetcherToTest = createOAuth2OpaqueTokenIntrospectorNoMinimumTokenValidityAndNoClusterCachePersistence();

    }

    /* @formatter:off */
    @ParameterizedTest
    @ArgumentsSource(OAuth2OpaqueTokenIntrospectionResponseSingleNullArgumentProvider.class)
    void construct_with_null_argument_fails(String variant,
                                                                               RestTemplate restTemplate,
                                                                               String introspectionUri,
                                                                               String clientId,
                                                                               String clientSecret,
                                                                               String errMsg) {

        assertThatThrownBy(() -> RestTemplateOAuth2OpaqueTokenIDPIntrospectionResponseFetcher.builder().

                    setRestTemplate(restTemplate).
                    setIntrospectionUri(introspectionUri).
                    setClientId(clientId).
                    setClientSecret(clientSecret).
                build())
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining(errMsg);
    }
    /* @formatter:on */

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = { " ", "  " })
    void fetchOpaqueTokenIntrospectionFromIDP_with_null_or_empty_opaque_token_fails(String opaqueToken) {
        /* @formatter:off */
        assertThatThrownBy(() -> fetcherToTest.fetchOpaqueTokenIntrospectionFromIDP(opaqueToken))
                .isInstanceOf(BadOpaqueTokenException.class)
                .hasMessageContaining("Token is null or empty");
        /* @formatter:on */
    }

    @Test
    void fetchOpaqueTokenIntrospectionFromIDP_calls_introspection_endpoint() {
        /* prepare */
        OAuth2OpaqueTokenIntrospectionResponse introspectionResponse = createIntrospectionResponse(Boolean.TRUE);
        when(restTemplate.postForObject(eq(INTROSPECTION_URI), any(), eq(OAuth2OpaqueTokenIntrospectionResponse.class))).thenReturn(introspectionResponse);

        /* check preconditions */
        verifyNoInteractions(restTemplate);

        /* execute */
        fetcherToTest.fetchOpaqueTokenIntrospectionFromIDP(OPAQUE_TOKEN);

        /* test */
        verify(restTemplate).postForObject(eq(INTROSPECTION_URI), any(), eq(OAuth2OpaqueTokenIntrospectionResponse.class));
    }

/* @formatter:off */
    private static RestTemplateOAuth2OpaqueTokenIDPIntrospectionResponseFetcher createOAuth2OpaqueTokenIntrospectorNoMinimumTokenValidityAndNoClusterCachePersistence() {

        return RestTemplateOAuth2OpaqueTokenIDPIntrospectionResponseFetcher.builder().
                setRestTemplate(restTemplate).
                setIntrospectionUri(INTROSPECTION_URI).
                setClientId(CLIENT_ID).
                setClientSecret(CLIENT_SECRET).

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

    private static class OAuth2OpaqueTokenIntrospectionResponseSingleNullArgumentProvider implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext) throws Exception {
            /* @formatter:off */
            return Stream.of(
                    Arguments.of("a0", null, INTROSPECTION_URI, CLIENT_ID, CLIENT_SECRET, "Parameter restTemplate must not be null"),
                    Arguments.of("a1", restTemplate, null, CLIENT_ID, CLIENT_SECRET, "Parameter introspectionUri must not be null"),
                    Arguments.of("a2", restTemplate, INTROSPECTION_URI, null, CLIENT_SECRET, "Parameter clientId must not be null"),
                    Arguments.of("a3", restTemplate, INTROSPECTION_URI, CLIENT_ID, null, "Parameter clientSecret must not be null"));
            /* @formatter:on */
        }
    }
}