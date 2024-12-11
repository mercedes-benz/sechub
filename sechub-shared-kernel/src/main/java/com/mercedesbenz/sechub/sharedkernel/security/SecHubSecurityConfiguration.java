// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;

import com.mercedesbenz.sechub.sharedkernel.Profiles;
import com.mercedesbenz.sechub.spring.security.AbstractSecurityConfiguration;

@Configuration
@EnableMethodSecurity(jsr250Enabled = true)
@EnableWebSecurity
public class SecHubSecurityConfiguration extends AbstractSecurityConfiguration {

    private final Environment environment;

    SecHubSecurityConfiguration(Environment environment) {
        this.environment = environment;
    }

    @Override
    protected boolean isOAuth2Enabled() {
        return environment.matchesProfiles(Profiles.OAUTH2);
    }

    @Override
    protected Customizer<AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry> authorizeHttpRequests() {
        /* @formatter:off */
        return (auth) -> auth
                .requestMatchers(APIConstants.API_ADMINISTRATION + "**").hasAnyRole(RoleConstants.ROLE_SUPERADMIN)
                .requestMatchers(APIConstants.API_USER + "**").hasAnyRole(RoleConstants.ROLE_USER, RoleConstants.ROLE_SUPERADMIN)
                .requestMatchers(APIConstants.API_PROJECT + "**").hasAnyRole(RoleConstants.ROLE_USER, RoleConstants.ROLE_SUPERADMIN)
                .requestMatchers(APIConstants.API_OWNER + "**").hasAnyRole(RoleConstants.ROLE_OWNER, RoleConstants.ROLE_SUPERADMIN)
                .requestMatchers(APIConstants.API_ANONYMOUS + "**").permitAll()
                .requestMatchers(APIConstants.ERROR_PAGE).permitAll()
                .requestMatchers(APIConstants.ACTUATOR + "**").permitAll()
                .requestMatchers("/**").denyAll();
        /* @formatter:on */
    }
}
