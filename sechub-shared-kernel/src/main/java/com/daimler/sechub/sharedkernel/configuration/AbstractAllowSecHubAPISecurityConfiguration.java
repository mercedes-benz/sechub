// SPDX-License-Identifier: MIT
package com.daimler.sechub.sharedkernel.configuration;

import static com.daimler.sechub.sharedkernel.RoleConstants.*;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;

import com.daimler.sechub.sharedkernel.APIConstants;

/**
 * A base class for web security having access to "/api/**" when being a user or
 * super admin <br>
 * <br>
 * Defaults:
 * <ul>
 * <li>CSRF is disabled</li>
 * <li>HTTP basic enabled</li>
 * <li>api/anonymous is available also when not authenticated</li>
 * <li>api/user is available when authenticated and role user</li>
 * <li>api/project is available when authenticated and role user</li>
 * <li>api/admin is available when authenticated and role superadmin</li>
 * </ul>
 *
 */
public abstract class AbstractAllowSecHubAPISecurityConfiguration extends WebSecurityConfigurerAdapter {
	// https://spring.io/blog/2017/09/15/security-changes-in-spring-boot-2-0-m4
	// https://docs.spring.io/spring-security/site/docs/current/reference/htmlsingle/#core-services-password-encoding

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		/* @formatter:off */
 		http.
		 sessionManagement().
		 	sessionCreationPolicy(SessionCreationPolicy.STATELESS).
	 	 and().
 		 authorizeRequests().
 				antMatchers(APIConstants.API_ADMINISTRATION+"**").
 					hasAnyAuthority(ROLE_SUPERADMIN).

 				antMatchers(APIConstants.API_USER+"**").
 					hasAnyAuthority(ROLE_USER, ROLE_SUPERADMIN).

 				antMatchers(APIConstants.API_PROJECT+"**").
 					hasAnyAuthority(ROLE_USER, ROLE_SUPERADMIN).

 				antMatchers(APIConstants.API_OWNER+"**").
 					hasAnyAuthority(ROLE_OWNER, ROLE_SUPERADMIN).

 				antMatchers(APIConstants.API_ANONYMOUS+"**").
 					permitAll().
 				antMatchers(APIConstants.ACTUATOR+"**").
 					permitAll().
 				/* to prevent configuration failures - I had this issue before -
 				 * all other matchers do deny all. So if not correct
 				 * configured nobody has access - please keep the denyAll parts
 				 */
 				antMatchers("/**").
 					denyAll().

			and().
		csrf().
			disable(). /* disable CSRF for api so we have no CSRF-TOKEN problems - POST would not work*/
			httpBasic();/* no login screen, just basic auth */

 		/* @formatter:on */
	}

}