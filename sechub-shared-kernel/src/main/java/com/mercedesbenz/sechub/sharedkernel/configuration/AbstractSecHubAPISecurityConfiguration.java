// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.configuration;

import static com.mercedesbenz.sechub.sharedkernel.RoleConstants.*;

import org.springframework.context.annotation.Bean;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

import com.mercedesbenz.sechub.sharedkernel.APIConstants;

public abstract class AbstractSecHubAPISecurityConfiguration {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {

        /* @formatter:off */
		httpSecurity.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests((auth) -> auth.
						requestMatchers(APIConstants.API_ADMINISTRATION + "**").hasAnyRole(ROLE_SUPERADMIN).
						requestMatchers(APIConstants.API_USER + "**").hasAnyRole(ROLE_USER, ROLE_SUPERADMIN).
						requestMatchers(APIConstants.API_PROJECT + "**").hasAnyRole(ROLE_USER, ROLE_SUPERADMIN).
						requestMatchers(APIConstants.API_OWNER + "**").hasAnyRole(ROLE_OWNER, ROLE_SUPERADMIN).
						requestMatchers(APIConstants.API_PROJECTS + "**").hasAnyRole(ROLE_USER, ROLE_OWNER, ROLE_SUPERADMIN).

						requestMatchers(APIConstants.API_ANONYMOUS + "**").permitAll().
						requestMatchers(APIConstants.ERROR_PAGE).permitAll().
						requestMatchers(APIConstants.ACTUATOR + "**").permitAll().
						requestMatchers("/**").denyAll())
				.csrf((csrf) -> csrf.disable()) // CSRF protection disabled. The CookieServerCsrfTokenRepository does
												// not work since Spring Boot 3
				.httpBasic(Customizer.withDefaults()).headers((headers) -> headers
						.contentSecurityPolicy((csp) -> csp.policyDirectives("default-src 'none'; style-src 'unsafe-inline'")));
		/* @formatter:on */

        return httpSecurity.build();
    }
}
