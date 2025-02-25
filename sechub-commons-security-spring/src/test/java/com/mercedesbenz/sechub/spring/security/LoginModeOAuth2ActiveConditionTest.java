// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.spring.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;

class LoginModeOAuth2ActiveConditionTest {

    private static final ConditionContext conditionContext = mock();
    private static final Environment environment = mock();
    private static final AnnotatedTypeMetadata metadata = mock();
    private static final LoginModeOAuth2ActiveCondition conditionToTest = new LoginModeOAuth2ActiveCondition();

    @BeforeEach
    void beforeEach() {
        reset(conditionContext, environment);
        when(conditionContext.getEnvironment()).thenReturn(environment);
    }

    @ParameterizedTest
    @ValueSource(strings = { "oauth2","classic,oauth2","oauth2, classic", "classic,saml,session,ssh,apiKey,oauth2", "classic,saml,oauth2,session,ssh,apiKey" })
    void returns_true_when_any_listed_mode_is_oauth2(String value) {
        /* prepare */
        when(environment.getProperty("sechub.security.login.modes")).thenReturn(value);

        /* execute */
        boolean result = conditionToTest.matches(conditionContext, metadata);

        /* test */
        assertThat(result).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = { "classic,saml,session,ssh,apiKey", "classic", "something-else" })
    @EmptySource
    @NullSource
    void returns_false_when_no_listed_mode_is_oauth2(String value) {
        /* prepare */
        when(environment.getProperty("sechub.security.login.modes")).thenReturn(value);

        /* execute */
        boolean result = conditionToTest.matches(conditionContext, metadata);

        /* test */
        assertThat(result).isFalse();
    }
}
