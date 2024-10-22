// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.webserver.security;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;

import com.mercedesbenz.sechub.webserver.encryption.AES256Encryption;

@TestConfiguration
@Import({ SecurityConfiguration.class, OAuth2PropertiesConfig.class, AES256Encryption.class })
public class SecurityTestConfiguration {
}
