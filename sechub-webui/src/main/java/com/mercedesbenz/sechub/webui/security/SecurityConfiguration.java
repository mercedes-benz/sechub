// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.webui.security;

import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.web.client.RestTemplate;

import com.mercedesbenz.sechub.webui.ApplicationProfiles;
import com.mercedesbenz.sechub.webui.RequestConstants;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
class SecurityConfiguration {
    private static final String[] PUBLIC_PATHS = { RequestConstants.LOGIN_CLASSIC, RequestConstants.LOGIN_OAUTH2, "/css/**", "/js/**", "/images/**" };
    private static final String SCOPE = "openid";
    private static final String USER_NAME_ATTRIBUTE_NAME = "sub";

    private final Environment environment;
    private final OAuth2Properties oAuth2Properties;

    SecurityConfiguration(@Autowired Environment environment, @Autowired(required = false) OAuth2Properties oAuth2Properties) {
        this.environment = environment;
        if (isOAuth2Enabled() && oAuth2Properties == null) {
            throw new NoSuchBeanDefinitionException(
                    "No qualifying bean of type 'OAuth2Properties' available: expected at least 1 bean which qualifies as autowire candidate.");
        }
        if (!isOAuth2Enabled() && !isClassicAuthEnabled()) {
            throw new IllegalStateException("At least one authentication method must be enabled");
        }
        this.oAuth2Properties = oAuth2Properties;
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
    SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        AuthenticationSuccessHandler authenticationSuccessHandler = new LoginAuthenticationSuccessHandler();

        /* @formatter:off */

        httpSecurity
                /* Disable CSRF */
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(exchanges -> exchanges
                        /* Allow access to public paths */
                        .requestMatchers(PUBLIC_PATHS).permitAll()
                        /* Protect all other paths */
                        .anyRequest().authenticated()
                )
                /* Make the application stateless */
                .sessionManagement(httpSecuritySessionManagementConfigurer -> httpSecuritySessionManagementConfigurer
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        if (isOAuth2Enabled()) {
            RestTemplate restTemplate = new RestTemplate();
            Base64EncodedClientIdAndSecretOAuth2AccessTokenClient base64EncodedClientIdAndSecretOAuth2AccessTokenClient = new Base64EncodedClientIdAndSecretOAuth2AccessTokenClient(restTemplate);
            /* Enable OAuth2 */
            httpSecurity.oauth2Login(oauth2 -> oauth2
                .loginPage(RequestConstants.LOGIN_OAUTH2)
                .tokenEndpoint(token -> token.accessTokenResponseClient(base64EncodedClientIdAndSecretOAuth2AccessTokenClient))
                .successHandler(authenticationSuccessHandler));
        }

        if (isClassicAuthEnabled()) {
            /*
                Enable Classic Authentication
                Note: This must be the last configuration in order to set the default 'loginPage' to oAuth2
                because spring uses the 'loginPage' from the first authentication method configured
            */
            httpSecurity
                .formLogin(form -> form
                .loginPage(RequestConstants.LOGIN_CLASSIC)
                .permitAll()
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
