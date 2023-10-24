// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.security;

import static com.mercedesbenz.sechub.pds.security.PDSRoleConstants.*;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;

import com.mercedesbenz.sechub.pds.PDSAPIConstants;

/**
 * A base class for web security having access to "/api/**" when being a user or
 * super admin <br>
 * <br>
 * Defaults:
 * <ul>
 * <li>CSRF is disabled</li>
 * <li>HTTP basic enabled</li>
 * <li>api/anonymous is available also when not authenticated</li>
 * <li>api/job is available when authenticated and role user</li>
 * </ul>
 *
 */
public abstract class AbstractAllowPDSAPISecurityConfiguration extends WebSecurityConfigurerAdapter {
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
 				antMatchers(PDSAPIConstants.API_JOB+"**").
 					hasAnyAuthority(ROLE_USER, ROLE_SUPERADMIN).
 				antMatchers(PDSAPIConstants.API_ADMIN+"**").
                    hasAnyAuthority(ROLE_SUPERADMIN).
 				antMatchers(PDSAPIConstants.API_ANONYMOUS+"**").
 					permitAll().
 				antMatchers(PDSAPIConstants.ERROR_PAGE).
 				    permitAll().
 	 			antMatchers(PDSAPIConstants.ACTUATOR+"**").
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
			httpBasic()./* no login screen, just basic auth */
 		and().
			headers().
				contentSecurityPolicy("default-src 'none'");

 		/* @formatter:on */
    }

}