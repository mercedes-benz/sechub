// SPDX-License-Identifier: MIT
package com.daimler.sechub.sharedkernel.util;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.springframework.stereotype.Component;

@Component
public class HostnameBuilder {

    public String buildHostname() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return "localhost";
        }
    }
}
