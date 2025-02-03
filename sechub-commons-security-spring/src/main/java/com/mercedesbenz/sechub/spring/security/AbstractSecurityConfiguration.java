// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.spring.security;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanInstantiationException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
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
import org.springframework.security.web.context.SecurityContextHolderFilter;
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
@EnableConfigurationProperties(SecHubSecurityProperties.class)
public abstract class AbstractSecurityConfiguration {
	static final String CLASSIC_AUTH_COOKIE_NAME = "SECHUB_CLASSIC_AUTH_CREDENTIALS";
	static final String OAUTH2_COOKIE_NAME = "SECHUB_OAUTH2_ACCESS_TOKEN";
	static final String BASE_PATH = "/";

    private static final Logger LOG = LoggerFactory.getLogger(AbstractSecurityConfiguration.class);
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
														  SecHubSecurityProperties secHubSecurityProperties,
														  @Autowired(required = false) UserDetailsService userDetailsService,
														  RestTemplate restTemplate,
														  @Autowired(required = false) AES256Encryption aes256Encryption,
														  @Autowired(required = false) JwtDecoder jwtDecoder) throws Exception {
		configureResourceServerSecurityMatcher(httpSecurity, secHubSecurityProperties.getLoginProperties());

		httpSecurity
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(authorizeHttpRequests())
				.exceptionHandling(exceptionHandling -> exceptionHandling
						/* Unauthorized requests will return a 401 status code */
						.authenticationEntryPoint((request, response, authException) -> response.sendError(HttpStatus.UNAUTHORIZED.value(), HttpStatus.UNAUTHORIZED.getReasonPhrase()))
				)
				/* CSRF protection disabled. The CookieServerCsrfTokenRepository does not work since Spring Boot 3 */
				.csrf(AbstractHttpConfigurer::disable)
				.headers((headers) -> headers.contentSecurityPolicy((csp) -> csp.policyDirectives("default-src 'none'; style-src 'unsafe-inline'")));

		configureResourceServerMode(httpSecurity, secHubSecurityProperties.getResourceServerProperties(), userDetailsService, aes256Encryption, jwtDecoder, restTemplate);

        return httpSecurity.build();
    }
	/* @formatter:on */

    @Bean
    @Conditional(LoginOAuth2EnabledCondition.class)
    ClientRegistrationRepository clientRegistrationRepository(SecHubSecurityProperties secHubSecurityProperties) {
        SecHubSecurityProperties.LoginProperties login = secHubSecurityProperties.getLoginProperties();
        SecHubSecurityProperties.LoginProperties.OAuth2Properties oAuth2 = login.getOAuth2Properties();

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
												 SecHubSecurityProperties secHubSecurityProperties,
												 RestTemplate restTemplate,
												 AES256Encryption aes256Encryption,
												 @Autowired(required = false) OAuth2AuthorizedClientService oAuth2AuthorizedClientService) throws Exception {
		SecHubSecurityProperties.LoginProperties loginProperties = secHubSecurityProperties.getLoginProperties();

		Set<String> publicPaths = new HashSet<>(DEFAULT_PUBLIC_PATHS);
		publicPaths.add(loginProperties.getLoginPage());

		httpSecurity.securityMatcher(publicPaths.toArray(new String[0]))
				/* Disable CSRF */
				.csrf(AbstractHttpConfigurer::disable)

				/* Make the application stateless */
				.sessionManagement(httpSecuritySessionManagementConfigurer -> httpSecuritySessionManagementConfigurer
						.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

		if (!loginProperties.isOAuth2ModeEnabled() && !loginProperties.isClassicModeEnabled()) {
			String exMsg = "At least one of 'classic' or 'oauth2' mode must be enabled by setting the '%s.%s' property".formatted(
					SecHubSecurityProperties.LoginProperties.PREFIX,
					SecHubSecurityProperties.LoginProperties.MODES
			);
			/* @formatter:on */

            throw new BeanInstantiationException(SecurityFilterChain.class, exMsg);
        }
        if (loginProperties.isOAuth2ModeEnabled()) {
            /*
             * Note:
             *
             * This will set the default login page to the oauth2 login page. Spring will
             * always use the default login page of the first configured login mode.
             */
            configureLoginOAuth2Mode(httpSecurity, loginProperties, restTemplate, aes256Encryption, oAuth2AuthorizedClientService);
        }

        if (loginProperties.isClassicModeEnabled()) {
            configureLoginClassicMode(httpSecurity, loginProperties, aes256Encryption);
        }

        /* @formatter:on */

        return httpSecurity.build();
    }

    protected abstract Customizer<AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry> authorizeHttpRequests();

    /**
     * Configures the security matcher for the resource server.
     * <p>
     * By default the http security config of the resource server will apply to all
     * paths. If the login is enabled, the login page and public paths will be
     * excluded from the security matcher.
     * </p>
     *
     * @param httpSecurity    the {@link HttpSecurity} object to configure
     * @param loginProperties the SecHub security login properties
     * @see HttpSecurity#securityMatcher(RequestMatcher)
     */
    private static void configureResourceServerSecurityMatcher(HttpSecurity httpSecurity, SecHubSecurityProperties.LoginProperties loginProperties) {
        if (loginProperties == null || !loginProperties.isEnabled()) {
            return;
        }

        Set<String> publicPaths = new HashSet<>(DEFAULT_PUBLIC_PATHS);
        publicPaths.add(loginProperties.getLoginPage());
        RequestMatcher publicPathsMatcher = new OrRequestMatcher(publicPaths.stream().map(AntPathRequestMatcher::new).toArray(AntPathRequestMatcher[]::new));
        RequestMatcher protectedPathsMatcher = new NegatedRequestMatcher(publicPathsMatcher);
        httpSecurity.securityMatcher(protectedPathsMatcher);
    }

    /* @formatter:off */
    private static void configureResourceServerMode(HttpSecurity httpSecurity,
													SecHubSecurityProperties.ResourceServerProperties resourceServerProperties,
													UserDetailsService userDetailsService,
													AES256Encryption aes256Encryption,
													JwtDecoder jwtDecoder,
													RestTemplate restTemplate) throws Exception {

		if (resourceServerProperties == null) {
			/*
			 * This is useful for testing purposes where the security layer is mocked or if you need disable all authentication
			 * modes for any reason. Note that the application is still protected but all requests to protected paths will be
			 * rejected.
			 */
			LOG.warn("No resource server configuration detected. All requests to protected paths will be rejected.");
			return;
		}

		if (!resourceServerProperties.isClassicModeEnabled() && !resourceServerProperties.isOAuth2ModeEnabled()) {
			String exMsg = "At least one of 'classic' or 'oauth2' mode must be enabled by setting the '%s.%s' property".formatted(
					SecHubSecurityProperties.ResourceServerProperties.PREFIX,
					SecHubSecurityProperties.ResourceServerProperties.MODES
			);
			/* @formatter:on */

            throw new BeanInstantiationException(SecurityFilterChain.class, exMsg);
        }

        if (resourceServerProperties.isClassicModeEnabled()) {
            configureResourceServerClassicMode(httpSecurity, aes256Encryption);
        }

        if (resourceServerProperties.isOAuth2ModeEnabled()) {
            configureResourceServerOAuth2Mode(httpSecurity, resourceServerProperties.getOAuth2Properties(), userDetailsService, aes256Encryption, jwtDecoder,
                    restTemplate);
        }
    }

    private static void configureResourceServerClassicMode(HttpSecurity httpSecurity, AES256Encryption aes256Encryption) throws Exception {
		ClassicAuthCredentialsCookieFilter classicAuthCredentialsCookieFilter = new ClassicAuthCredentialsCookieFilter(aes256Encryption);

		/* @formatter:off */
		httpSecurity
				.httpBasic(Customizer.withDefaults())
				.addFilterBefore(classicAuthCredentialsCookieFilter, SecurityContextHolderFilter.class);
		/* @formatter:on */
    }

    /* @formatter:off */
	private static void configureResourceServerOAuth2Mode(HttpSecurity httpSecurity,
														  SecHubSecurityProperties.ResourceServerProperties.OAuth2Properties oAuth2Properties,
														  UserDetailsService userDetailsService,
														  AES256Encryption aes256Encryption,
														  JwtDecoder jwtDecoder,
														  RestTemplate restTemplate) throws Exception {

		if (oAuth2Properties.isJwtModeEnabled() == oAuth2Properties.isOpaqueTokenModeEnabled()) {
			String exMsg = "Either 'jwt' or opaque token mode must be enabled by setting the '%s.%s' property to either '%s' or '%s'".formatted(
					SecHubSecurityProperties.ResourceServerProperties.OAuth2Properties.PREFIX,
					SecHubSecurityProperties.ResourceServerProperties.OAuth2Properties.MODE,
					SecHubSecurityProperties.ResourceServerProperties.OAuth2Properties.OAUTH2_JWT_MODE,
					SecHubSecurityProperties.ResourceServerProperties.OAuth2Properties.OAUTH2_OPAQUE_TOKEN_MODE
			);

			throw new BeanInstantiationException(SecurityFilterChain.class, exMsg);
		}

		if (oAuth2Properties.isJwtModeEnabled()) {
			configureResourceServerOAuth2JwtMode(httpSecurity, userDetailsService, jwtDecoder, aes256Encryption);
		}

		if (oAuth2Properties.isOpaqueTokenModeEnabled()) {
			configureResourceServerOAuth2OpaqueTokenMode(httpSecurity, oAuth2Properties.getOpaqueTokenProperties(), userDetailsService, restTemplate, aes256Encryption);
		}
	}
	/* @formatter:on */

    /* @formatter:off */
	private static void configureResourceServerOAuth2JwtMode(HttpSecurity httpSecurity,
															 UserDetailsService userDetailsService,
															 JwtDecoder jwtDecoder,
															 AES256Encryption aes256Encryption) throws Exception {

		if (userDetailsService == null) {
			throw new NoSuchBeanDefinitionException(UserDetailsService.class);
		}

		if (jwtDecoder == null) {
			throw new NoSuchBeanDefinitionException(JwtDecoder.class);
		}

		AuthenticationProvider authenticationProvider = new OAuth2JwtAuthenticationProvider(userDetailsService, jwtDecoder);

		if (aes256Encryption == null) {
			throw new NoSuchBeanDefinitionException(AES256Encryption.class);
		}

		BearerTokenResolver bearerTokenResolver = new DynamicBearerTokenResolver(aes256Encryption);

		httpSecurity
				.oauth2ResourceServer(oauth2ResourceServer -> oauth2ResourceServer
						.jwt(jwt -> jwt.decoder(jwtDecoder))
						.bearerTokenResolver(bearerTokenResolver)
				)
				.authenticationProvider(authenticationProvider);
	}
	/* @formatter:on */

    /* @formatter:off */
	private static void configureResourceServerOAuth2OpaqueTokenMode(HttpSecurity httpSecurity,
																	 SecHubSecurityProperties.ResourceServerProperties.OAuth2Properties.OpaqueTokenProperties opaqueTokenProperties,
																	 UserDetailsService userDetailsService,
																	 RestTemplate restTemplate,
																	 AES256Encryption aes256Encryption) throws Exception {

		if (userDetailsService == null) {
			throw new NoSuchBeanDefinitionException(UserDetailsService.class);
		}

		if (restTemplate == null) {
			throw new NoSuchBeanDefinitionException(RestTemplate.class);
		}

		OpaqueTokenIntrospector opaqueTokenIntrospector = new OAuth2OpaqueTokenIntrospector(
				restTemplate,
				opaqueTokenProperties.getIntrospectionUri(),
				opaqueTokenProperties.getClientId(),
				opaqueTokenProperties.getClientSecret(),
				userDetailsService);

		if (aes256Encryption == null) {
			throw new NoSuchBeanDefinitionException(AES256Encryption.class);
		}

		BearerTokenResolver bearerTokenResolver = new DynamicBearerTokenResolver(aes256Encryption);

		httpSecurity
				.oauth2ResourceServer(oauth2ResourceServer -> oauth2ResourceServer
						.opaqueToken(opaqueTokenConfigurer -> opaqueTokenConfigurer.introspector(opaqueTokenIntrospector))
						.bearerTokenResolver(bearerTokenResolver)
				);
		}
	/* @formatter:on */

    /* @formatter:off */
	private static void configureLoginOAuth2Mode(HttpSecurity httpSecurity,
												 SecHubSecurityProperties.LoginProperties loginProperties,
												 RestTemplate restTemplate,
												 AES256Encryption aes256Encryption,
												 OAuth2AuthorizedClientService oAuth2AuthorizedClientService) throws Exception {
		SecHubSecurityProperties.LoginProperties.OAuth2Properties loginOAuth2Properties = loginProperties.getOAuth2Properties();

		if (restTemplate == null) {
			throw new NoSuchBeanDefinitionException(RestTemplate.class);
		}

		LoginOAuth2AccessTokenClient loginOAuth2AccessTokenClient = new LoginOAuth2AccessTokenClient(restTemplate);

		if (oAuth2AuthorizedClientService == null) {
			throw new NoSuchBeanDefinitionException(OAuth2AuthorizedClientService.class);
		}

		if (aes256Encryption == null) {
			throw new NoSuchBeanDefinitionException(AES256Encryption.class);
		}

		AuthenticationSuccessHandler authenticationSuccessHandler = new LoginOAuth2SuccessHandler(loginOAuth2Properties.getProvider(), oAuth2AuthorizedClientService,
				aes256Encryption, loginProperties.getRedirectUri());

		/* Enable OAuth2 */
		httpSecurity.oauth2Login(oauth2 -> oauth2.loginPage(loginProperties.getLoginPage())
				.tokenEndpoint(token -> token.accessTokenResponseClient(loginOAuth2AccessTokenClient))
				.successHandler(authenticationSuccessHandler));
	}
	/* @formatter:on */

    private static void configureLoginClassicMode(HttpSecurity httpSecurity,
												  SecHubSecurityProperties.LoginProperties loginProperties,
												  AES256Encryption aes256Encryption) throws Exception {
		AuthenticationSuccessHandler authenticationSuccessHandler = new LoginClassicSuccessHandler(loginProperties.getRedirectUri(), aes256Encryption);
		String loginPage = loginProperties.getLoginPage();
		/* @formatter:off */
		httpSecurity.formLogin(form -> form
				.loginPage(loginPage)
				.successHandler(authenticationSuccessHandler)
				.failureUrl("%s?tab=classic&error=true&errorMsg=Invalid User ID or API Token".formatted(loginPage))
		);
		/* @formatter:on */
    }
}
