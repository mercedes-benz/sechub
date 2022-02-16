// SPDX-License-Identifier: MIT
package com.daimler.sechub.server;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

import com.daimler.sechub.sharedkernel.configuration.AbstractAllowSecHubAPISecurityConfiguration;

@Configuration
@EnableGlobalMethodSecurity(jsr250Enabled = true)
@EnableWebSecurity
@Order(1)
public class SecHubServerSecurityConfiguration extends AbstractAllowSecHubAPISecurityConfiguration {

}