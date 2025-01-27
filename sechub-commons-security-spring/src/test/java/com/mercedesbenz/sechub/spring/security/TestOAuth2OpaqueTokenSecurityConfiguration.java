// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.spring.security;

import static com.mercedesbenz.sechub.spring.security.TestRoles.OWNER;
import static com.mercedesbenz.sechub.spring.security.TestRoles.SUPERADMIN;
import static com.mercedesbenz.sechub.spring.security.TestRoles.USER;
import static java.util.Objects.requireNonNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.opentest4j.TestAbortedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

/**
 * This configuration class provides the necessary beans to test Springs OAuth2
 * integration with SecHub components in opaque token mode.
 *
 * @author hamidonos
 */
@Configuration
public class TestOAuth2OpaqueTokenSecurityConfiguration {

    public static final String BEARER_PREFIX = OAuth2AccessToken.TokenType.BEARER.getValue() + " ";

    private static final String TOKEN = "token";
    private static final String ADMIN_OPAQUE_TOKEN = "admin-opaque-token";
    private static final String ADMIN_OWNER_OPAQUE_TOKEN = "admin-owner-opaque-token";
    private static final String ADMIN_USER_OPAQUE_TOKEN = "admin-user-opaque-token";
    private static final String OWNER_OPAQUE_TOKEN = "owner-opaque-token";
    private static final String OWNER_USER_OPAQUE_TOKEN = "owner-user-opaque-token";
    private static final String USER_OPAQUE_TOKEN = "user-opaque-token";

    private static final String ADMIN_ID = UUID.randomUUID().toString();
    private static final String ADMIN_OWNER_ID = UUID.randomUUID().toString();
    private static final String ADMIN_USER_ID = UUID.randomUUID().toString();
    private static final String OWNER_ID = UUID.randomUUID().toString();
    private static final String OWNER_USER_ID = UUID.randomUUID().toString();
    private static final String USER_ID = UUID.randomUUID().toString();

    /* @formatter:off */
    @Autowired
    TestOAuth2OpaqueTokenSecurityConfiguration(RestTemplate restTemplate,
                                               SecHubSecurityProperties secHubSecurityProperties) {

        String introspectionUri = secHubSecurityProperties.getResourceServerProperties().getOAuth2Properties().getOpaqueTokenProperties().getIntrospectionUri();

        when(restTemplate.postForObject(eq(introspectionUri), any(), eq(OAuth2OpaqueTokenIntrospectionResponse.class))).thenAnswer(invocation -> {
            HttpEntity<MultiValueMap<String, String>> request = invocation.getArgument(1);
            String token = requireNonNull(request.getBody()).getFirst(TOKEN);
            boolean isActive = false;
            String subject = "";

            if (ADMIN_OPAQUE_TOKEN.equals(token)) {
                isActive = true;
                subject = ADMIN_ID;
            }
            if (ADMIN_OWNER_OPAQUE_TOKEN.equals(token)) {
                isActive = true;
                subject = ADMIN_OWNER_ID;
            }
            if (ADMIN_USER_OPAQUE_TOKEN.equals(token)) {
                isActive = true;
                subject = ADMIN_USER_ID;
            }
            if (OWNER_OPAQUE_TOKEN.equals(token)) {
                isActive = true;
                subject = OWNER_ID;
            }
            if (OWNER_USER_OPAQUE_TOKEN.equals(token)) {
                isActive = true;
                subject = OWNER_USER_ID;
            }
            if (USER_OPAQUE_TOKEN.equals(token)) {
                isActive = true;
                subject = USER_ID;
            }

            return new OAuth2OpaqueTokenIntrospectionResponse(isActive,
                    "scope",
                    "client-id",
                    "client-type",
                    subject,
                    "token-type",
                    Instant.now().plusSeconds(60L).getEpochSecond(),
                    subject,
                    "aud",
                    "group-type");
        });
    }
    /* @formatter:on */

    /**
     * Here we mock the {@link UserDetailsService} to return a
     * {@link TestUserDetails} object based on the user id (or subject). The subject
     * is determined by the {@link OAuth2OpaqueTokenIntrospector} component.
     * Depending on the user id, the {@link TestUserDetails} object will contain the
     * corresponding authorities.
     */
    @Bean
    UserDetailsService userDetailsService() {
        UserDetailsService userDetailsService = mock();
        when(userDetailsService.loadUserByUsername(anyString())).thenAnswer(invocation -> {
            String username = invocation.getArgument(0);
            if (!Set.of(ADMIN_ID, ADMIN_OWNER_ID, ADMIN_USER_ID, OWNER_ID, OWNER_USER_ID, USER_ID).contains(username)) {
                throw new UsernameNotFoundException("User %s not found".formatted(username));
            }

            Collection<SimpleGrantedAuthority> authorities = new HashSet<>();

            if (ADMIN_ID.equals(username)) {
                authorities.add(new SimpleGrantedAuthority(SUPERADMIN));
            }
            if (ADMIN_OWNER_ID.equals(username)) {
                authorities.add(new SimpleGrantedAuthority(SUPERADMIN));
                authorities.add(new SimpleGrantedAuthority(OWNER));
            }
            if (ADMIN_USER_ID.equals(username)) {
                authorities.add(new SimpleGrantedAuthority(SUPERADMIN));
                authorities.add(new SimpleGrantedAuthority(USER));
            }
            if (OWNER_ID.equals(username)) {
                authorities.add(new SimpleGrantedAuthority(OWNER));
            }
            if (OWNER_USER_ID.equals(username)) {
                authorities.add(new SimpleGrantedAuthority(OWNER));
                authorities.add(new SimpleGrantedAuthority(USER));
            }
            if (USER_ID.equals(username)) {
                authorities.add(new SimpleGrantedAuthority(USER));
            }

            return new TestUserDetails(authorities, username);
        });

        return userDetailsService;
    }

    public static String createOpaqueTokenHeader(Set<String> roles) {
        if (roles.isEmpty()) {
            throw new TestAbortedException("Roles cannot be empty");
        }

        if (roles.equals(Set.of(SUPERADMIN))) {
            return BEARER_PREFIX + ADMIN_OPAQUE_TOKEN;
        }

        if (roles.equals(Set.of(SUPERADMIN, OWNER))) {
            return BEARER_PREFIX + ADMIN_OWNER_OPAQUE_TOKEN;
        }

        if (roles.equals(Set.of(SUPERADMIN, USER))) {
            return BEARER_PREFIX + ADMIN_USER_OPAQUE_TOKEN;
        }

        if (roles.equals(Set.of(OWNER))) {
            return BEARER_PREFIX + OWNER_OPAQUE_TOKEN;
        }

        if (roles.equals(Set.of(OWNER, USER))) {
            return BEARER_PREFIX + OWNER_USER_OPAQUE_TOKEN;
        }

        if (roles.equals(Set.of(USER))) {
            return BEARER_PREFIX + USER_OPAQUE_TOKEN;
        }

        throw new TestAbortedException("Invalid roles");
    }
}
