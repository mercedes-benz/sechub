// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.resolve;

import static com.mercedesbenz.sechub.sharedkernel.DocumentationScopeConstants.*;

import java.net.InetAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.commons.core.doc.MustBeDocumented;
import com.mercedesbenz.sechub.domain.scan.NetworkTarget;
import com.mercedesbenz.sechub.domain.scan.NetworkTargetType;

/**
 * Service for target resolving. Fallback target types for IP and URIs will be
 * always INTERNET.
 *
 * @author Albert Tregnaghi
 *
 */
@Service
public class TargetResolverService implements NetworkTargetResolver {

    @Autowired
    IllegalURItargetDetector illegalURItargetDetector;

    @Autowired
    IllegalInetAddressTargetDetector illegalInetAddressTargetDetector;

    @Value("${sechub.target.resolve.strategy.uri:}")
    @MustBeDocumented(value = "*One ore more strategies to decide target types by given URI.* +\n"
            + "Starts always with strategy-identifer, colon and value(s). Values are comma separated. Currently only 'intranet-hostname-ends-with' and 'intranet-hostname-starts-with' are supported as strategies. For example: "
            + "`intranet-hostname-ends-with:intranet.example.org,intx.example.com`. Other hostnames are interpreted as being inside INTERNET. "
            + "But no matter if strategy is defined or not: loopback addresses are always illegal and so ignored. You can define multiple strategies at same time by using a pipe symbol to separate them. As an example: "
            + "`intranet-hostname-ends-with:intranet.example.org,intx.example.com|intranet-hostname-starts-with:10.5`", scope = SCOPE_SCAN)
    String definedUriStrategy;

    @Value("${sechub.target.resolve.strategy.ip:}")
    @MustBeDocumented(value = "*Strategy to decide target types by given IP.* +\n"
            + "Starts always with strategy-identifer, colon and value(s). Values are comma separated. Currently only 'intranet-ip-pattern' is supported as strategy. Inside this strategy,"
            + "it is possible to define IPv4 or IPv6 adresses (wildcards are also possible). For example: "
            + "`intranet-ip-pattern:192.168.178.\\*,2001:db8:85a3:0:0:8a2e:370:*`. Other IPs are interpreted as being inside INTERNET. "
            + "But no matter if strategy is defined or not: loopback addresses are always illegal and so ignored", scope = SCOPE_SCAN)
    String definedInetAddressStrategy;

    @Autowired
    List<InetAdressTargetResolveStrategy> inetAddressTargetResolveStrategies = new ArrayList<>();

    @Autowired
    List<URITargetResolveStrategy> uriTargetResolveStrategies = new ArrayList<>();

    private List<URITargetResolveStrategy> usedUriTargetResolveStrategies = new ArrayList<>();
    private InetAdressTargetResolveStrategy usedInetAddressTargetResolveStrategy;
    private boolean initialized;

    public TargetResolverService() {
    }

    @Override
    public NetworkTarget resolveTarget(URI uri) {
        ensureInitialized();

        if (uri == null) {
            return new NetworkTarget(uri, NetworkTargetType.UNKNOWN);
        }
        if (illegalURItargetDetector != null) {
            if (illegalURItargetDetector.isIllegal(uri)) {
                return new NetworkTarget(uri, NetworkTargetType.ILLEGAL);
            }
        }
        NetworkTarget resolved = null;
        for (URITargetResolveStrategy usedUriTargetResolveStrategy : usedUriTargetResolveStrategies) {
            if (usedUriTargetResolveStrategy != null) {
                resolved = usedUriTargetResolveStrategy.resolveTargetFor(uri);
            }
            if (resolved != null) {
                break;
            }
        }
        if (resolved == null) {
            resolved = new NetworkTarget(uri, NetworkTargetType.INTERNET);
        }
        return resolved;
    }

    @Override
    public NetworkTarget resolveTarget(InetAddress inetAdress) {
        ensureInitialized();

        if (inetAdress == null) {
            return new NetworkTarget(inetAdress, NetworkTargetType.UNKNOWN);
        }
        if (illegalInetAddressTargetDetector != null) {
            if (illegalInetAddressTargetDetector.isIllegal(inetAdress)) {
                return new NetworkTarget(inetAdress, NetworkTargetType.ILLEGAL);
            }
        }
        NetworkTarget resolved = null;
        if (usedInetAddressTargetResolveStrategy != null) {
            resolved = usedInetAddressTargetResolveStrategy.resolveTargetFor(inetAdress);
        }
        if (resolved == null) {
            resolved = new NetworkTarget(inetAdress, NetworkTargetType.INTERNET);
        }
        return resolved;
    }

    private void ensureInitialized() {
        if (initialized) {
            return;
        }
        initInetAddressStrategy();
        initURIStrategy();

        initialized = true;
    }

    private void initURIStrategy() {
        if (definedUriStrategy == null) {
            return;
        }
        String[] splittedUriTargetResolveStrategies = definedUriStrategy.split("\\|");

        for (String splittedPart : splittedUriTargetResolveStrategies) {

            for (URITargetResolveStrategy strategy : uriTargetResolveStrategies) {
                if (strategy.initialize(splittedPart)) {
                    usedUriTargetResolveStrategies.add(strategy);
                    break;
                }
            }
        }
    }

    private void initInetAddressStrategy() {
        for (InetAdressTargetResolveStrategy strategy : inetAddressTargetResolveStrategies) {
            if (strategy.initialize(definedInetAddressStrategy)) {
                usedInetAddressTargetResolveStrategy = strategy;
                break;
            }
        }
    }

}
