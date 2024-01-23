// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.security;

import static com.mercedesbenz.sechub.pds.security.PDSRoleConstants.ROLE_SUPERADMIN;
import static com.mercedesbenz.sechub.pds.security.PDSRoleConstants.ROLE_USER;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

import com.mercedesbenz.sechub.pds.PDSAPIConstants;

@Configuration
@EnableWebSecurity
public class PDSAPISecurityConfiguration {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {

        /* @formatter:off */
		httpSecurity.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests((auth) -> auth.
						requestMatchers(PDSAPIConstants.API_JOB + "**").hasAnyRole(ROLE_USER, ROLE_SUPERADMIN).
						requestMatchers(PDSAPIConstants.API_ADMIN + "**").hasAnyRole(ROLE_USER, ROLE_SUPERADMIN).
						requestMatchers(PDSAPIConstants.API_ANONYMOUS+"**").permitAll().
						requestMatchers(PDSAPIConstants.ERROR_PAGE).permitAll().
						requestMatchers(PDSAPIConstants.ACTUATOR + "**").permitAll().
						requestMatchers("/**").denyAll())
				.csrf((csrf) -> csrf.disable()) // CSRF protection disabled. The CookieServerCsrfTokenRepository does
												// not work, since Spring Boot 3
				.httpBasic(Customizer.withDefaults()).headers((headers) -> headers
						.contentSecurityPolicy((csp) -> csp.policyDirectives("default-src 'none'")));
		/* @formatter:on */

        return httpSecurity.build();
    }

}
