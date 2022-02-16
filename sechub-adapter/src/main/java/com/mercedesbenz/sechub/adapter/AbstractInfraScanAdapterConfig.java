package com.mercedesbenz.sechub.adapter;

import java.net.InetAddress;
import java.net.URI;
import java.util.LinkedHashSet;
import java.util.Set;

public abstract class AbstractInfraScanAdapterConfig extends AbstractAdapterConfig implements InfraScanAdapterConfig {
    Set<URI> targetURIs = new LinkedHashSet<>();

    Set<URI> rootTargetUris = new LinkedHashSet<>();
    Set<InetAddress> targetIPs = new LinkedHashSet<>();

    private String targetType;

    @Override
    public String getTargetType() {
        if (targetType == null) {
            return "";
        }
        return targetType;
    }

    @Override
    public final Set<URI> getTargetURIs() {
        return targetURIs;
    }

    public String getTargetAsString() {
        if (targetURIs == null || targetURIs.isEmpty()) {
            return null;
        }
        return targetURIs.iterator().next().toString();
    }

    @Override
    public final Set<InetAddress> getTargetIPs() {
        return targetIPs;
    }

    @Override
    public Set<URI> getRootTargetURIs() {
        return rootTargetUris;
    }
}
