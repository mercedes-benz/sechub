// SPDX-License-Identifier: MIT
package com.daimler.sechub.pds;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

@Configuration
@EnableGlobalMethodSecurity(jsr250Enabled = true)
@EnableWebSecurity
@Order(1)
public class PDSSecurityConfiguration extends AbstractAllowPDSAPISecurityConfiguration {


}