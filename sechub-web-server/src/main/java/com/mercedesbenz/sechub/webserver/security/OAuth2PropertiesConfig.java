// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.webserver.security;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.mercedesbenz.sechub.webserver.ApplicationProfiles;

/**
 * The
 * {@link org.springframework.boot.context.properties.ConfigurationProperties}
 * annotation does not support the {@link Profile} annotation. To ensure that
 * the properties are only loaded when the
 * {@link ApplicationProfiles#OAUTH2_ENABLED} profile is active, this separate
 * configuration class is created with the {@link Profile} annotation.
 *
 * @author hamidonos
 */
@Configuration
@Profile(ApplicationProfiles.OAUTH2_ENABLED)
@EnableConfigurationProperties(OAuth2Properties.class)
class OAuth2PropertiesConfig {
}
