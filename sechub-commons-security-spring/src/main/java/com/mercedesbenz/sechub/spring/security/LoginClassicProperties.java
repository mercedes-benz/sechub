// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.spring.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = LoginClassicProperties.PREFIX)
public class LoginClassicProperties {

    static final String PREFIX = "sechub.security.login.classic";

}
