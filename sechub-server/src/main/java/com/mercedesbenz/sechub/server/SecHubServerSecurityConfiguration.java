// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.server;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

import com.mercedesbenz.sechub.sharedkernel.security.AbstractSecHubAPISecurityConfiguration;

@Configuration
@EnableMethodSecurity(jsr250Enabled = true)
@EnableWebSecurity
@Order(1)
public class SecHubServerSecurityConfiguration extends AbstractSecHubAPISecurityConfiguration {

}