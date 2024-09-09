// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.webui.security;

import com.mercedesbenz.sechub.webui.RequestConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.web.server.SecurityWebFilterChain;

import com.mercedesbenz.sechub.webui.page.user.UserDetailInformationService;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfiguration {
	private static final String[] PERMITTED_PATHS = {
			RequestConstants.LOGIN,
			RequestConstants.OIDC_LOGIN,
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
						/* Allow access to public paths */
						.pathMatchers(PERMITTED_PATHS).permitAll()
						/* Protect all other paths */
						.anyExchange().authenticated())
				/* Enable OAuth2 login */
				.oauth2Login(withDefaults())
				/* CSRF protection disabled. The CookieServerCsrfTokenRepository does not work, since Spring Boot 3 */
				.csrf(ServerHttpSecurity.CsrfSpec::disable);

        return httpSecurity.build();
    }

    @Bean
    public MapReactiveUserDetailsService userDetailsService() {
        return new MapReactiveUserDetailsService(userDetailInformationService.getUser(), userDetailInformationService.getUser());
    }
}
