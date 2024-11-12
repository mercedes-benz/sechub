// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.testframework.spring;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.opentest4j.TestAbortedException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;

import com.mercedesbenz.sechub.domain.authorization.AuthUser;
import com.mercedesbenz.sechub.domain.authorization.AuthUserDetailsService;
import com.mercedesbenz.sechub.domain.authorization.AuthUserRepository;

/**
 * This configuration class provides the necessary beans to test Springs OAuth2
 * integration with SecHub components.
 *
 * @author hamidonos
 */
@Configuration
@Import({ AuthUserDetailsService.class })
public class OAuth2SecurityTestConfiguration {

    public static final String BEARER_PREFIX = OAuth2AccessToken.TokenType.BEARER.getValue() + " ";

    public static final String ADMIN = "SUPERADMIN";
    public static final String OWNER = "OWNER";
    public static final String USER = "USER";

    private static final String ADMIN_JWT = "admin-jwt";
    private static final String OWNER_JWT = "owner-jwt";
    private static final String USER_JWT = "user-jwt";

    private static final String ADMIN_ID = UUID.randomUUID().toString();
    private static final String OWNER_ID = UUID.randomUUID().toString();
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
            if (OWNER_JWT.equals(jwtTokenValue)) {
                return builder.subject(OWNER_ID).build();
            }
            if (USER_JWT.equals(jwtTokenValue)) {
                return builder.subject(USER_ID).build();
            }

            throw new JwtException("Invalid JWT token");
        });
        return jwtDecoder;
    }

    /**
     * Here we mock the {@link AuthUserRepository} to return a {@link AuthUser}
     * object based on the user id (or subject). The subject is determined by the
     * {@link com.mercedesbenz.sechub.testframework.spring.OAuth2SecurityTestConfiguration#jwtDecoder()}
     * bean. Depending on the user id, the {@link AuthUser} object will have the
     * corresponding role enabled.
     */
    @Bean
    AuthUserRepository authUserRepository() {
        AuthUserRepository authUserRepository = mock();
        when(authUserRepository.findByUserId(anyString())).thenAnswer(invocation -> {
            String userId = invocation.getArgument(0);
            if (!Set.of(ADMIN_ID, OWNER_ID, USER_ID).contains(userId)) {
                return Optional.empty();
            }
            AuthUser authUser = new AuthUser();
            authUser.setUserId(userId);
            if (ADMIN_ID.equals(userId)) {
                authUser.setRoleUser(true);
            }
            if (OWNER_ID.equals(userId)) {
                authUser.setRoleOwner(true);
            }
            if (USER_ID.equals(userId)) {
                authUser.setRoleSuperAdmin(true);
            }
            return Optional.of(authUser);
        });
        return authUserRepository;
    }

    public static String getJwtAuthHeader(String role) {
        return BEARER_PREFIX + switch (role) {
        case ADMIN -> ADMIN_JWT;
        case OWNER -> OWNER_JWT;
        case USER -> USER_JWT;
        default -> throw new TestAbortedException("Invalid role");
        };
    }

}
