// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter;

import java.net.URI;

public abstract class AbstractWebScanAdapterConfig extends AbstractAdapterConfig implements WebScanAdapterConfig {

    AbstractLoginConfig loginConfig;
    SecHubTimeUnitData maxScanDuration;
    URI targetURI;
    URI rootTargetURI;
    String targetType;

    public SecHubTimeUnitData getMaxScanDuration() {
        return maxScanDuration;
    }

    public LoginConfig getLoginConfig() {
        return loginConfig;
    }

    public boolean hasMaxScanDuration() {
        return maxScanDuration != null;
    }

    public String getTargetType() {
        if (targetType == null) {
            return "";
        }
        return targetType;
    }
    
    public URI getTargetURI() {
        return targetURI;
    }
    
    public String getRootTargetURIasString() {
        URI uri = getRootTargetURI();
        if (uri == null) {
            return null;
        }
        return uri.toString();
    }

    public URI getRootTargetURI() {
        return rootTargetURI;
    }
    
    public String getTargetAsString() {
        URI uri = getTargetURI();
        if (uri == null) {
            return null;
        }
        return uri.toString();
    }
}
