// SPDX-License-Identifier: MIT
package com.daimler.sechub.pds.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

import com.daimler.sechub.pds.PDSMustBeDocumented;

@Configuration
@EnableGlobalMethodSecurity(jsr250Enabled = true)
@EnableWebSecurity
@Order(1)
public class PDSSecurityConfiguration extends AbstractAllowPDSAPISecurityConfiguration {

    /* TODO Albert Tregnaghi, 2020-06-18: extreme simple approach: we just allow ONE user at the moment
     * which is a technical user only. Enough for communication at the beginning, but should be improved later */
    
    @PDSMustBeDocumented(value="Techuser user id",scope="credentials")
    @Value("${sechub.pds.techuser.userid}")
    String techUserId;
    
    @PDSMustBeDocumented(value="Techuser user api token",scope="credentials")
    @Value("${sechub.pds.techuser.apitoken}")
    String techUserApiToken;
    
    /* TODO Albert Tregnaghi, 2020-07-05: extreme simple approach: we just allow ONE admin at the moment
     * Enough for communication at the beginning, but should be improved later */
    
    @PDSMustBeDocumented(value="Administrator user id",scope="credentials")
    @Value("${sechub.pds.admin.userid}")
    String adminUserId;
    
    @PDSMustBeDocumented(value="Administrator api token",scope="credentials")
    @Value("${sechub.pds.admin.apitoken}")
    String adminApiToken;
    
    @Bean
    @Override
    public UserDetailsService userDetailsService() {
        /* @formatter:off */
        UserDetails user =
             User.builder()
                .username(techUserId)
                .password(techUserApiToken)
                .roles(PDSRoles.USER.getRole())
                .build();
        /* remove field after start */
        techUserApiToken=null;
        
        UserDetails admin =
                User.builder()
                   .username(adminUserId)
                   .password(adminApiToken)
                   .roles(PDSRoles.SUPERADMIN.getRole())
                   .build();
           /* remove field after start */
        adminApiToken=null;
        /* @formatter:on */
        return new InMemoryUserDetailsManager(user,admin);
    }

}