package com.mercedesbenz.sechub.webui.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@EnableWebSecurity
public class SecurityConfiguration {
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
		UserDetails user = User.withDefaultPasswordEncoder().
				username("user").password("password").roles("USER").
				build();
		return new InMemoryUserDetailsManager(user);
		/* @formatter:on */
    }
}
