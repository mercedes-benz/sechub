// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.webui.security;

import static org.springframework.security.config.Customizer.withDefaults;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import com.mercedesbenz.sechub.webui.ApplicationProfiles;
import com.mercedesbenz.sechub.webui.RequestConstants;

/**
 * @author hamidonos
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfiguration {
    private static final String[] PUBLIC_PATHS = { RequestConstants.LOGIN_CLASSIC, RequestConstants.LOGIN_OAUTH2, "/css/**", "/js/**", "/images/**" };

    private final Environment environment;

    public SecurityConfiguration(Environment environment) {
        this.environment = environment;
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
                /* Enable stateful sessions */
                .sessionManagement(httpSecuritySessionManagementConfigurer -> httpSecuritySessionManagementConfigurer
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                /* Enable Basic Auth */
                .httpBasic(withDefaults())
                .formLogin(form -> form
                        .loginPage(RequestConstants.LOGIN_CLASSIC)
                        .permitAll()
                        .successHandler(authenticationSuccessHandler));

        if (environment.matchesProfiles(ApplicationProfiles.OAUTH2_ENABLED)) {
            MercedesBenzOAuth2AccessTokenClient mercedesBenzOAuth2AccessTokenClient = new MercedesBenzOAuth2AccessTokenClient();
            /* Enable OAuth2 */
            httpSecurity.oauth2Login(oauth2 -> oauth2
                .loginPage(RequestConstants.LOGIN_OAUTH2)
                .tokenEndpoint(token -> token.accessTokenResponseClient(mercedesBenzOAuth2AccessTokenClient))
                .successHandler(authenticationSuccessHandler));
        }

        /* @formatter:on */

        return httpSecurity.build();
    }
}
