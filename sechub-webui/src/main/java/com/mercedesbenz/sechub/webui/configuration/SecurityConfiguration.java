// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.webui.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@EnableWebSecurity
public class SecurityConfiguration {
    @Autowired
    UserDetailInformationService userDetailInformationService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        /* @formatter:off */
		httpSecurity.
			authorizeRequests().
			// allow unauthenticated access to css, js and images
			antMatchers("/css/**", "/js/**", "/images/**").permitAll().
			anyRequest().authenticated().
			and().
			formLogin((form) -> form
					.loginPage("/login")
					.permitAll()).
			logout().
				logoutRequestMatcher(new AntPathRequestMatcher("/logout")).
				logoutSuccessUrl("/login")
			;
		/* @formatter:on */

        return httpSecurity.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        /* @formatter:off */
		return new InMemoryUserDetailsManager(userDetailInformationService.getUser());
		/* @formatter:on */
    }
}
