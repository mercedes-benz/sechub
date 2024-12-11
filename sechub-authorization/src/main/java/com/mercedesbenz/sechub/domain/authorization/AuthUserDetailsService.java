// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.authorization;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        /* @formatter:off */
        return repository
                .findByUserId(username.toLowerCase())
                .map(AuthUserDetailsService::adoptUser)
                .orElseThrow(() -> new UsernameNotFoundException(username));
        /* @formatter:on */
    }

    static UserDetails adoptUser(AuthUser entity) {
        User.UserBuilder builder = User.builder();
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

        List<String> authorities = accumulateAuthorities(entity);
        builder.authorities(authorities.toArray(new String[authorities.size()]));

        /* when api token is empty or null then access is disabled */
        boolean disabled = hashedApiToken == null || hashedApiToken.isEmpty();
        builder.disabled(disabled);
        UserDetails details = builder.build();
        LOG.trace("User:{} has authorities: {}, entity:{}", entity.getUserId(), details.getAuthorities(), entity);
        return details;
    }

    private static List<String> accumulateAuthorities(AuthUser entity) {
        List<String> authorities = new ArrayList<String>();

        if (entity.isRoleUser()) {
            authorities.add(AuthorityConstants.AUTHORITY_ROLE_PREFIX + RoleConstants.ROLE_USER);
        }
        if (entity.isRoleSuperAdmin()) {
            authorities.add(AuthorityConstants.AUTHORITY_ROLE_PREFIX + RoleConstants.ROLE_SUPERADMIN);
        }
        if (entity.isRoleOwner()) {
            authorities.add(AuthorityConstants.AUTHORITY_ROLE_PREFIX + RoleConstants.ROLE_OWNER);
        }
        return authorities;
    }
}
