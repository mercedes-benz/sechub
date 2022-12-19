// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.adapter;

import java.net.URI;
import java.util.Set;

public abstract class AbstractWebScanAdapterConfig extends AbstractAdapterConfig implements WebScanAdapterConfig {

    AbstractLoginConfig loginConfig;
    SecHubTimeUnitData maxScanDuration;
    URI targetURI;
    URI rootTargetURI;
    String targetType;
    Set<String> includes;
    Set<String> excludes;

    public Set<String> getIncludes() {
        return includes;
    }

    public Set<String> getExcludes() {
        return excludes;
    }

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
