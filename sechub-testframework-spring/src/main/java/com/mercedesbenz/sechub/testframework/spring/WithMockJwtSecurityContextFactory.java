// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.testframework.spring;

import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

/**
 * {@code WithMockJwtSecurityContextFactory} is a custom
 * {@link WithSecurityContextFactory} implementation that sets up a mock
 * security context with a JWT token for testing purposes.
 *
 * <p>
 * This factory is used in conjunction with the {@link WithMockJwtUser}
 * annotation to create a {@link SecurityContext} containing a
 * {@link BearerTokenAuthenticationToken} with the specified JWT token. This
 * allows for simulating an authenticated user in test scenarios.
 * </p>
 *
 * @see WithMockJwtUser
 * @see WithSecurityContextFactory
 * @see SecurityContext
 * @see JwtAuthenticationToken
 * @see Jwt
 *
 * @author hamidonos
 */
public class WithMockJwtSecurityContextFactory implements WithSecurityContextFactory<WithMockJwtUser> {

    @Override
    public SecurityContext createSecurityContext(WithMockJwtUser withMockJwtUser) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        String jwt = withMockJwtUser.jwt();
        BearerTokenAuthenticationToken authenticationToken = new BearerTokenAuthenticationToken(jwt);
        context.setAuthentication(authenticationToken);
        return context;
    }
}
