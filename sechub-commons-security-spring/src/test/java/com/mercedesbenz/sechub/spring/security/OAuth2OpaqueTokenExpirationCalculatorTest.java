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

class OAuth2OpaqueTokenExpirationCalculatorTest {

    private OAuth2OpaqueTokenExpirationCalculator calculatorToTest;

    @BeforeEach
    void beforeEach() throws Exception {
        calculatorToTest = new OAuth2OpaqueTokenExpirationCalculator();
    }

    @Test
    void isExpired_throws_illegal_argument_exception_if_now_is_null() {
        /* prepare */
        OAuth2OpaqueTokenIntrospectionResponse response = mock();

        /* execute + test */
        assertThatThrownBy(() -> calculatorToTest.isExpired(response, null)).isInstanceOf(NullPointerException.class).hasMessageContaining("now");
    }

    @Test
    void isExpired_throws_illegal_argument_exception_if_now_is_null_() {
        assertThatThrownBy(() -> calculatorToTest.isExpired(null, Instant.now())).isInstanceOf(NullPointerException.class).hasMessageContaining("response");
    }

    @ParameterizedTest
    @ArgumentsSource(ExpirationArgumentsProvider.class)
    void isExpired(String variant, boolean shallBeExpired, Instant now, Instant expiresAt) {

        /* prepare */
        OAuth2OpaqueTokenIntrospectionResponse response = mock();
        when(response.getExpiresAt()).thenReturn(expiresAt);

        /* execute */
        boolean expired = calculatorToTest.isExpired(response, now);

        /* test */
        assertThat(expired).isEqualTo(shallBeExpired);
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
