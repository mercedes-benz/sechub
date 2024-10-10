// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.webserver.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;

import com.mercedesbenz.sechub.webserver.page.user.UserDetailInformationService;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfiguration {
    @Autowired
    UserDetailInformationService userDetailInformationService;

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity httpSecurity) {

        /* @formatter:off */
    	httpSecurity.
	        authorizeExchange(exchanges -> exchanges.
	                pathMatchers("/css/**", "/js/**", "/images/**").permitAll().
	                pathMatchers("/login").permitAll().
	                anyExchange().authenticated()
	        ).
	        formLogin(formLogin -> formLogin.
	                loginPage("/login")
	        ).
			logout(logout -> logout.
					logoutUrl("/logout").
					requiresLogout(ServerWebExchangeMatchers.pathMatchers(HttpMethod.GET, "/logout"))
			).
			csrf((csrf) -> csrf.disable() // CSRF protection disabled. The CookieServerCsrfTokenRepository does not work, since Spring Boot 3
        );
    	/* @formatter:on */
        return httpSecurity.build();
    }

    @Bean
    public MapReactiveUserDetailsService userDetailsService() {
        return new MapReactiveUserDetailsService(userDetailInformationService.getUser(), userDetailInformationService.getUser());
    }
}
