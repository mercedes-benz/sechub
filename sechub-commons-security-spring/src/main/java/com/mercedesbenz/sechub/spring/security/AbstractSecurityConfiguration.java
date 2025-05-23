// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.spring.security;

import java.time.Duration;
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
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;
import org.springframework.security.web.context.SecurityContextHolderFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.NegatedRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.client.RestTemplate;

import com.mercedesbenz.sechub.commons.core.cache.CachePersistence;
import com.mercedesbenz.sechub.commons.core.cache.InMemoryCachePersistence;
import com.mercedesbenz.sechub.commons.core.shutdown.ApplicationShutdownHandler;

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
			"/login/**",
			"/oauth2/**"
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
														  @Autowired(required = false) JwtDecoder jwtDecoder,
														  ApplicationShutdownHandler applicationShutdownHandler,
														  @Autowired(required = false) OAuth2TokenExpirationCalculator expirationCalculator,
														  @Autowired(required = false) OAuth2OpaqueTokenIntrospectionResponseCryptoAccessProvider cryptoAccessprovider) throws Exception {
        LOG.debug("Setup security filter chain for ressource server");
		SecHubSecurityProperties.LoginProperties loginProperties = secHubSecurityProperties.getLoginProperties();
		configureResourceServerSecurityMatcher(httpSecurity, loginProperties);

		httpSecurity
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(authorizeHttpRequests())
				.exceptionHandling(exceptionHandling -> exceptionHandling
						/* Forbidden requests will return a 403 status code */
						.accessDeniedHandler((request, response, accessDeniedException) -> response.sendError(HttpStatus.FORBIDDEN.value(), HttpStatus.FORBIDDEN.getReasonPhrase()))
						/* Unauthorized requests will return a 401 status code */
						.authenticationEntryPoint((request, response, authException) -> {

						            /* clear any existing oauth2 cookie if there is an authentication error - e.g. an expired token */
        						    CookieHelper.removeCookie(response, OAUTH2_COOKIE_NAME);

        						    response.sendError(HttpStatus.UNAUTHORIZED.value(), HttpStatus.UNAUTHORIZED.getReasonPhrase());
						        })
				)
				/* CSRF protection disabled. The CookieServerCsrfTokenRepository does not work since Spring Boot 3 */
				.csrf(AbstractHttpConfigurer::disable)
				.headers((headers) -> headers.contentSecurityPolicy((csp) -> csp.policyDirectives("default-src 'none'; style-src 'unsafe-inline'")));

		configureLogout(httpSecurity, loginProperties);

		/* Configure the resource server */
		configureResourceServerSecurityMatcher(httpSecurity, loginProperties);
		configureResourceServerMode(
				httpSecurity,
				secHubSecurityProperties,
				userDetailsService,
				aes256Encryption,
				jwtDecoder,
				restTemplate,
				applicationShutdownHandler,
				expirationCalculator,
				cryptoAccessprovider);

        return httpSecurity.build();
    }

	/* @formatter:on */

    @Bean
    @Conditional(LoginModeOAuth2ActiveCondition.class)
    ClientRegistrationRepository clientRegistrationRepository(SecHubSecurityProperties sechubSecurityProperties) {
        SecHubSecurityProperties.LoginProperties login = sechubSecurityProperties.getLoginProperties();
        SecHubSecurityProperties.LoginProperties.OAuth2Properties oAuth2 = login.getOAuth2Properties();

        LOG.debug("Provide oauth2 client registry");
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
												 SecHubSecurityProperties sechubSecurityProperties,
												 RestTemplate restTemplate,
												 AES256Encryption aes256Encryption,
												 @Autowired(required = false) OAuth2AuthorizedClientService oAuth2AuthorizedClientService,
												 OAuth2TokenExpirationCalculator expirationCalculator) throws Exception {
        LOG.debug("Setup security filter chain for login");

        SecHubSecurityProperties.LoginProperties loginProperties = sechubSecurityProperties.getLoginProperties();

		Set<String> publicPaths = new HashSet<>(DEFAULT_PUBLIC_PATHS);
		publicPaths.add(loginProperties.getLoginPage());

		httpSecurity.securityMatcher(publicPaths.toArray(new String[0]))
				/* Disable CSRF */
				.csrf(AbstractHttpConfigurer::disable)

				/* Make the application stateless */
				.sessionManagement(httpSecuritySessionManagementConfigurer -> httpSecuritySessionManagementConfigurer
						.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

		LOG.info("Configure login mode: classic={}, oauth2={}", loginProperties.isClassicModeEnabled(), loginProperties.isOAuth2ModeEnabled());

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
            configureLoginOAuth2Mode(httpSecurity, sechubSecurityProperties, restTemplate, aes256Encryption, oAuth2AuthorizedClientService,
                    expirationCalculator);
        }

        if (loginProperties.isClassicModeEnabled()) {
            configureLoginClassicMode(httpSecurity, aes256Encryption, sechubSecurityProperties);
        }

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
        if (isLoginNotEnabled(loginProperties)) {
            return;
        }

        Set<String> publicPaths = new HashSet<>(DEFAULT_PUBLIC_PATHS);
        publicPaths.add(loginProperties.getLoginPage());
        RequestMatcher publicPathsMatcher = new OrRequestMatcher(publicPaths.stream().map(AntPathRequestMatcher::new).toArray(AntPathRequestMatcher[]::new));
        RequestMatcher protectedPathsMatcher = new NegatedRequestMatcher(publicPathsMatcher);
        httpSecurity.securityMatcher(protectedPathsMatcher);
    }

    /* @formatter:off */
    private void configureResourceServerMode(HttpSecurity httpSecurity,
													SecHubSecurityProperties sechubSecurityProperties,
													UserDetailsService userDetailsService,
													AES256Encryption aes256Encryption,
													JwtDecoder jwtDecoder,
													RestTemplate restTemplate,
													ApplicationShutdownHandler applicationShutdownHandler,
													OAuth2TokenExpirationCalculator expirationCalculator,
													OAuth2OpaqueTokenIntrospectionResponseCryptoAccessProvider cryptoAccessprovider) throws Exception {
        SecHubSecurityProperties.ResourceServerProperties resourceServerProperties = sechubSecurityProperties.getResourceServerProperties();
		if (resourceServerProperties == null) {
			/*
			 * This is useful for testing purposes where the security layer is mocked or if you need disable all authentication
			 * modes for any reason. Note that the application is still protected but all requests to protected paths will be
			 * rejected.
			 */
			LOG.warn("No resource server configuration detected. All requests to protected paths will be rejected.");
			return;
		}

		LOG.info("Configure resource server mode: classic={}, oauth2={}", resourceServerProperties.isClassicModeEnabled(), resourceServerProperties.isOAuth2ModeEnabled());

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
            /* @formatter:off */
            configureResourceServerOAuth2Mode(
					httpSecurity,
					resourceServerProperties.getOAuth2Properties(),
					userDetailsService,
					aes256Encryption,
					jwtDecoder,
                    restTemplate,
					applicationShutdownHandler,
					expirationCalculator,
					sechubSecurityProperties.getMinimumTokenValidity(),
					cryptoAccessprovider);
			/* @formatter:on */
        }
    }

    private void configureResourceServerClassicMode(HttpSecurity httpSecurity, AES256Encryption aes256Encryption) throws Exception {
        ClassicAuthCredentialsCookieFilter classicAuthCredentialsCookieFilter = new ClassicAuthCredentialsCookieFilter(aes256Encryption);

        /* @formatter:off */
		httpSecurity
				.httpBasic(Customizer.withDefaults())
				.addFilterBefore(classicAuthCredentialsCookieFilter, SecurityContextHolderFilter.class);
		/* @formatter:on */
    }

    /* @formatter:off */
	private void configureResourceServerOAuth2Mode(HttpSecurity httpSecurity,
														  SecHubSecurityProperties.ResourceServerProperties.OAuth2Properties oAuth2Properties,
														  UserDetailsService userDetailsService,
														  AES256Encryption aes256Encryption,
														  JwtDecoder jwtDecoder,
														  RestTemplate restTemplate,
														  ApplicationShutdownHandler applicationShutdownHandler,
														  OAuth2TokenExpirationCalculator expirationCalculator,
														  Duration minimumTokenValidity,
														  OAuth2OpaqueTokenIntrospectionResponseCryptoAccessProvider cryptoAccessprovider) throws Exception {
	    if (oAuth2Properties==null) {
	        throw new BeanInstantiationException(SecurityFilterChain.class, "The oauth2 resource server properties must not be null! You have to configure: "+SecHubSecurityProperties.ResourceServerProperties.OAuth2Properties.PREFIX);
	    }
	    LOG.info("Configure resource server oAuth2 mode: jwt={}, opaqueToken={}", oAuth2Properties.isJwtModeEnabled(), oAuth2Properties.isOpaqueTokenModeEnabled());

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
			configureResourceServerOAuth2OpaqueTokenMode(httpSecurity, oAuth2Properties.getOpaqueTokenProperties(), userDetailsService, restTemplate, aes256Encryption, applicationShutdownHandler, expirationCalculator, minimumTokenValidity,cryptoAccessprovider);
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
	private void configureResourceServerOAuth2OpaqueTokenMode(HttpSecurity httpSecurity,
																	 SecHubSecurityProperties.ResourceServerProperties.OAuth2Properties.OpaqueTokenProperties opaqueTokenProperties,
																	 UserDetailsService userDetailsService,
																	 RestTemplate restTemplate,
																	 AES256Encryption aes256Encryption,
																	 ApplicationShutdownHandler applicationShutdownHandler,
																	 OAuth2TokenExpirationCalculator expirationCalculator,
																	 Duration minimumTokenValidity,
																	 OAuth2OpaqueTokenIntrospectionResponseCryptoAccessProvider cryptoAccessprovider) throws Exception {

		if (userDetailsService == null) {
			throw new NoSuchBeanDefinitionException(UserDetailsService.class);
		}

		if (restTemplate == null) {
			throw new NoSuchBeanDefinitionException(RestTemplate.class);
		}

		if (applicationShutdownHandler == null) {
			throw new NoSuchBeanDefinitionException(ApplicationShutdownHandler.class);
		}

		CachePersistence<OAuth2OpaqueTokenIntrospectionResponse> tokenClusterCachePersistence = getOAuth2OpaqueTokenClusterPersistence();

		RestTemplateOAuth2OpaqueTokenIDPIntrospectionResponseFetcher fetcher = RestTemplateOAuth2OpaqueTokenIDPIntrospectionResponseFetcher.builder().
		        setRestTemplate(restTemplate).
                setIntrospectionUri(opaqueTokenProperties.getIntrospectionUri()).
                setClientId(opaqueTokenProperties.getClientId()).
                setClientSecret(opaqueTokenProperties.getClientSecret()).
		build();


        OpaqueTokenIntrospector opaqueTokenIntrospector = OAuth2OpaqueTokenIntrospector.builder().
                setCryptoAccessProvider(cryptoAccessprovider).
                setTokenInMemoryCachePersistence(new InMemoryCachePersistence<>()).
                setIntrospectionResponseFetcher(fetcher).
				setDefaultTokenExpiresIn(opaqueTokenProperties.getDefaultTokenExpiresAt()).
				setMaxCacheDuration(opaqueTokenProperties.getMaxCacheDuration()).
				setPreCacheDuration(opaqueTokenProperties.getPreCacheDuration()).
                setInMemoryCacheClearPeriod(opaqueTokenProperties.getInMemoryCacheClearPeriod()).
                setClusterCacheClearPeriod(opaqueTokenProperties.getClusterCacheClearPeriod()).
				setUserDetailsService(userDetailsService).
				setApplicationShutdownHandler(applicationShutdownHandler).
				setExpirationCalculator(expirationCalculator).
				setTokenClusterCachePersistence(tokenClusterCachePersistence).
				setMinimumTokenValidity(minimumTokenValidity).
		build();

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

    /**
     * Returns token cluster persistence, used for long term and cluster wide opaque
     * token response caching (if cluster persistence is supported by application by
     * current configuration setup)
     *
     * @return token cluster persistence or <code>null</code>
     */
    protected abstract CachePersistence<OAuth2OpaqueTokenIntrospectionResponse> getOAuth2OpaqueTokenClusterPersistence();

    /* @formatter:off */
	private static void configureLoginOAuth2Mode(HttpSecurity httpSecurity,
												 SecHubSecurityProperties sechubSecurityProperties,
												 RestTemplate restTemplate,
												 AES256Encryption aes256Encryption,
												 OAuth2AuthorizedClientService oAuth2AuthorizedClientService,
												 OAuth2TokenExpirationCalculator expirationCalculator) throws Exception {

	    SecHubSecurityProperties.LoginProperties loginProperties = sechubSecurityProperties.getLoginProperties();
	    if (loginProperties == null) {
            throw new NoSuchBeanDefinitionException(SecHubSecurityProperties.LoginProperties.class);
        }

		SecHubSecurityProperties.LoginProperties.OAuth2Properties loginOAuth2Properties = loginProperties.getOAuth2Properties();
		if (loginOAuth2Properties == null) {
            throw new NoSuchBeanDefinitionException(SecHubSecurityProperties.LoginProperties.OAuth2Properties.class);
        }

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

		Duration minimumTokenValidity = sechubSecurityProperties.getMinimumTokenValidity();
		AuthenticationSuccessHandler authenticationSuccessHandler = new LoginOAuth2SuccessHandler(loginOAuth2Properties.getProvider(), oAuth2AuthorizedClientService,
				aes256Encryption, loginProperties.getRedirectUri(), minimumTokenValidity, expirationCalculator);

		/* Enable OAuth2 */
		httpSecurity.oauth2Login(oauth2 -> oauth2.loginPage(loginProperties.getLoginPage())
				.tokenEndpoint(token -> token.accessTokenResponseClient(loginOAuth2AccessTokenClient))
				.successHandler(authenticationSuccessHandler));
	}
	/* @formatter:on */

    private static void configureLoginClassicMode(HttpSecurity httpSecurity, AES256Encryption aes256Encryption,
            SecHubSecurityProperties secHubSecurityProperties) throws Exception {
        if (aes256Encryption == null) {
            throw new NoSuchBeanDefinitionException(AES256Encryption.class);
        }
        SecHubSecurityProperties.LoginProperties loginProperties = secHubSecurityProperties.getLoginProperties();
        if (loginProperties == null) {
            throw new IllegalArgumentException("Property '%s' must not be null".formatted(SecHubSecurityProperties.LoginProperties.PREFIX));
        }

        Duration minimumTokenValidity = secHubSecurityProperties.getMinimumTokenValidity();
        Duration classicCookieAge = loginProperties.getClassicAuthProperties().getCookieAge();
        if (minimumTokenValidity != null && classicCookieAge.getSeconds() < minimumTokenValidity.getSeconds()) {
            LOG.debug(
                    "Will use minimum token validity time of {} seconds for classic cookie age, because {} seconds were configured for classic authentication, which is smaller than the configured minimum.",
                    minimumTokenValidity.getSeconds(), classicCookieAge.getSeconds());
            classicCookieAge = minimumTokenValidity;
        }
        /* @formatter:off */
        AuthenticationSuccessHandler authenticationSuccessHandler = new LoginClassicSuccessHandler(
				aes256Encryption,
				classicCookieAge,
				loginProperties.getRedirectUri()
		);
        String loginPage = loginProperties.getLoginPage();
		httpSecurity.formLogin(form -> form
				.loginPage(loginPage)
				.successHandler(authenticationSuccessHandler)
				.failureUrl("%s?tab=classic&error=true&errorMsg=Invalid User ID or API Token".formatted(loginPage))
		);
		/* @formatter:on */
    }

    private static void configureLogout(HttpSecurity httpSecurity, SecHubSecurityProperties.LoginProperties loginProperties) throws Exception {
        if (isLoginNotEnabled(loginProperties)) {
            return;
        }

        /* we redirect to the frontend because of CORS */
        SimpleUrlLogoutSuccessHandler logoutSuccessHandler = new SimpleUrlLogoutSuccessHandler();
        logoutSuccessHandler.setDefaultTargetUrl(loginProperties.getRedirectUri());

        /* @formatter:off */
		/* logout need to be setup in same Bean as authorizeHttpRequests */
		/* the default logout URL is /logout */
		httpSecurity
				.logout(logout -> logout
						.logoutSuccessHandler(logoutSuccessHandler)
						.deleteCookies(CLASSIC_AUTH_COOKIE_NAME, OAUTH2_COOKIE_NAME));
		/* @formatter:on */
    }

    private static boolean isLoginNotEnabled(SecHubSecurityProperties.LoginProperties loginProperties) {
        return loginProperties == null || !Boolean.TRUE.equals(loginProperties.isEnabled());
    }
}
