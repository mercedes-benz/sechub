// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.webserver.server;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

@ConfigurationProperties(prefix = ManagementServerProperties.PREFIX)
public final class ManagementServerProperties {

    static final String PREFIX = "management.server";

    private final int port;

    @ConstructorBinding
    ManagementServerProperties(int port) {
        this.port = port;
    }

    public int getPort() {
        return port;
    }
}