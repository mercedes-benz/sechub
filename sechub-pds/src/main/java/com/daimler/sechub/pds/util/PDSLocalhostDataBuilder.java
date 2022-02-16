// SPDX-License-Identifier: MIT
package com.daimler.sechub.pds.util;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PDSLocalhostDataBuilder {

    @Value("${server.port}") // same as used by spring boot for HTTP server - so always available
    private int serverPort;

    public String buildHostname() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return "localhost";
        }
    }

    public String buildIP() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            return "127.0.0.1";
        }
    }

    public int buildPort() {
        return serverPort;
    }
}
