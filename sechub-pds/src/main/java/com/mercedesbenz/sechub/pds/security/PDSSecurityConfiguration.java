// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;

import com.mercedesbenz.sechub.pds.PDSMustBeDocumented;

@Configuration
@EnableMethodSecurity(jsr250Enabled = true)
@EnableWebSecurity
@Order(1)
public class PDSSecurityConfiguration {

    /*
     * TODO Albert Tregnaghi, 2020-06-18: extreme simple approach: we just allow ONE
     * user at the moment which is a technical user only. Enough for communication
     * at the beginning, but should be improved later
     */

    @PDSMustBeDocumented(value = "Techuser user id", scope = "credentials")
    @Value("${pds.techuser.userid}")
    String techUserId;

    @PDSMustBeDocumented(value = "Techuser user api token", scope = "credentials")
    @Value("${pds.techuser.apitoken}")
    String techUserApiToken;

    /*
     * TODO Albert Tregnaghi, 2020-07-05: extreme simple approach: we just allow ONE
     * admin at the moment Enough for communication at the beginning, but should be
     * improved later
     */

    @PDSMustBeDocumented(value = "Administrator user id", scope = "credentials")
    @Value("${pds.admin.userid}")
    String adminUserId;

    @PDSMustBeDocumented(value = "Administrator api token", scope = "credentials")
    @Value("${pds.admin.apitoken}")
    String adminApiToken;

    @Bean
    public UserDetailsManager userDetailsService() {
        /* @formatter:off */

        PDSPasswordTransformer pdsPasswordTransformer = new PDSPasswordTransformer();

        UserDetails user =
                User.builder()
                        .username(techUserId)
                        .password(pdsPasswordTransformer.transformPassword(techUserApiToken))
                        .roles(PDSRoles.USER.getRole())
                        .build();
        /* remove field after start */
        techUserApiToken = null;

        UserDetails admin =
                User.builder()
                        .username(adminUserId)
                        .password(pdsPasswordTransformer.transformPassword(adminApiToken))
                        .roles(PDSRoles.SUPERADMIN.getRole())
                        .build();
        /* remove field after start */
        adminApiToken = null;
        /* @formatter:on */
        return new InMemoryUserDetailsManager(user, admin);
    }
}