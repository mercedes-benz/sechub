// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.spring.security;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = LoginProperties.PREFIX, name = "enabled", havingValue = "true")
@EnableConfigurationProperties(LoginProperties.class)
class LoginPropertiesConfiguration {
}
