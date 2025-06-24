// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.spring.security;

import static org.mockito.Mockito.mock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.config.core.GrantedAuthorityDefaults;
import org.springframework.web.client.RestTemplate;

import com.mercedesbenz.sechub.commons.core.cache.CachePersistence;
import com.mercedesbenz.sechub.commons.core.shutdown.ApplicationShutdownHandler;

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

    @Bean
    RestTemplate restTemplate() {
        return mock();
    }

    @Bean
    GrantedAuthorityDefaults grantedAuthorityDefaults() {
        /*
         * In Spring Security, roles are prefixed with ROLE_ by default when performing
         * authorization checks. This will remove the default prefix ROLE_ from the
         * check.
         */
        return new GrantedAuthorityDefaults("");
    }

    @Bean
    ApplicationShutdownHandler applicationShutdownHandler() {
        return mock();
    }

    @Override
    protected Customizer<AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry> authorizeHttpRequests() {
        /* @formatter:off */
        return (auth) -> auth.
                requestMatchers("/api/admin" + "/**").hasAnyRole(TestRoles.SUPERADMIN).
                requestMatchers("/api/user"+ "/**").hasAnyRole(TestRoles.USER, TestRoles.SUPERADMIN).
                requestMatchers("/api/owner"+ "/**").hasAnyRole(TestRoles.OWNER, TestRoles.SUPERADMIN).
                requestMatchers("/api/anonymous"+ "/**").permitAll().
                requestMatchers("/**").denyAll();
        /* @formatter:on */
    }

    @Override
    protected CachePersistence<OAuth2OpaqueTokenIntrospectionResponse> getOAuth2OpaqueTokenClusterPersistence() {
        return null;
    }
}
