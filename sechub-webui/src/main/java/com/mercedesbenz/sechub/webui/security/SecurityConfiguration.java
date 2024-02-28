// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.webui.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.csrf.CookieServerCsrfTokenRepository;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;

import com.mercedesbenz.sechub.webui.page.user.UserDetailInformationService;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfiguration {
    @Autowired
    UserDetailInformationService userDetailInformationService;

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity httpSecurity) throws Exception {

        /* @formatter:off */
		httpSecurity.
			        authorizeExchange().
					  pathMatchers("/css/**", "/js/**", "/images/**").permitAll().
					  pathMatchers("/login", "/logout").permitAll().
					  anyExchange().authenticated().
					and().
					  formLogin().loginPage("/login").
					and().
					  logout().
					  requiresLogout(ServerWebExchangeMatchers.pathMatchers(HttpMethod.GET, "/logout")).
					and().
					  csrf(csrf -> csrf.csrfTokenRepository(new CookieServerCsrfTokenRepository()));
		/* @formatter:on */

        return httpSecurity.build();
    }

    @Bean
    public MapReactiveUserDetailsService userDetailsService() {
        return new MapReactiveUserDetailsService(userDetailInformationService.getUser());
    }
}
