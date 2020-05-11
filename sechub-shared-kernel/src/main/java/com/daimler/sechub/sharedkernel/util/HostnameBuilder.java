package com.daimler.sechub.sharedkernel.util;

import java.net.InetAddress;

import org.springframework.stereotype.Component;

@Component
public class HostnameBuilder {

    public String buildHostname() {
        return InetAddress.getLoopbackAddress().getHostName();
    }
}
