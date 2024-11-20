// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.spring.security;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(OAuth2OpaqueTokenProperties.class)
@ConditionalOnProperty(prefix = OAuth2OpaqueTokenProperties.PREFIX, name = "enabled", havingValue = "true")
class OAuth2OpaqueTokenPropertiesConfiguration {
}
