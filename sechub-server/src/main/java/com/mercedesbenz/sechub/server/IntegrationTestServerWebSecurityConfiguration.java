// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.server;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;

import com.mercedesbenz.sechub.sharedkernel.Profiles;

@Configuration
@EnableMethodSecurity(jsr250Enabled = true)
@EnableWebSecurity
@Order(4000)
@Profile(Profiles.INTEGRATIONTEST)
public class IntegrationTestServerWebSecurityConfiguration {

    @Bean
    public WebSecurityCustomizer ignoreIntegrationtestAPI(HttpSecurity httpSecurity) throws Exception {
        return (web) -> web.ignoring().requestMatchers("api/integrationtest/*");
    }
}
