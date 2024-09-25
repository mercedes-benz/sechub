package com.mercedesbenz.sechub.webui;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@TestConfiguration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityTestConfiguration {
    private static final String[] PERMITTED_PATHS = { "/auth/bauth/login", "/auth/oauth/login", "/css/**", "/js/**", "/images/**" };

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        return defaultHttpSecurity(httpSecurity).build();
    }

    private static HttpSecurity defaultHttpSecurity(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity.csrf(AbstractHttpConfigurer::disable).authorizeHttpRequests(exchanges -> exchanges
                /* Allow access to public paths */
                .requestMatchers(PERMITTED_PATHS).permitAll()
                /* Protect all other paths */
                .anyRequest().authenticated());
    }
}
