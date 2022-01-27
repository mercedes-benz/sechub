package com.daimler.sechub.adapter;

import java.net.InetAddress;
import java.net.URI;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public abstract class AbstractInfraScanAdapterConfigBuilder<B extends AbstractInfraScanAdapterConfigBuilder<B, C>, C extends InfraScanAdapterConfig>
extends AbstractAdapterConfigBuilder<B, C> 
{
    private LinkedHashSet<URI> targetURIs = new LinkedHashSet<>();
    private LinkedHashSet<URI> rootTargetURIs = new LinkedHashSet<>();

    private LinkedHashSet<InetAddress> targetIPs = new LinkedHashSet<>();
    
    protected AbstractInfraScanAdapterConfigBuilder() {
        super();
    }
    
    @SuppressWarnings("unchecked")
    public B setTargetURIs(Set<URI> targetURIs) {
        if (targetURIs == null) {
            this.targetURIs = new LinkedHashSet<>();
        } else {
            this.targetURIs = new LinkedHashSet<>();
            this.targetURIs.addAll(targetURIs);
            
            this.rootTargetURIs = new LinkedHashSet<>();
            this.rootTargetURIs.addAll(uriShrinkSupport.shrinkToRootURIs(targetURIs));
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
    
    @Override
    void packageInternalCustomBuild(C config) {
        if (! (config instanceof AbstractInfraScanAdapterConfig)) {
            throw new IllegalArgumentException("Wrong config type. Your config is of type " + config.getClass().getName() + " but should be " + AbstractCodeScanAdapterConfig.class.getSimpleName());
        }
        
        AbstractInfraScanAdapterConfig abstractInfraScanConfig = (AbstractInfraScanAdapterConfig) config;
        
        abstractInfraScanConfig.rootTargetUris = rootTargetURIs;
        abstractInfraScanConfig.targetURIs = targetURIs;
        abstractInfraScanConfig.targetIPs = targetIPs;
    }
}
