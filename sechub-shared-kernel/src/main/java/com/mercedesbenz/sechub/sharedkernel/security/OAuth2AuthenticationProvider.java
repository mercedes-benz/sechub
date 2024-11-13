// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.security;

import static java.util.Objects.requireNonNull;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthenticationToken;

/**
 * <p>
 * This class integrates authentication and authorization in SecHub by combining
 * OAuth2-based authentication with custom
 * {@link com.mercedesbenz.sechub.domain.authorization.AuthUserDetailsService}
 * for authorization. While OAuth2 manages the authentication process, our
 * system fetches roles and permissions from the database to handle
 * authorization.
 * </p>
 *
 * <p>
 * The {@link org.springframework.security.oauth2.jwt.JwtDecoder} is employed to
 * decode the JWT token, extracting the username by interacting with the
 * identity provider. This username is then utilized to retrieve user details
 * from the user details service. These details are subsequently used to create
 * a
 * {@link org.springframework.security.authentication.UsernamePasswordAuthenticationToken},
 * which encapsulates information about the authenticated user and their roles.
 * </p>
 *
 * @see com.mercedesbenz.sechub.domain.authorization.AuthUserDetailsService
 * @see org.springframework.security.oauth2.jwt.JwtDecoder
 * @see org.springframework.security.core.userdetails.UserDetailsService
 * @see org.springframework.security.authentication.AuthenticationProvider
 *
 * @author hamidonos
 */
@SuppressWarnings("JavadocReference")
class OAuth2AuthenticationProvider implements AuthenticationProvider {

    private final UserDetailsService userDetailsService;
    private final JwtDecoder jwtDecoder;

    public OAuth2AuthenticationProvider(UserDetailsService userDetailsService, JwtDecoder jwtDecoder) {
        this.userDetailsService = requireNonNull(userDetailsService, "Property userDetailsService must not be null");
        this.jwtDecoder = requireNonNull(jwtDecoder, "Property jwtDecoder must not be null");
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        if (!(authentication instanceof BearerTokenAuthenticationToken bearerToken)) {
            return null;
        }

        Jwt jwt;
        try {
            jwt = jwtDecoder.decode(bearerToken.getToken());
        } catch (Exception e) {
            throw new BadCredentialsException("The presented JWT could not be decoded", e);
        }
        String username = jwt.getSubject();
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return BearerTokenAuthenticationToken.class.isAssignableFrom(authentication);
    }

}