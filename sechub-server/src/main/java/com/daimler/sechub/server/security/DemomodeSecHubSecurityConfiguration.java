// SPDX-License-Identifier: MIT
package com.daimler.sechub.server.security;

import static com.daimler.sechub.domain.authorization.AuthUserRole.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

import com.daimler.sechub.sharedkernel.Profiles;
import com.daimler.sechub.sharedkernel.configuration.AbstractAllowSecHubAPISecurityConfiguration;
@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
@EnableWebSecurity
@Profile({Profiles.DEMOMODE}) // only for demomode
@Order(1)
public class DemomodeSecHubSecurityConfiguration extends AbstractAllowSecHubAPISecurityConfiguration {

	private static final Logger LOG = LoggerFactory.getLogger(DemomodeSecHubSecurityConfiguration.class);

	@Override
 	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		LOG.info("*******************************************************************");
		LOG.info("* SecHub Server DEMO MODE activated!");
		LOG.info("*******************************************************************");
 		/* @formatter:off */
 		auth.inMemoryAuthentication().
 			withUser("developer").
 				password("{bcrypt}$2a$10$L2uu3Ws4iBcP/5Z196cqu.OahGNHuPdy/4d.axnXK0KKqagx1lRTq").
 				roles(USER.name()).
 			and().
 			withUser("alice").// demo user for security user conference. deactivate after... pwd known
 				password("{bcrypt}$2a$10$3CmrXQ4oBE5xWKpWtMj83uyQBT8uEG2BXD/gH/IbouW3LYX12S5T6").
 				roles(USER.name());
 		
 		/* @formatter:on */
 	}
	
	
}