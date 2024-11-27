package com.mercedesbenz.sechub.spring.security;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(OAuth2JwtProperties.class)
@ConditionalOnProperty(prefix = AbstractSecurityConfiguration.OAUTH2_PROPERTIES_PREFIX, name = AbstractSecurityConfiguration.OAUTH2_PROPERTIES_MODE, havingValue = OAuth2JwtPropertiesConfiguration.MODE)
class OAuth2JwtPropertiesConfiguration {
    static final String MODE = "JWT";
}
