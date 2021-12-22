// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter;

import java.net.URI;

public abstract class AbstractWebScanAdapterConfig extends AbstractAdapterConfig implements WebScanAdapterConfig {

    AbstractLoginConfig loginConfig;
    SecHubTimeUnitData maxScanDuration;
    URI targetURI;
    private String targetType;

    public SecHubTimeUnitData getMaxScanDuration() {
        return maxScanDuration;
    }

    public LoginConfig getLoginConfig() {
        return loginConfig;
    }

    @Override
    public boolean hasMaxScanDuration() {
        return maxScanDuration != null;
    }

    @Override
    public String getTargetType() {
        if (targetType == null) {
            return "";
        }
        return targetType;
    }
    
    @Override
    public URI getTargetURI() {
        return targetURI;
    }
    
    @Override
    public String getTargetAsString() {
        URI uri = getTargetURI();
        if (uri == null) {
            return null;
        }
        return uri.toString();
    }
}
