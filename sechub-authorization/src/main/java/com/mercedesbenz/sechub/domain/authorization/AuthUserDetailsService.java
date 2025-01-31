// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.authorization;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.sharedkernel.security.AuthorityConstants;
import com.mercedesbenz.sechub.sharedkernel.security.RoleConstants;

/**
 * <p>
 * Service class responsible for loading user-specific data from the database
 * and enriching authentication with SecHub-specific user roles. Implements the
 * {@link UserDetailsService} interface used by Spring Security.
 * </p>
 *
 * <p>
 * Usually the {@link UserDetailsService} is is used by Spring Security Basic
 * Auth. However, in our case it is also used by OAuth2. We do this to
 * centralize the user authentication logic so that we have a single source of
 * truth for user roles and permissions.
 * </p>
 *
 * @see UserDetailsService
 * @see AuthUserRepository
 * @see AuthUser
 * @see UserDetails
 *
 * @author Albert Tregnaghi, hamidonos
 */
@Service
public class AuthUserDetailsService implements UserDetailsService {

    private final AuthUserRepository repository;
    private static final Logger LOG = LoggerFactory.getLogger(AuthUserDetailsService.class);
    private static final String NOOP_DUMMY_PASSWORD_FORMAT = "{noop}DUMMY-%s";

    AuthUserDetailsService(AuthUserRepository repository) {
        this.repository = repository;
    }

    @Override
    public AuthUserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        /* @formatter:off */
        return repository
                .findByUserId(username.toLowerCase())
                .map(AuthUserDetailsService::adoptUser)
                .orElseThrow(() -> new UsernameNotFoundException(username));
        /* @formatter:on */
    }

    static AuthUserDetails adoptUser(AuthUser entity) {
        AuthUserDetails.Builder builder = AuthUserDetails.BUILDER;

        builder.username(entity.getUserId());
        String hashedApiToken = entity.getHashedApiToken();

        if (hashedApiToken == null) {
            /*
             * This is a fallback for authentication with OAuth2, because the OAuth2 way
             * does not require to have a hashed api token in the database. To make Spring
             * Security not throw an exception because of a missing password we set a dummy
             * throwaway password here that can never be used for actual authentication.
             */
            String randomThrowAwayPassword = UUID.randomUUID().toString();
            String dummyApiToken = NOOP_DUMMY_PASSWORD_FORMAT.formatted(randomThrowAwayPassword);
            builder.password(dummyApiToken);
        } else {
            /*
             * Here we have the normal case where a hashed api token is in the database.
             * This can be true for both Basic Auth and OAuth2.
             */
            builder.password(hashedApiToken);
        }

        /* user is enabled if api token is present */
        boolean enabled = hashedApiToken != null && !hashedApiToken.isEmpty();
        builder.enabled(enabled);

        /* SecHub user is never locked, expired or has expired credentials */
        builder.accountNonLocked(true);
        builder.accountNonExpired(true);
        builder.credentialsNonExpired(true);

        Collection<GrantedAuthority> authorities = accumulateAuthorities(entity);
        builder.authorities(authorities);

        AuthUserDetails details = builder.build();

        LOG.trace("User:{} has authorities: {}, entity:{}", entity.getUserId(), details.getAuthorities(), entity);

        return details;
    }

    private static Collection<GrantedAuthority> accumulateAuthorities(AuthUser entity) {
        Collection<GrantedAuthority> authorities = new ArrayList<>();

        if (entity.isRoleUser()) {
            authorities.add(() -> AuthorityConstants.AUTHORITY_ROLE_PREFIX + RoleConstants.ROLE_USER);
        }
        if (entity.isRoleSuperAdmin()) {
            authorities.add(() -> AuthorityConstants.AUTHORITY_ROLE_PREFIX + RoleConstants.ROLE_SUPERADMIN);
        }
        if (entity.isRoleOwner()) {
            authorities.add(() -> AuthorityConstants.AUTHORITY_ROLE_PREFIX + RoleConstants.ROLE_OWNER);
        }

        return authorities;
    }
}
