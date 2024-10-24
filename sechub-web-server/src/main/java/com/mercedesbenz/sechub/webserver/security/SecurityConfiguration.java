// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.webserver.security;

import java.util.Arrays;

import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.NegatedRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.client.RestTemplate;

import com.mercedesbenz.sechub.webserver.ApplicationProfiles;
import com.mercedesbenz.sechub.webserver.RequestConstants;
import com.mercedesbenz.sechub.webserver.encryption.AES256Encryption;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
class SecurityConfiguration {
    static final String ACCESS_TOKEN = "access_token";

    // @formatter:off
    private static final String[] PUBLIC_PATHS = {
            RequestConstants.ROOT,
            RequestConstants.LOGIN,
            "/login/**",
            "/oauth2/**",
            "/css/**",
            "/js/**",
            "/sechub-logo.svg",
            "/images/**",
    };
    // @formatter:on
    private static final String SCOPE = "openid";
    private static final String USER_NAME_ATTRIBUTE_NAME = "sub";

    private final Environment environment;
    private final OAuth2Properties oAuth2Properties;
    private final AES256Encryption aes256Encryption;

    SecurityConfiguration(@Autowired Environment environment, @Autowired(required = false) OAuth2Properties oAuth2Properties,
            @Autowired AES256Encryption aes256Encryption) {
        this.environment = environment;
        if (isOAuth2Enabled() && oAuth2Properties == null) {
            throw new NoSuchBeanDefinitionException(
                    "No qualifying bean of type 'OAuth2Properties' available: expected at least 1 bean which qualifies as autowire candidate.");
        }
        if (!isOAuth2Enabled() && !isClassicAuthEnabled()) {
            throw new IllegalStateException("At least one authentication method must be enabled");
        }
        this.oAuth2Properties = oAuth2Properties;
        this.aes256Encryption = aes256Encryption;
    }

    @Bean
    @Profile(ApplicationProfiles.OAUTH2_ENABLED)
    ClientRegistrationRepository clientRegistrationRepository() {
        /* @formatter:off */
        ClientRegistration clientRegistration = ClientRegistration
                .withRegistrationId(oAuth2Properties.getProvider())
                .clientId(oAuth2Properties.getClientId())
                .clientSecret(oAuth2Properties.getClientSecret())
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri(oAuth2Properties.getRedirectUri())
                .issuerUri(oAuth2Properties.getIssuerUri()).scope(SCOPE)
                .authorizationUri(oAuth2Properties.getAuthorizationUri())
                .tokenUri(oAuth2Properties.getTokenUri())
                .userInfoUri(oAuth2Properties.getUserInfoUri())
                .jwkSetUri(oAuth2Properties.getJwkSetUri())
                .userNameAttributeName(USER_NAME_ATTRIBUTE_NAME)
                .build();
        /* @formatter:on */

        return new InMemoryClientRegistrationRepository(clientRegistration);
    }

    @Bean
    @Profile(ApplicationProfiles.OAUTH2_ENABLED)
    SecurityFilterChain securityFilterChainAuthenticated(HttpSecurity httpSecurity, @Autowired(required = false) AuthenticationManager authenticationManager)
            throws Exception {
        AuthenticationEntryPoint authenticationEntryPoint = new OAuth2MissingAuthenticationEntryPointHandler();
        BearerTokenResolver bearerTokenResolver = new JwtCookieResolver(aes256Encryption);
        /* @formatter:off */
        RequestMatcher publicPathsMatcher = new OrRequestMatcher(
                Arrays.stream(PUBLIC_PATHS)
                        .map(AntPathRequestMatcher::new)
                        .toArray(AntPathRequestMatcher[]::new)
        );
        RequestMatcher protectedPathsMatcher = new NegatedRequestMatcher(publicPathsMatcher);

        httpSecurity
                .securityMatcher(protectedPathsMatcher)
                .oauth2ResourceServer(oauth2ResourceServer -> oauth2ResourceServer
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .bearerTokenResolver(bearerTokenResolver)
                        .jwt(jwt -> jwt.jwkSetUri(oAuth2Properties.getJwkSetUri()))
                );
        /* @formatter:on */

        if (authenticationManager != null) {
            /*
             * This is useful to mock authentication when no real authentication manager can
             * be constructed (e.g. in tests)
             */
            httpSecurity.authenticationManager(authenticationManager);
        }

        return httpSecurity.build();
    }

    @Bean
    SecurityFilterChain securityFilterChainAnonymous(HttpSecurity httpSecurity,
            @Autowired(required = false) OAuth2AuthorizedClientService oAuth2AuthorizedClientService) throws Exception {
        /* @formatter:off */

        httpSecurity
                /* Disable CSRF */
                .securityMatcher(PUBLIC_PATHS)
                .csrf(AbstractHttpConfigurer::disable)
                /* Make the application stateless */
                .sessionManagement(httpSecuritySessionManagementConfigurer -> httpSecuritySessionManagementConfigurer
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        if (isOAuth2Enabled()) {
            RestTemplate restTemplate = new RestTemplate();
            Base64EncodedClientIdAndSecretOAuth2AccessTokenClient base64EncodedClientIdAndSecretOAuth2AccessTokenClient = new Base64EncodedClientIdAndSecretOAuth2AccessTokenClient(restTemplate);
            if (oAuth2AuthorizedClientService == null) {
                throw new NoSuchBeanDefinitionException(
                        "No qualifying bean of type 'OAuth2AuthorizedClientService' available: expected at least 1 bean which qualifies as autowire candidate.");
            }
            AuthenticationSuccessHandler authenticationSuccessHandler = new OAuth2LoginSuccessHandler(oAuth2Properties, oAuth2AuthorizedClientService, aes256Encryption);
            /* Enable OAuth2 */
            httpSecurity.oauth2Login(oauth2 -> oauth2
                .loginPage(RequestConstants.LOGIN)
                .tokenEndpoint(token -> token.accessTokenResponseClient(base64EncodedClientIdAndSecretOAuth2AccessTokenClient))
                .successHandler(authenticationSuccessHandler));
        }

        if (isClassicAuthEnabled()) {
            /*
                Enable Classic Authentication
                Note: This must be the last configuration in order to set the default 'loginPage' to oAuth2
                because spring uses the 'loginPage' from the first authentication method configured
            */
            AuthenticationSuccessHandler authenticationSuccessHandler = new ClassicLoginSuccessHandler();
            httpSecurity
                .formLogin(form -> form
                .loginPage(RequestConstants.LOGIN)
                .successHandler(authenticationSuccessHandler));
        }

        /* @formatter:on */

        return httpSecurity.build();
    }

    private boolean isOAuth2Enabled() {
        return environment.matchesProfiles(ApplicationProfiles.OAUTH2_ENABLED);
    }

    private boolean isClassicAuthEnabled() {
        return environment.matchesProfiles(ApplicationProfiles.CLASSIC_AUTH_ENABLED);
    }

}
