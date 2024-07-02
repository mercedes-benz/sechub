package com.mercedesbenz.sechub.wrapper.prepare;

import static com.mercedesbenz.sechub.wrapper.prepare.cli.PrepareWrapperKeyConstants.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PrepareWrapperProxySupport {

    private final static String UNDEFINED = "";

    @Value("${" + KEY_PDS_HTTPS_PROXY + ":" + UNDEFINED +"}")
    String httpsProxy;

    @Value("${" + KEY_PDS_NO_PROXY + ":" + UNDEFINED +"}")
    String noProxy;

    @Value("${" + KEY_PDS_PREPARE_PROXY_ENABLED + ":false}")
    boolean proxyEnabled;

    @Autowired
    PrepareWrapperSystemPropertySupport propertySupport;

    public void setUpProxy(String url) {
        if (!proxyEnabled) {
            return;
        }

        assertHttpsProxy();

        if (isProxyRequiredForURL(url)) {
            setProxySystemProperty();
        }
    }

    private boolean isProxyRequiredForURL(String url) {
        if (noProxy == null || noProxy.isBlank()) {
            return true;
        }
        String[] noProxyList = noProxy.split(",");

        for (String noProxy : noProxyList) {
            if (url.contains(noProxy)) {
                return false;
            }
        }
        return true;
    }

    private void assertHttpsProxy() {
        if (httpsProxy == null || httpsProxy.isBlank()) {
            throw new IllegalStateException(
                    "No HTTPS proxy is set. Please set the environment variable: " + KEY_PDS_HTTPS_PROXY + " with the format: <hostname>:<port>");
        }
    }

    private String resolveHostname() {
        return httpsProxy.split(":")[0];
    }

    private String resolvePort() {
        String [] splitProxy = httpsProxy.split(":");
        if (splitProxy.length < 2) {
            throw new IllegalStateException(
                    "No port number is set. Please set the environment variable: " + KEY_PDS_HTTPS_PROXY + " with the format: <hostname>:<port>");
        }
        String port = splitProxy[1];
        assertPort(port);
        return port;
    }

    private void setProxySystemProperty() {
        propertySupport.setSystemProperty("https.proxyHost", resolveHostname());
        propertySupport.setSystemProperty("https.proxyPort", resolvePort());
    }

    private void assertPort(String port) {
        if (port == null || port.isBlank()) {
            throw new IllegalStateException(
                    "No port number is set. Please set the environment variable: " + KEY_PDS_HTTPS_PROXY + " with the format: <hostname>:<port>");
        }
        if (port.chars().noneMatch(Character::isDigit)) {
            throw new IllegalStateException(
                    "Port number is not a number. Please set the environment variable: " + KEY_PDS_HTTPS_PROXY + " with the format: <hostname>:<port>");
        }
    }
}
