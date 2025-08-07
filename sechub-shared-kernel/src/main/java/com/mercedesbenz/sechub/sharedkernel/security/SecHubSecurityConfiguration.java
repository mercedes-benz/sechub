// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;

import com.mercedesbenz.sechub.commons.core.cache.CachePersistence;
import com.mercedesbenz.sechub.sharedkernel.security.clustercache.OAuth2OpaqueTokenClusterCachePersistence;
import com.mercedesbenz.sechub.spring.security.AbstractSecurityConfiguration;
import com.mercedesbenz.sechub.spring.security.OAuth2OpaqueTokenIntrospectionResponse;

@Configuration
@EnableMethodSecurity(jsr250Enabled = true)
@EnableWebSecurity
public class SecHubSecurityConfiguration extends AbstractSecurityConfiguration {

    @Autowired(required = false) // required = false, because only injected/necessary when oauth2 used
    OAuth2OpaqueTokenClusterCachePersistence opaqueTokenClusterCachePersistence;

    @Override
    protected Customizer<AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry> authorizeHttpRequests() {
        /* @formatter:off */
        return (auth) -> auth
                .requestMatchers(APIConstants.API_ADMINISTRATION + "**").hasAnyRole(RoleConstants.ROLE_SUPERADMIN)
                .requestMatchers(APIConstants.API_USER + "**").hasAnyRole(RoleConstants.ROLE_USER, RoleConstants.ROLE_SUPERADMIN)
                .requestMatchers(APIConstants.API_PROJECT + "**").hasAnyRole(RoleConstants.ROLE_USER, RoleConstants.ROLE_SUPERADMIN)
                .requestMatchers(APIConstants.API_OWNER + "**").hasAnyRole(RoleConstants.ROLE_OWNER, RoleConstants.ROLE_SUPERADMIN)
                .requestMatchers(APIConstants.API_PROJECTS).hasAnyRole(RoleConstants.ROLE_USER, RoleConstants.ROLE_SUPERADMIN, RoleConstants.ROLE_OWNER)
                .requestMatchers(APIConstants.API_MANAGEMENT + "**").hasAnyRole(RoleConstants.ROLE_USER, RoleConstants.ROLE_SUPERADMIN, RoleConstants.ROLE_OWNER)
                .requestMatchers(APIConstants.API_ASSISTANT + "**").hasAnyRole(RoleConstants.ROLE_USER, RoleConstants.ROLE_SUPERADMIN, RoleConstants.ROLE_OWNER)
                .requestMatchers(APIConstants.API_ANONYMOUS + "**").permitAll()
                .requestMatchers(APIConstants.ERROR_PAGE).permitAll()
                .requestMatchers(APIConstants.ACTUATOR + "**").permitAll()
                .requestMatchers(APIConstants.STATIC_LOGIN + "/**").permitAll()
                .requestMatchers(APIConstants.LOGOUT).authenticated()
                .requestMatchers("/**").denyAll();
        /* @formatter:on */
    }

    @Override
    protected CachePersistence<OAuth2OpaqueTokenIntrospectionResponse> getOAuth2OpaqueTokenClusterPersistence() {
        return opaqueTokenClusterCachePersistence;
    }
}
