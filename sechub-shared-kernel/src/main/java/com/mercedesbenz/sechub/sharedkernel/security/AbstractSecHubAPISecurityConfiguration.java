// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.security;

import static com.mercedesbenz.sechub.sharedkernel.security.RoleConstants.*;

import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;
import org.springframework.security.web.SecurityFilterChain;

import com.mercedesbenz.sechub.sharedkernel.Profiles;

public abstract class AbstractSecHubAPISecurityConfiguration {

    /* @formatter:off */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity,
										   Environment environment,
										   @Autowired(required = false) OAuth2Properties oAuth2Properties,
										   @Autowired(required = false) UserDetailsService userDetailsService,
										   @Autowired(required = false) JwtDecoder jwtDecoder) throws Exception {

		httpSecurity.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests((auth) -> auth.
						requestMatchers(APIConstants.API_ADMINISTRATION + "**").hasAnyRole(ROLE_SUPERADMIN).
						requestMatchers(APIConstants.API_USER + "**").hasAnyRole(ROLE_USER, ROLE_SUPERADMIN).
						requestMatchers(APIConstants.API_PROJECT + "**").hasAnyRole(ROLE_USER, ROLE_SUPERADMIN).
						requestMatchers(APIConstants.API_OWNER + "**").hasAnyRole(ROLE_OWNER, ROLE_SUPERADMIN).

						requestMatchers(APIConstants.API_ANONYMOUS + "**").permitAll().
						requestMatchers(APIConstants.ERROR_PAGE).permitAll().
						requestMatchers(APIConstants.ACTUATOR + "**").permitAll().
						requestMatchers("/**").denyAll())
				.csrf(AbstractHttpConfigurer::disable) // CSRF protection disabled. The CookieServerCsrfTokenRepository does
				// not work since Spring Boot 3
				.httpBasic(Customizer.withDefaults()).headers((headers) -> headers
						.contentSecurityPolicy((csp) -> csp.policyDirectives("default-src 'none'; style-src 'unsafe-inline'")));

		if (environment.matchesProfiles(Profiles.OAUTH2)) {
			if (oAuth2Properties == null) {
				throw new NoSuchBeanDefinitionException(OAuth2Properties.class);
			}

			if (userDetailsService == null) {
				throw new NoSuchBeanDefinitionException(UserDetailsService.class);
			}

			if (jwtDecoder == null) {
				throw new NoSuchBeanDefinitionException(JwtDecoder.class);
			}

			AuthenticationProvider authenticationProvider = new OAuth2AuthenticationProvider(userDetailsService, jwtDecoder);
			BearerTokenResolver bearerTokenResolver = new DefaultBearerTokenResolver();

			httpSecurity
					.oauth2ResourceServer(oauth2ResourceServer -> oauth2ResourceServer
							.jwt(jwt -> jwt.jwkSetUri(oAuth2Properties.getJwkSetUri()))
							.bearerTokenResolver(bearerTokenResolver)
			).authenticationProvider(authenticationProvider);
		}
		/* @formatter:on */

        return httpSecurity.build();
    }
}
