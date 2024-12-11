// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.spring.security;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(OAuth2OpaqueTokenProperties.class)
@ConditionalOnProperty(prefix = AbstractSecurityConfiguration.OAUTH2_PROPERTIES_PREFIX, name = AbstractSecurityConfiguration.OAUTH2_PROPERTIES_MODE, havingValue = OAuth2OpaqueTokenPropertiesConfiguration.MODE)
class OAuth2OpaqueTokenPropertiesConfiguration {
    static final String MODE = "OPAQUE_TOKEN";
}