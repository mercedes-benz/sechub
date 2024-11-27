// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.spring.security;

import static com.mercedesbenz.sechub.spring.security.TestRoles.OWNER;
import static com.mercedesbenz.sechub.spring.security.TestRoles.SUPERADMIN;
import static com.mercedesbenz.sechub.spring.security.TestRoles.USER;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.opentest4j.TestAbortedException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;

/**
 * This configuration class provides the necessary beans to test Springs OAuth2
 * integration with SecHub components in JWT mode.
 *
 * @author hamidonos
 */
@Configuration
public class TestOAuth2JwtSecurityConfiguration {

    public static final String BEARER_PREFIX = OAuth2AccessToken.TokenType.BEARER.getValue() + " ";

    private static final String ADMIN_JWT = "admin-jwt-token";
    private static final String ADMIN_OWNER_JWT = "admin-owner-jwt-token";
    private static final String ADMIN_USER_JWT = "admin-user-jwt-token";
    private static final String OWNER_JWT = "owner-jwt-token";
    private static final String OWNER_USER_JWT = "owner-user-jwt-token";
    private static final String USER_JWT = "user-jwt-token";

    private static final String ADMIN_ID = UUID.randomUUID().toString();
    private static final String ADMIN_OWNER_ID = UUID.randomUUID().toString();
    private static final String ADMIN_USER_ID = UUID.randomUUID().toString();
    private static final String OWNER_ID = UUID.randomUUID().toString();
    private static final String OWNER_USER_ID = UUID.randomUUID().toString();
    private static final String USER_ID = UUID.randomUUID().toString();

    private static final String ALGORITHM = "alg";
    private static final String ALGORITHM_NONE = "none";

    /**
     * This bean provides a {@link JwtDecoder} that decodes the JWT token and
     * returns a {@link Jwt} object. The behaviour is completely mocked and the
     * possible JWT tokens are pre-defined. Every possible JWT value is mapped to a
     * specific subject (or user id). The subject will be returned as part of the
     * JWT decode process. To keep testing as simple as possible, we map only ONE
     * role to ONE user and provide here no combinations.
     */
    @Bean
    JwtDecoder jwtDecoder() {
        JwtDecoder jwtDecoder = mock();
        when(jwtDecoder.decode(anyString())).thenAnswer(invocation -> {
            String jwtTokenValue = invocation.getArgument(0);
            Jwt.Builder builder = Jwt.withTokenValue(jwtTokenValue).header(ALGORITHM, ALGORITHM_NONE);
            if (ADMIN_JWT.equals(jwtTokenValue)) {
                return builder.subject(ADMIN_ID).build();
            }
            if (ADMIN_OWNER_JWT.equals(jwtTokenValue)) {
                return builder.subject(ADMIN_OWNER_ID).build();
            }
            if (ADMIN_USER_JWT.equals(jwtTokenValue)) {
                return builder.subject(ADMIN_USER_ID).build();
            }
            if (OWNER_JWT.equals(jwtTokenValue)) {
                return builder.subject(OWNER_ID).build();
            }
            if (OWNER_USER_JWT.equals(jwtTokenValue)) {
                return builder.subject(OWNER_USER_ID).build();
            }
            if (USER_JWT.equals(jwtTokenValue)) {
                return builder.subject(USER_ID).build();
            }

            throw new JwtException("Invalid JWT token");
        });
        return jwtDecoder;
    }

    /**
     * Here we mock the {@link UserDetailsService} to return a
     * {@link TestUserDetails} object based on the user id (or subject). The subject
     * is determined by the {@link TestOAuth2JwtSecurityConfiguration#jwtDecoder()}
     * bean. Depending on the user id, the {@link TestUserDetails} object will
     * contain the corresponding authorities.
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

    public static String createJwtAuthHeader(Set<String> roles) {
        if (roles.isEmpty()) {
            throw new TestAbortedException("Roles cannot be empty");
        }

        if (roles.equals(Set.of(SUPERADMIN))) {
            return BEARER_PREFIX + ADMIN_JWT;
        }

        if (roles.equals(Set.of(SUPERADMIN, OWNER))) {
            return BEARER_PREFIX + ADMIN_OWNER_JWT;
        }

        if (roles.equals(Set.of(SUPERADMIN, USER))) {
            return BEARER_PREFIX + ADMIN_USER_JWT;
        }

        if (roles.equals(Set.of(OWNER))) {
            return BEARER_PREFIX + OWNER_JWT;
        }

        if (roles.equals(Set.of(OWNER, USER))) {
            return BEARER_PREFIX + OWNER_USER_JWT;
        }

        if (roles.equals(Set.of(USER))) {
            return BEARER_PREFIX + USER_JWT;
        }

        throw new TestAbortedException("Invalid roles");
    }

}
