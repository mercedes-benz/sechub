// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.spring.security;

import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.BeanInstantiationException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.NegatedRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
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
 * @see org.springframework.security.config.annotation.web.builders.HttpSecurity
 * @see org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer
 * @see org.springframework.security.oauth2.jwt.JwtDecoder
 * @see org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector
 * @see org.springframework.security.oauth2.server.resource.web.BearerTokenResolver
 * @see org.springframework.security.web.SecurityFilterChain
 *
 * @author hamidonos
 */
@EnableConfigurationProperties(SecurityProperties.class)
public abstract class AbstractSecurityConfiguration {

    static final String ACCESS_TOKEN = "access_token";
    static final String SERVER_OAUTH2_PROPERTIES_PREFIX = "sechub.security.server.oauth2";;
    static final String MODE = "mode";

    private static final String SCOPE = "openid";
    private static final String SUBJECT = "sub";
    /* @formatter:off */
	private static final Set<String> DEFAULT_PUBLIC_PATHS = Set.of(
			"/css/**",
			"/js/**",
			"/images/**",
			"/login/oauth2/**",
			"/oauth2/**",
			"/favicon.ico",
			"/sechub-logo.svg"
	);
	/* @formatter:on */

    /* @formatter:off */
    @Bean
	@Order(1)
	SecurityFilterChain securityFilterChainResourceServer(HttpSecurity httpSecurity,
														  SecurityProperties securityProperties,
														  @Autowired(required = false) UserDetailsService userDetailsService,
														  RestTemplate restTemplate,
														  @Autowired(required = false) AES256Encryption aes256Encryption,
														  @Autowired(required = false) JwtDecoder jwtDecoder) throws Exception {
		SecurityProperties.Login login = securityProperties.getLogin();

		if (login != null && login.isEnabled()) {
			Set<String> publicPaths = new HashSet<>(DEFAULT_PUBLIC_PATHS);
			publicPaths.add(login.getLoginPage());

			RequestMatcher publicPathsMatcher = new OrRequestMatcher(
					publicPaths.stream()
							.map(AntPathRequestMatcher::new)
							.toArray(AntPathRequestMatcher[]::new));

			RequestMatcher protectedPathsMatcher = new NegatedRequestMatcher(publicPathsMatcher);

			httpSecurity.securityMatcher(protectedPathsMatcher);
		}

		httpSecurity
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(authorizeHttpRequests())
				.csrf(AbstractHttpConfigurer::disable) // CSRF protection disabled. The CookieServerCsrfTokenRepository does
				// not work since Spring Boot 3
				.httpBasic(Customizer.withDefaults()).headers((headers) -> headers
						.contentSecurityPolicy((csp) -> csp.policyDirectives("default-src 'none'; style-src 'unsafe-inline'")));

		SecurityProperties.Server server = securityProperties.getServer();

		if (server != null && server.isOAuth2ModeEnabled()) {
			if (userDetailsService == null) {
				throw new NoSuchBeanDefinitionException(UserDetailsService.class);
			}

			SecurityProperties.Server.OAuth2 oAuth2 = server.getOAuth2();

			if (oAuth2.isJwtModeEnabled() == oAuth2.isOpaqueTokenModeEnabled()) {
				String exMsg = "Either 'jwt' or opaque token mode must be enabled by setting the '%s.%s' property to either '%s' or '%s'".formatted(
						SERVER_OAUTH2_PROPERTIES_PREFIX,
						MODE,
						SecurityProperties.Server.OAuth2.OAUTH2_JWT_MODE,
						SecurityProperties.Server.OAuth2.OAUTH2_OPAQUE_TOKEN_MODE
				);

				throw new BeanInstantiationException(SecurityFilterChain.class, exMsg);
			}

			if (aes256Encryption == null) {
				throw new NoSuchBeanDefinitionException(AES256Encryption.class);
			}

			BearerTokenResolver bearerTokenResolver = new DynamicBearerTokenResolver(aes256Encryption);

			if (oAuth2.isJwtModeEnabled()) {
				if (jwtDecoder == null) {
					throw new NoSuchBeanDefinitionException(JwtDecoder.class);
				}
				AuthenticationProvider authenticationProvider = new OAuth2JwtAuthenticationProvider(userDetailsService, jwtDecoder);

				httpSecurity
						.oauth2ResourceServer(oauth2ResourceServer -> oauth2ResourceServer
                                    .jwt(jwt -> jwt.decoder(jwtDecoder))
                                    .bearerTokenResolver(bearerTokenResolver)
						)
						.authenticationProvider(authenticationProvider);
			}

			if (oAuth2.isOpaqueTokenModeEnabled()) {
				SecurityProperties.Server.OAuth2.OpaqueToken opaqueToken = oAuth2.getOpaqueToken();
				OpaqueTokenIntrospector opaqueTokenIntrospector = new OAuth2OpaqueTokenIntrospector(
						restTemplate,
						opaqueToken.getIntrospectionUri(),
						opaqueToken.getClientId(),
						opaqueToken.getClientSecret(),
						userDetailsService);

				httpSecurity
						.oauth2ResourceServer(oauth2ResourceServer -> oauth2ResourceServer
								.opaqueToken(opaqueTokenConfigurer -> opaqueTokenConfigurer.introspector(opaqueTokenIntrospector))
								.bearerTokenResolver(bearerTokenResolver)
						);
			}
		}
		/* @formatter:on */

        return httpSecurity.build();
    }

