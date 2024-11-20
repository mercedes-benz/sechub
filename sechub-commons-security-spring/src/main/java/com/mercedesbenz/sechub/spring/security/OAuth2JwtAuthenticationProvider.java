// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.spring.security;

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
 * This class integrates security in SecHub by combining OAuth2-based
 * authentication with authorization from the {@link UserDetailsService}. While
 * OAuth2 manages the authentication process, the user details service is
 * responsible for providing application specific user details and authorities.
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
 * @see org.springframework.security.oauth2.jwt.JwtDecoder
 * @see org.springframework.security.core.userdetails.UserDetailsService
 * @see org.springframework.security.authentication.AuthenticationProvider
 *
 * @author hamidonos
 */
public class OAuth2JwtAuthenticationProvider implements AuthenticationProvider {

    private final UserDetailsService userDetailsService;
    private final JwtDecoder jwtDecoder;

    public OAuth2JwtAuthenticationProvider(UserDetailsService userDetailsService, JwtDecoder jwtDecoder) {
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