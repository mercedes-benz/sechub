// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.webserver.security;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;

@TestConfiguration
@Import({ SecurityConfiguration.class, OAuth2PropertiesConfig.class })
public class SecurityTestConfiguration {
}
