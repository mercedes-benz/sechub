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

/* @formatter:off */
/**
 * The <code>TestSecurityConfiguration</code> class extends the {@link AbstractSecurityConfiguration} and provides
 * security configuration for testing purposes. It defines security rules for different test API paths.
 *
 * <p>
 * The following API paths are configured:
 * </p>
 * <ul>
 *   <li><code>/api/admin/**</code> - Accessible only to users having the <code>SUPERADMIN</code> authority.</li>
 *   <li><code>/api/user/**</code> - Accessible to users having the <code>USER</code> or <code>SUPERADMIN</code> authority.</li>
 *   <li><code>/api/owner/**</code> - Accessible to users with having the <code>OWNER</code> or <code>SUPERADMIN</code> authority.</li>
 *   <li><code>/api/anonymous/**</code> - Accessible to all users without authentication.</li>
 *   <li><code>/</code> - All other paths are denied access.</li>
 * </ul>
 *
 * @see AbstractSecurityConfiguration
 *
 * @author hamidonos
 */
/* @formatter:on */
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
