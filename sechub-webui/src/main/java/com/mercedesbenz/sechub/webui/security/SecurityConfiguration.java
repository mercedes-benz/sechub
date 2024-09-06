// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.webui.security;

import com.mercedesbenz.sechub.webui.RequestConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;

import com.mercedesbenz.sechub.webui.page.user.UserDetailInformationService;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfiguration {
	private static final String[] PERMITTED_PATHS = {
			"/css/**",
			"/js/**",
			"/images/**"
	};

	@Autowired
    UserDetailInformationService userDetailInformationService;

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity httpSecurity) {

    	httpSecurity
				.authorizeExchange(exchanges -> exchanges
						.pathMatchers(PERMITTED_PATHS).permitAll()
						.pathMatchers(RequestConstants.LOGIN)
						.permitAll()
						.anyExchange()
						.authenticated())
				// CSRF protection disabled. The CookieServerCsrfTokenRepository does not work, since Spring Boot 3
				.csrf(ServerHttpSecurity.CsrfSpec::disable);

        return httpSecurity.build();
    }

    @Bean
    public MapReactiveUserDetailsService userDetailsService() {
        return new MapReactiveUserDetailsService(userDetailInformationService.getUser(), userDetailInformationService.getUser());
    }
}
