// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.spring.security;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Duration;
import java.time.Instant;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.springframework.security.oauth2.core.OAuth2AccessToken;

class OAuth2TokenExpirationCalculatorTest {
    private static final Duration DEFAULT_EXPIRY = Duration.ofHours(1);

    private OAuth2TokenExpirationCalculator calculatorToTest;

    @BeforeEach
    void beforeEach() throws Exception {
        calculatorToTest = new OAuth2TokenExpirationCalculator();
    }

    @Test
    void isExpired_throws_illegal_argument_exception_if_now_is_null() {
        /* prepare */
        OAuth2OpaqueTokenIntrospectionResponse response = mock();

        /* execute + test */
        assertThatThrownBy(() -> calculatorToTest.isExpired(response, null)).isInstanceOf(NullPointerException.class).hasMessageContaining("now");
    }

    @Test
    void isExpired_throws_illegal_argument_exception_if_response_is_null() {
        /* execute + test */
        assertThatThrownBy(() -> calculatorToTest.isExpired(null, Instant.now())).isInstanceOf(NullPointerException.class).hasMessageContaining("response");
    }

    @ParameterizedTest
    @ArgumentsSource(ExpirationArgumentsProvider.class)
    void isExpired(String variant, boolean shallBeExpired, Instant now, Instant expiresAt) {

        /* prepare */
        OAuth2OpaqueTokenIntrospectionResponse response = mock();
        when(response.getExpiresAtAsInstant()).thenReturn(expiresAt);

        /* execute */
        boolean expired = calculatorToTest.isExpired(response, now);

        /* test */
        assertThat(expired).isEqualTo(shallBeExpired);
    }

    @Test
    void calculateAccessTokenDuration_now_null_throws_exception() {
        /* prepare */
        Duration defaultDuration = mock();
        OAuth2AccessToken oAuth2AccessToken = mock();
        Duration minimumTokenValidity = mock();

        /* execute + test */
        assertThatThrownBy(() -> calculatorToTest.calculateAccessTokenDuration(null, defaultDuration, oAuth2AccessToken, minimumTokenValidity))
                .isInstanceOf(NullPointerException.class).hasMessageContaining("now");
    }

    @Test
    void calculateAccessTokenDuration_default_duration_null_throws_exception() {
        /* prepare */
        Instant now = mock();
        OAuth2AccessToken oAuth2AccessToken = mock();
        Duration minimumTokenValidity = mock();

        /* execute + test */
        assertThatThrownBy(() -> calculatorToTest.calculateAccessTokenDuration(now, null, oAuth2AccessToken, minimumTokenValidity))
                .isInstanceOf(NullPointerException.class).hasMessageContaining("defaultDuration");
    }

    @Test
    void calculateAccessTokenDuration_oAuth2AccessToken_null_throws_exception() {
        /* prepare */
        Instant now = mock();
        Duration defaultDuration = mock();
        Duration minimumTokenValidity = mock();

        /* execute + test */
        assertThatThrownBy(() -> calculatorToTest.calculateAccessTokenDuration(now, defaultDuration, null, minimumTokenValidity))
                .isInstanceOf(NullPointerException.class).hasMessageContaining("oAuth2AccessToken");
    }

    @Test
    void calculateAccessTokenDuration_minimumTokenValidity_null_throws_exception() {
        /* prepare */
        Instant now = mock();
        Duration defaultDuration = mock();
        OAuth2AccessToken oAuth2AccessToken = mock();

        /* execute + test */
        assertThatThrownBy(() -> calculatorToTest.calculateAccessTokenDuration(now, defaultDuration, oAuth2AccessToken, null))
                .isInstanceOf(NullPointerException.class).hasMessageContaining("minimumTokenValidity");
    }

    @Test
    void calculateAccessTokenDuration_minimum_token_validity_when_default_expiry_is_less_than_minimum_and_expires_at_is_null() {
        /* prepare */
        Instant now = Instant.now();
        Duration minimumTokenValidity = DEFAULT_EXPIRY.plusDays(1);
        Instant expectedMinimumTokenValidity = now.plusSeconds(minimumTokenValidity.getSeconds());
        OAuth2AccessToken oAuth2AccessToken = mock();
        when(oAuth2AccessToken.getExpiresAt()).thenReturn(null);

        /* execute */
        Instant calculatedDuration = calculatorToTest.calculateAccessTokenDuration(now, DEFAULT_EXPIRY, oAuth2AccessToken, minimumTokenValidity);

        /* test */
        assertThat(calculatedDuration).isEqualTo(expectedMinimumTokenValidity);
    }

    @Test
    void calculateAccessTokenDuration_on_authentication_success_assumes_default_expiry_when_default_is_greater_than_minimum_and_expires_at_is_null() {
        /* prepare */
        Instant now = Instant.now();
        Duration minimumTokenValidity = DEFAULT_EXPIRY.minusMinutes(20);
        Instant expectedDefaultExpiry = now.plusSeconds(DEFAULT_EXPIRY.getSeconds());
        OAuth2AccessToken oAuth2AccessToken = mock();
        when(oAuth2AccessToken.getExpiresAt()).thenReturn(null);

        /* execute */
        Instant calculatedDuration = calculatorToTest.calculateAccessTokenDuration(now, DEFAULT_EXPIRY, oAuth2AccessToken, minimumTokenValidity);

        /* test */
        assertThat(calculatedDuration).isEqualTo(expectedDefaultExpiry);
    }

    @Test
    void calculateAccessTokenDuration_on_authentication_success_assumes_expires_from_token_when_greater_than_default_and_minimum() {
        /* prepare */
        Instant now = Instant.now();
        Duration minimumTokenValidity = DEFAULT_EXPIRY.plusDays(1);

        Instant expiresAtFromToken = now.plusSeconds(minimumTokenValidity.plusDays(5).getSeconds());
        OAuth2AccessToken oAuth2AccessToken = mock();
        when(oAuth2AccessToken.getExpiresAt()).thenReturn(expiresAtFromToken);

        /* execute */
        Instant calculatedDuration = calculatorToTest.calculateAccessTokenDuration(now, DEFAULT_EXPIRY, oAuth2AccessToken, minimumTokenValidity);

        /* test */
        assertThat(calculatedDuration).isEqualTo(expiresAtFromToken);
    }

    private static class ExpirationArgumentsProvider implements ArgumentsProvider {
        /* @formatter:off */
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext) throws Exception {
            return Stream.of(
                  //Arguments:variant, expectedToBeExpired, now, expiresAt
                  Arguments.of("a1",false, Instant.now(),Instant.now().plus(Duration.ofHours(1))),
                  Arguments.of("a2",false, Instant.now(),Instant.now().plus(Duration.ofMinutes(2))),
                  Arguments.of("a3",false, Instant.now(),Instant.now().plus(Duration.ofSeconds(3))),
                  Arguments.of("a4",false, Instant.now(),Instant.now().plus(Duration.ofSeconds(2))),

                  Arguments.of("b0",true, Instant.now(),null),
                  Arguments.of("b1",true, Instant.now(),Instant.now().minus(Duration.ofHours(2))),
                  Arguments.of("b2",true, Instant.now(),Instant.now().minus(Duration.ofMinutes(2))),
                  Arguments.of("b3",true, Instant.now(),Instant.now().minus(Duration.ofSeconds(1))),
                  Arguments.of("b4",true, Instant.now(),Instant.now().minus(Duration.ofMillis(1)))
            );
        }
        /* @formatter:on*/
    }
}
