// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.webui.page.user;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class UserDetailInformationService {
    public UserDetails getUser() {
        /* FIXME Albert Tregnaghi, 2024-02-28:implement real user management */
        return User.withDefaultPasswordEncoder().username("user").password("password").roles("USER").build();
    }
}