// SPDX-License-Identifier: MIT
package com.daimler.sechub.server;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

import com.daimler.sechub.sharedkernel.Profiles;

@Configuration
@EnableGlobalMethodSecurity(jsr250Enabled = true)
@EnableWebSecurity
@Order(4000)
@Profile(Profiles.INTEGRATIONTEST)
public class IntegrationTestServerWebSecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Override
    public void configure(WebSecurity web) {
        /* api for integration test is always allowed */
        web.ignoring().antMatchers("api/integrationtest/*");
    }
}
