// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan;

import java.net.InetAddress;
import java.net.URI;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mercedesbenz.sechub.domain.scan.NetworkTargetRegistry.NetworkTargetInfo;
import com.mercedesbenz.sechub.domain.scan.resolve.NetworkTargetResolver;
import com.mercedesbenz.sechub.sharedkernel.UUIDTraceLogID;

public class NetworkTargetInfoFactory {

    private static final Logger LOG = LoggerFactory.getLogger(NetworkTargetInfoFactory.class);

    private NetworkTargetResolver targetResolver;
    private String identifier;

    public NetworkTargetInfoFactory(NetworkTargetResolver targetResolver, String identifier) {
        this.targetResolver = targetResolver;
        this.identifier = identifier;
    }

    public NetworkTargetInfo createInfo(NetworkTargetType targetType, UUIDTraceLogID traceLogId, NetworkLocationProvider networkLocationProvider,
            NetworkTargetProductServerDataSuppport support) {

        NetworkTargetRegistry registry = new NetworkTargetRegistry();
        if (support.isAbleToScan(targetType)) {
            LOG.debug("{} is able to scan target type {} with {}", traceLogId, targetType, getIdentifier());

            List<URI> uris = networkLocationProvider.getURIs();
            registerURIs(traceLogId, support, registry, uris);

            List<InetAddress> inetAdresses = networkLocationProvider.getInetAddresses();
            registerInetAdresses(traceLogId, support, registry, inetAdresses);

        } else {
            LOG.debug("{} is not able to scan target type {} with {}", traceLogId, targetType, getIdentifier());
        }
        return registry.createRegistryInfo(targetType);

    }

    private void registerURIs(UUIDTraceLogID traceLogId, NetworkTargetProductServerDataSuppport support, NetworkTargetRegistry registry, List<URI> uris) {
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

    private void registerInetAdresses(UUIDTraceLogID traceLogId, NetworkTargetProductServerDataSuppport support, NetworkTargetRegistry registry,
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
