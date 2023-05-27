// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.webui.configuration;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class UserDetailInformationService {

    public UserDetails getUser() {
    	/* @formatter:off */
        return User.withDefaultPasswordEncoder()
        		.username("user")
        		.password("password")
        		.roles("USER")
        		.build();
        /* @formatter:off */
    }
    
    public UserDetails getAdmin() {
    	/* @formatter:off */
        return User.withDefaultPasswordEncoder()
        		.username("admin")
        		.password("password")
        		.roles("ADMIN", "USER")
        		.build();
        /* @formatter:off */
    }
}