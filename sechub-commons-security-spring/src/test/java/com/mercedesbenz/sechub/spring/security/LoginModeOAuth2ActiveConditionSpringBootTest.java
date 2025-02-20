// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.spring.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.TestPropertySource;

import com.mercedesbenz.sechub.testframework.spring.YamlPropertyLoaderFactory;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-login-mode-oauth2-active-condition-test.yaml", factory = YamlPropertyLoaderFactory.class)
class LoginModeOAuth2ActiveConditionSpringBootTest {

    private final ApplicationContext applicationContext;

    @Autowired
    LoginModeOAuth2ActiveConditionSpringBootTest(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Test
    void LoginModeOAuth2ActiveCondition_works_with_oauth2_mode_enabled_in_property_source() {
        /* test */
        assertThat(applicationContext.getBean("randomBean")).isNotNull();
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
