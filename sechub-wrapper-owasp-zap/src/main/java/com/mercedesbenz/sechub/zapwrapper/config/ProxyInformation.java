// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.zapwrapper.config;

public class ProxyInformation {
    private String host;
    private int port;

    public ProxyInformation(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

}
