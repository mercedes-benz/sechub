// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.spring.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.test.context.TestPropertySource;

import com.mercedesbenz.sechub.testframework.spring.YamlPropertyLoaderFactory;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-login-mode-oauth2-active-condition-test.yaml", factory = YamlPropertyLoaderFactory.class)
class LoginModeOAuth2ActiveConditionTest {

    private static final ConditionContext conditionContext = mock();
    private static final Environment environment = mock();
    private static final AnnotatedTypeMetadata metadata = mock();
    private static final LoginModeOAuth2ActiveCondition conditionToTest = new LoginModeOAuth2ActiveCondition();

    private final ApplicationContext applicationContext;

    @Autowired
    LoginModeOAuth2ActiveConditionTest(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @BeforeEach
    void beforeEach() {
        reset(conditionContext);
        when(conditionContext.getEnvironment()).thenReturn(environment);
    }

    @Test
    void matches_with_valid_properties_source_works() {
        /* test */
        assertThat(applicationContext.getBean("randomBean")).isNotNull();
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
        when(environment.getProperty("sechub.security.login.modes[0]")).thenReturn(null);

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

    @Configuration
    static class TestConfig {

        @Bean
        @Conditional(LoginModeOAuth2ActiveCondition.class)
        Object randomBean() {
            return new Object();
        }

    }
}
