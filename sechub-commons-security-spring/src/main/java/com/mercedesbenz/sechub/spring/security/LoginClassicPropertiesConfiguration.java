// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.spring.security;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(LoginClassicProperties.class)
@ConditionalOnProperty(prefix = AbstractSecurityConfiguration.LOGIN_PROPERTIES_PREFIX, name = "enabled", havingValue = "true")
class LoginClassicPropertiesConfiguration {
}