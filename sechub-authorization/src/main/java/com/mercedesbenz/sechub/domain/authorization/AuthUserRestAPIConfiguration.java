// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.authorization;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetailsService;

@Configuration
class AuthUserRestAPIConfiguration {

    @Bean
    UserDetailsService userDetailsService(final AuthUserRepository repository) {
        return new AuthUserDetailsService(repository);
    }

}
