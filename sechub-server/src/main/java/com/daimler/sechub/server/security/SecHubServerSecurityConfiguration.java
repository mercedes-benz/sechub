// SPDX-License-Identifier: MIT
package com.daimler.sechub.server.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

import com.daimler.sechub.sharedkernel.Profiles;
import com.daimler.sechub.sharedkernel.configuration.AbstractAllowSecHubAPISecurityConfiguration;

@Configuration
@EnableGlobalMethodSecurity(jsr250Enabled = true)
@EnableWebSecurity
@Profile({"!"+Profiles.DEMOMODE}) // not for demomode
@Order(1)
public class SecHubServerSecurityConfiguration extends AbstractAllowSecHubAPISecurityConfiguration {


}