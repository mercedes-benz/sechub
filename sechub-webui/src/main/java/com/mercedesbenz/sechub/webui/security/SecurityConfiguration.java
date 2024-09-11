// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.webui.security;

import com.mercedesbenz.sechub.webui.RequestConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
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
			RequestConstants.LOGIN_OIDC,
			"/css/**",
			"/js/**",
			"/images/**"
	};

	@Autowired
    UserDetailInformationService userDetailInformationService;

    @Bean
	@Profile({"webui_prod", "webui_dev"})
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity httpSecurity) {
    	return defaultHttpSecurity(httpSecurity)
				/* Enable OAuth2 login */
				.oauth2Login(withDefaults())
				.build();
    }

	@Bean
	@Profile("webui_local")
	public SecurityWebFilterChain springSecurityFilterChainLocal(ServerHttpSecurity httpSecurity) {
		return defaultHttpSecurity(httpSecurity).build();
	}

	@Bean
    public MapReactiveUserDetailsService userDetailsService() {
        return new MapReactiveUserDetailsService(userDetailInformationService.getUser(), userDetailInformationService.getUser());
    }

	private static ServerHttpSecurity defaultHttpSecurity(ServerHttpSecurity httpSecurity) {
		return httpSecurity
				.authorizeExchange(exchanges -> exchanges
						/* Allow access to public paths */
						.pathMatchers(PERMITTED_PATHS).permitAll()
						/* Protect all other paths */
						.anyExchange().authenticated())
				/* CSRF protection disabled. The CookieServerCsrfTokenRepository does not work, since Spring Boot 3 */
				.csrf(ServerHttpSecurity.CsrfSpec::disable);
	}
}
