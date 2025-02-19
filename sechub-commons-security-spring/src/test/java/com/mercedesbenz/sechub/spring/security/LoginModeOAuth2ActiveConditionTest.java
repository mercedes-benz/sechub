// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.spring.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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

    @Test
    void matches_when_only_oauth2_mode_enabled_returns_true() {
        /* prepare */
        when(environment.getProperty("sechub.security.login.modes[0]")).thenReturn("oauth2");

        /* execute */
        boolean result = conditionToTest.matches(conditionContext, metadata);

        /* test */
        assertThat(result).isTrue();
    }

    @Test
    void matches_when_many_other_modes_and_oauth2_mode_enabled_returns_true() {
        /* prepare */
        when(environment.getProperty("sechub.security.login.modes[0]")).thenReturn("classic");
        when(environment.getProperty("sechub.security.login.modes[1]")).thenReturn("saml");
        when(environment.getProperty("sechub.security.login.modes[2]")).thenReturn("session");
        when(environment.getProperty("sechub.security.login.modes[3]")).thenReturn("ssh");
        when(environment.getProperty("sechub.security.login.modes[4]")).thenReturn("apiKey");
        when(environment.getProperty("sechub.security.login.modes[5]")).thenReturn("oauth2");

        /* execute */
        boolean result = conditionToTest.matches(conditionContext, metadata);

        /* test */
        assertThat(result).isTrue();
    }

    @Test
    void matches_when_no_mode_enabled_returns_false() {
        /* prepare */
        when(environment.getProperty("sechub.security.login.modes")).thenReturn(null);

        /* execute */
        boolean result = conditionToTest.matches(conditionContext, metadata);

        /* test */
        assertThat(result).isFalse();
    }

    @Test
    void matches_when_many_other_modes_and_no_oauth2_mode_enabled_returns_false() {
        /* prepare */
        when(environment.getProperty("sechub.security.login.modes[0]")).thenReturn("classic");
        when(environment.getProperty("sechub.security.login.modes[1]")).thenReturn("saml");
        when(environment.getProperty("sechub.security.login.modes[2]")).thenReturn("session");
        when(environment.getProperty("sechub.security.login.modes[3]")).thenReturn("ssh");
        when(environment.getProperty("sechub.security.login.modes[4]")).thenReturn("apiKey");

        /* execute */
        boolean result = conditionToTest.matches(conditionContext, metadata);

        /* test */
        assertThat(result).isFalse();
    }
}
