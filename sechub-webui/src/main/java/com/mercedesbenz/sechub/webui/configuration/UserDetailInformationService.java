// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.webui.configuration;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class UserDetailInformationService {
    public UserDetails getUser() {
        return User.withDefaultPasswordEncoder().username("user").password("password").roles("USER").build();
    }
}