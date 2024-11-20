// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.spring.security;

import static org.mockito.Mockito.mock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.web.client.RestTemplate;

@Configuration
class TestSecurityConfiguration extends AbstractSecurityConfiguration {

    private final Environment environment;

    TestSecurityConfiguration(Environment environment) {
        this.environment = environment;
    }

    @Bean
    RestTemplate restTemplate() {
        return mock();
    }

    @Override
    protected boolean isOAuth2Enabled() {
        return environment.matchesProfiles("oauth2");
    }

    @Override
    protected Customizer<AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry> authorizeHttpRequests() {
        /* @formatter:off */
        return (auth) -> auth.
                requestMatchers("/api/admin" + "/**").hasAnyAuthority(TestRoles.SUPERADMIN).
                requestMatchers("/api/user"+ "/**").hasAnyAuthority(TestRoles.USER, TestRoles.SUPERADMIN).
                requestMatchers("/api/owner"+ "/**").hasAnyAuthority(TestRoles.OWNER, TestRoles.SUPERADMIN).
                requestMatchers("/api/anonymous"+ "/**").permitAll().
                requestMatchers("/**").denyAll();
        /* @formatter:on */
    }
}
