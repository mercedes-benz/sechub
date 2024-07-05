package com.mercedesbenz.sechub.wrapper.prepare;

import static com.mercedesbenz.sechub.wrapper.prepare.cli.PrepareWrapperKeyConstants.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PrepareWrapperProxySupport {

    private final static String UNDEFINED = "";

    @Value("${" + KEY_PDS_HTTPS_PROXY + ":" + UNDEFINED + "}")
    String httpsProxy;

    @Value("${" + KEY_PDS_NO_PROXY + ":" + UNDEFINED + "}")
    String noProxy;

    @Value("${" + KEY_PDS_PREPARE_PROXY_ENABLED + ":false}")
    boolean proxyEnabled;

    @Autowired
    PrepareWrapperSystemPropertySupport propertySupport;

    public void setUpProxy() {
        if (!proxyEnabled) {
            return;
        }

        assertHttpsProxy();
        setProxySystemProperty();
    }

    private String resolveHostname() {
        return httpsProxy.split(":")[0];
    }

    private String resolvePort() {
        String[] splitProxy = httpsProxy.split(":");
        if (splitProxy.length != 2) {
            throw new IllegalStateException(
                    "No port number is set. Please set the environment variable: " + KEY_PDS_HTTPS_PROXY + " with the format: <hostname>:<port>");
        }
        String port = splitProxy[1];
        assertPort(port);
        return port;
    }

    private String resolveNoProxy() {
        return noProxy.replace(",", "|");
    }

    private void assertHttpsProxy() {
        if (httpsProxy == null || httpsProxy.isBlank()) {
            throw new IllegalStateException(
                    "No HTTPS proxy is set. Please set the environment variable: " + KEY_PDS_HTTPS_PROXY + " with the format: <hostname>:<port>");
        }
    }

    private void assertPort(String port) {
        if (port == null || port.isBlank()) {
            throw new IllegalStateException(
                    "No port number is set. Please set the environment variable: " + KEY_PDS_HTTPS_PROXY + " with the format: <hostname>:<port>");
        }
        if (port.chars().noneMatch(Character::isDigit)) {
            throw new IllegalStateException("Port number " + port + " is not a number. Please set the environment variable: " + KEY_PDS_HTTPS_PROXY
                    + " with the format: <hostname>:<port>");
        }
    }

    private void setProxySystemProperty() {
        propertySupport.setSystemProperty("https.proxyHost", resolveHostname());
        propertySupport.setSystemProperty("https.proxyPort", resolvePort());
        propertySupport.setSystemProperty("https.nonProxyHosts", resolveNoProxy());
    }
}
