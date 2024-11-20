// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.spring.security;

import org.springframework.beans.BeanInstantiationException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.client.RestTemplate;

/**
 * Abstract class that provides a base configuration for securing stateless web
 * applications inside the SecHub project using Spring Security.
 *
 * <p>
 * Using this class will enable Basic Authentication by username and password.
 * </p>
 *
 * <p>
 * This configuration also supports OAuth2-based authentication, either using
 * JWT tokens or opaque tokens. It integrates with the
 * {@link UserDetailsService} to provide user details and roles for
 * authorization.
 * </p>
 *
 * <p>
 * To enable OAuth2 authentication in JWT mode, you must set the following
 * property {@code sechub.security.oauth2.jwt.enabled=true} in the application
 * properties. For opaque token mode, set
 * {@code sechub.security.oauth2.opaque-token.enabled=true}. <b>Note:</b> This
 * configuration requires <b>exactly one</b> of the two modes to be enabled.
 * </p>
 *
 * <p>
 * Subclasses must implement the {@link #isOAuth2Enabled()} method to indicate
 * whether OAuth2 is enabled and the {@link #authorizeHttpRequests()} method to
 * configure API access permissions.
 * </p>
 *
 * @see org.springframework.security.config.annotation.web.builders.HttpSecurity
 * @see org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer
 * @see org.springframework.security.oauth2.jwt.JwtDecoder
 * @see org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector
 * @see org.springframework.security.oauth2.server.resource.web.BearerTokenResolver
 * @see org.springframework.security.web.SecurityFilterChain
 *
 * @author hamidonos
 */
public abstract class AbstractSecurityConfiguration {

    /* @formatter:off */
    @Bean
	SecurityFilterChain filterChain(HttpSecurity httpSecurity,
									@Autowired(required = false) UserDetailsService userDetailsService,
									RestTemplate restTemplate,
									@Autowired(required = false) OAuth2JwtProperties oAuth2JwtProperties,
									@Autowired(required = false) OAuth2OpaqueTokenProperties oAuth2OpaqueTokenProperties,
									@Autowired(required = false) JwtDecoder jwtDecoder) throws Exception {

		httpSecurity.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(authorizeHttpRequests())
				.csrf(AbstractHttpConfigurer::disable) // CSRF protection disabled. The CookieServerCsrfTokenRepository does
				// not work since Spring Boot 3
				.httpBasic(Customizer.withDefaults()).headers((headers) -> headers
						.contentSecurityPolicy((csp) -> csp.policyDirectives("default-src 'none'; style-src 'unsafe-inline'")));

		if (isOAuth2Enabled()) {
			if (userDetailsService == null) {
				throw new NoSuchBeanDefinitionException(UserDetailsService.class);
			}

			if ((oAuth2JwtProperties == null && oAuth2OpaqueTokenProperties == null) || (oAuth2JwtProperties != null && oAuth2OpaqueTokenProperties != null)) {
				throw new BeanInstantiationException(SecurityFilterChain.class, "Either jwt or opaque token mode must be enabled");
			}

			if (oAuth2JwtProperties != null) {
				if (jwtDecoder == null) {
					throw new NoSuchBeanDefinitionException(JwtDecoder.class);
				}
				BearerTokenResolver bearerTokenResolver = new DefaultBearerTokenResolver();
				AuthenticationProvider authenticationProvider = new OAuth2JwtAuthenticationProvider(userDetailsService, jwtDecoder);

				httpSecurity
						.oauth2ResourceServer(oauth2ResourceServer -> oauth2ResourceServer
								.jwt(jwt -> jwt.decoder(jwtDecoder))
								.bearerTokenResolver(bearerTokenResolver))
						.authenticationProvider(authenticationProvider);
			}

			if (oAuth2OpaqueTokenProperties != null) {
				OpaqueTokenIntrospector opaqueTokenIntrospector = new Base64OAuth2OpaqueTokenIntrospector(
						restTemplate,
						oAuth2OpaqueTokenProperties.getIntrospectionUri(),
						oAuth2OpaqueTokenProperties.getClientId(),
						oAuth2OpaqueTokenProperties.getClientSecret(),
						userDetailsService);

				httpSecurity
						.oauth2ResourceServer(oauth2ResourceServer -> oauth2ResourceServer
								.opaqueToken(opaqueToken -> opaqueToken.introspector(opaqueTokenIntrospector)));
			}
		}
		/* @formatter:on */

        return httpSecurity.build();
    }

    protected abstract boolean isOAuth2Enabled();

    protected abstract Customizer<AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry> authorizeHttpRequests();

}
