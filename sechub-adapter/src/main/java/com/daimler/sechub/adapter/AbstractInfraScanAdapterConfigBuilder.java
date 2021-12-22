package com.daimler.sechub.adapter;

import java.net.InetAddress;
import java.net.URI;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public abstract class AbstractInfraScanAdapterConfigBuilder<B extends AbstractInfraScanAdapterConfigBuilder<B, C>, C extends AbstractInfraScanAdapterConfig>
extends AbstractAdapterConfigBuilder<B, C> {
    private LinkedHashSet<URI> targetURIs = new LinkedHashSet<>();

    private LinkedHashSet<InetAddress> targetIPs = new LinkedHashSet<>();
    
    @SuppressWarnings("unchecked")
    public B setTargetURIs(Set<URI> targetURIs) {
        if (targetURIs == null) {
            this.targetURIs = new LinkedHashSet<>();
        } else {
            this.targetURIs = new LinkedHashSet<>();
            this.targetURIs.addAll(targetURIs);
        }
        return (B) this;
    }

    @SuppressWarnings("unchecked")
    public B setTargetURI(URI targetURI) {
        if (targetURI == null) {
            return (B) this;
        }
        return setTargetURIs(Collections.singleton(targetURI));
    }
    
    @SuppressWarnings("unchecked")
    public B setTargetIPs(Set<InetAddress> targetIPs) {
        if (targetIPs == null) {
            this.targetIPs = new LinkedHashSet<>();
        } else {
            this.targetIPs = new LinkedHashSet<>();
            this.targetIPs.addAll(targetIPs);
        }
        return (B) this;
    }

    @SuppressWarnings("unchecked")
    public B setTargetIP(InetAddress ipAdress) {
        if (ipAdress == null) {
            return (B) this;
        }
        return setTargetIPs(Collections.singleton(ipAdress));
    }
}
