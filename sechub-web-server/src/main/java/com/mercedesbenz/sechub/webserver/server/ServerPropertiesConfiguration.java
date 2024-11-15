// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.webserver.server;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({ ServerProperties.class, ManagementServerProperties.class })
public class ServerPropertiesConfiguration {

}
