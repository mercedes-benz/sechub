// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.webui.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import com.mercedesbenz.sechub.webui.RequestConstants;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
class SecurityConfiguration {
    private static final String[] PUBLIC_PATHS = { RequestConstants.BASIC_AUTH_LOGIN, RequestConstants.OAUTH_LOGIN, "/css/**", "/js/**", "/images/**" };

    @Bean
    @Profile("oauth2-enabled")
    SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        AuthenticationSuccessHandler oAuth2SuccessHandler = new OAuth2SuccessHandler();
        MercedesBenzOAuth2AccessTokenClient mercedesBenzOAuth2AccessTokenClient = new MercedesBenzOAuth2AccessTokenClient();

        return defaultHttpSecurity(httpSecurity)
                /* Enable OAuth2 */
                .oauth2Login(oauth2 -> oauth2.tokenEndpoint(token -> token.accessTokenResponseClient(mercedesBenzOAuth2AccessTokenClient))
                        .successHandler(oAuth2SuccessHandler))
                .build();
    }

    @Bean
    @Profile("local")
    SecurityFilterChain securityFilterChainLocal(HttpSecurity httpSecurity) throws Exception {
        return defaultHttpSecurity(httpSecurity).build();
    }

    private static HttpSecurity defaultHttpSecurity(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity.csrf(AbstractHttpConfigurer::disable).authorizeHttpRequests(exchanges -> exchanges
                /* Allow access to public paths */
                .requestMatchers(PUBLIC_PATHS).permitAll()
                /* Protect all other paths */
                .anyRequest().authenticated())
                /* Enable stateful sessions */
                .sessionManagement(httpSecuritySessionManagementConfigurer -> httpSecuritySessionManagementConfigurer
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED));
    }
}
