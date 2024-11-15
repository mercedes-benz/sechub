// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.server;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.mercedesbenz.sechub.sharedkernel.Profiles;
import com.mercedesbenz.sechub.sharedkernel.security.OAuth2Properties;

@Configuration
@EnableConfigurationProperties(OAuth2Properties.class)
@Profile(Profiles.OAUTH2)
class SecHubServerOAuth2PropertiesConfig {
}
