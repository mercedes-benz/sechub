// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.authorization;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.User.UserBuilder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.mercedesbenz.sechub.sharedkernel.AuthorityConstants;
import com.mercedesbenz.sechub.sharedkernel.RoleConstants;

@Configuration
public class AuthUserRestAPIConfiguration {

    private static final Logger LOG = LoggerFactory.getLogger(AuthUserRestAPIConfiguration.class);

    @Autowired
    PasswordEncoder passwordEncoder;

    @Bean
    public UserDetailsService userDetailsService(final AuthUserRepository repository) {
        /* @formatter:off */
		return userid -> repository.
				findByUserId(userid).
				map(AuthUserRestAPIConfiguration::adoptUser).
				orElseThrow(()->new UsernameNotFoundException(userid));
		/* @formatter:on */
    }

    static UserDetails adoptUser(AuthUser entity) {
        UserBuilder builder = User.builder();
        builder.username(entity.getUserId());
        String hashedApiToken = entity.getHashedApiToken();
        builder.password(hashedApiToken);

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