    @Bean
    @Conditional(LoginEnabledCondition.class)
    ClientRegistrationRepository clientRegistrationRepository(SecurityProperties securityProperties) {
        SecurityProperties.Login login = securityProperties.getLogin();
        SecurityProperties.Login.OAuth2 oAuth2 = login.getOAuth2();

        /* @formatter:off */
		ClientRegistration clientRegistration = ClientRegistration
				.withRegistrationId(oAuth2.getProvider())
				.clientId(oAuth2.getClientId())
				.clientSecret(oAuth2.getClientSecret())
				.authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
				.redirectUri(oAuth2.getRedirectUri())
				.issuerUri(oAuth2.getIssuerUri())
				.scope(SCOPE)
				.authorizationUri(oAuth2.getAuthorizationUri())
				.tokenUri(oAuth2.getTokenUri())
				.userInfoUri(oAuth2.getUserInfoUri())
				.userNameAttributeName(SUBJECT)
				.jwkSetUri(oAuth2.getJwkSetUri())
				.build();
		/* @formatter:on */

        return new InMemoryClientRegistrationRepository(clientRegistration);
    }

    @Bean
    @Conditional(LoginEnabledCondition.class)
    @Order(2)
    /* @formatter:off */
	SecurityFilterChain securityFilterChainLogin(HttpSecurity httpSecurity,
												 @Autowired(required = false) SecurityProperties securityProperties,
												 @Autowired(required = false) AES256Encryption aes256Encryption,
												 @Autowired(required = false) OAuth2AuthorizedClientService oAuth2AuthorizedClientService) throws Exception {
		SecurityProperties.Login login = securityProperties.getLogin();

		Set<String> publicPaths = new HashSet<>(DEFAULT_PUBLIC_PATHS);
		publicPaths.add(login.getLoginPage());

		httpSecurity.securityMatcher(publicPaths.toArray(new String[0]))
				/* Disable CSRF */
				.csrf(AbstractHttpConfigurer::disable)
				/* Make the application stateless */
				.sessionManagement(httpSecuritySessionManagementConfigurer -> httpSecuritySessionManagementConfigurer
						.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

		if (login.isOAuth2ModeEnabled()) {
			SecurityProperties.Login.OAuth2 loginOAuth2 = login.getOAuth2();
			RestTemplate restTemplate = new RestTemplate();
			LoginOAuth2AccessTokenClient loginOAuth2AccessTokenClient = new LoginOAuth2AccessTokenClient(restTemplate);
			if (oAuth2AuthorizedClientService == null) {
				throw new NoSuchBeanDefinitionException(OAuth2AuthorizedClientService.class);
			}
			if (aes256Encryption == null) {
				throw new NoSuchBeanDefinitionException(AES256Encryption.class);
			}
			AuthenticationSuccessHandler authenticationSuccessHandler = new LoginOAuth2SuccessHandler(loginOAuth2.getProvider(), oAuth2AuthorizedClientService,
					aes256Encryption, login.getRedirectUri());
			/* Enable OAuth2 */
			httpSecurity.oauth2Login(oauth2 -> oauth2.loginPage(login.getLoginPage())
					.tokenEndpoint(token -> token.accessTokenResponseClient(loginOAuth2AccessTokenClient))
					.successHandler(authenticationSuccessHandler));
		}

		if (login.isClassicModeEnabled()) {
			/*
			 * Enable Classic Authentication
			 *
			 * Note: This must be the last configuration in
			 * order to set the default 'loginPage' to oAuth2 because spring uses the
			 * 'loginPage' from the first authentication method configured
			 */
			AuthenticationSuccessHandler authenticationSuccessHandler = new LoginClassicSuccessHandler(login.getRedirectUri());
			httpSecurity.formLogin(form -> form.loginPage(login.getLoginPage()).successHandler(authenticationSuccessHandler));
		}

		/* @formatter:on */

        return httpSecurity.build();
    }

    protected abstract Customizer<AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry> authorizeHttpRequests();

}
