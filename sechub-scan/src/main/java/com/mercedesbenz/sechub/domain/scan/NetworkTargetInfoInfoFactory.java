// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan;

import java.net.InetAddress;
import java.net.URI;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mercedesbenz.sechub.domain.scan.NetworkTargetRegistry.NetworkTargetInfo;
import com.mercedesbenz.sechub.domain.scan.resolve.TargetResolver;
import com.mercedesbenz.sechub.sharedkernel.UUIDTraceLogID;

public class NetworkTargetInfoInfoFactory {

    private static final Logger LOG = LoggerFactory.getLogger(NetworkTargetInfoInfoFactory.class);

    private TargetResolver targetResolver;
    private String identifier;

    public NetworkTargetInfoInfoFactory(TargetResolver targetResolver, String identifier) {
        this.targetResolver = targetResolver;
        this.identifier = identifier;
    }

    public NetworkTargetInfo createInfo(NetworkTargetType targetType, UUIDTraceLogID traceLogId, NetworkLocationProvider networkLocationProvider,
            NetworkTargetDataSuppport support) {

        NetworkTargetRegistry registry = new NetworkTargetRegistry();
        if (support.isAbleToScan(targetType)) {
            LOG.debug("{} is able to scan target type {} with {}", traceLogId, targetType, getIdentifier());

            List<URI> uris = networkLocationProvider.getURIs();
            registerURIs(traceLogId, support, registry, uris);

            List<InetAddress> inetAdresses = networkLocationProvider.getInetAdresses();
            registerInetAdresses(traceLogId, support, registry, inetAdresses);

        } else {
            LOG.debug("{} is not able to scan target type {} with {}", traceLogId, targetType, getIdentifier());
        }
        return registry.createRegistryInfo(targetType);

    }

    private void registerURIs(UUIDTraceLogID traceLogId, NetworkTargetDataSuppport support, NetworkTargetRegistry registry, List<URI> uris) {
        if (uris == null || uris.isEmpty()) {
            return;
        }
        for (URI uri : uris) {
            NetworkTarget target = targetResolver.resolveTarget(uri);
            if (!support.isAbleToScan(target.getType())) {
                LOG.error("{}: setup not able to scan target {}", getIdentifier(), target);
                continue;
            }
            LOG.debug("{} register scan target:{}", traceLogId, target);
            registry.register(target);
        }
    }

    private void registerInetAdresses(UUIDTraceLogID traceLogId, NetworkTargetDataSuppport support, NetworkTargetRegistry registry,
            List<InetAddress> inetAdresses) {
        if (inetAdresses == null || inetAdresses.isEmpty()) {
            return;
        }
        for (InetAddress inetAdress : inetAdresses) {
            NetworkTarget target = targetResolver.resolveTarget(inetAdress);
            if (!support.isAbleToScan(target.getType())) {
                LOG.error("{}: setup not able to scan target {}", getIdentifier(), target);
                continue;
            }
            LOG.debug("{} register scan target:{}", traceLogId, target);
            registry.register(target);
        }
    }

    private String getIdentifier() {
        return identifier;
    }
}
