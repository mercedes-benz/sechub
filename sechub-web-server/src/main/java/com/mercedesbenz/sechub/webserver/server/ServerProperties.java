// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.webserver.server;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

@ConfigurationProperties(prefix = ServerProperties.PREFIX)
public final class ServerProperties {

    static final String PREFIX = "server";

    private final int port;

    @ConstructorBinding
    ServerProperties(int port) {
        this.port = port;
    }

    public int getPort() {
        return port;
    }
}