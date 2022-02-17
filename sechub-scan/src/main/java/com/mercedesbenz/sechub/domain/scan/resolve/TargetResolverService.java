// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.resolve;

import java.net.InetAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.domain.scan.Target;
import com.mercedesbenz.sechub.domain.scan.TargetType;
import com.mercedesbenz.sechub.sharedkernel.MustBeDocumented;

/**
 * Service for target resolving. Fallback target types for IP and URIs will be
 * always INTERNET.
 *
 * @author Albert Tregnaghi
 *
 */
@Service
public class TargetResolverService implements TargetResolver {

    @Autowired
    IllegalURItargetDetector illegalURItargetDetector;

    @Autowired
    IllegalInetAddressTargetDetector illegalInetAddressTargetDetector;

    @Value("${sechub.target.resolve.strategy.uri:}")
    @MustBeDocumented(value = "*Strategy to decide target types by given URI.* +\n"
            + "Starts always with strategy-identifer, colon and value(s). Currently only 'intranet-hostname-ends-with' is supported as strategy. For example: "
            + "`intranet-hostname-ends-with::intranet.example.org,intx.example.com`. Other hostnames are interpreted as being inside INTERNET. "
            + "But no matter if strategy is defined or not: loopback addresses are always illegal and so ignored.")
    String definedUriStrategy;

    @Value("${sechub.target.resolve.strategy.ip:}")
    @MustBeDocumented(value = "*Strategy to decide target types by given IP.* +\n"
            + "Starts always with strategy-identifer, colon and value(s). Currently only 'intranet-ip-pattern' is supported as strategy. For example: "
            + "`intranet-ip-pattern:192.168.178.*,[2001:db8:85a3:0:0:8a2e:370:*]`. Other IPs are interpreted as being inside INTERNET. "
            + "But no matter if strategy is defined or not: loopback addresses are always illegal and so ignored")
    String definedInetAddressStrategy;

    @Autowired
    List<InetAdressTargetResolveStrategy> inetAddressTargetResolveStrategies = new ArrayList<>();

    @Autowired
    List<URITargetResolveStrategy> uriTargetResolveStrategies = new ArrayList<>();

    private URITargetResolveStrategy usedUriTargetResolveStrategy;
    private InetAdressTargetResolveStrategy usedInetAddressTargetResolveStrategy;
    private boolean initialized;

    public TargetResolverService() {
    }

    @Override
    public Target resolveTargetForPath(String path) {
        /*
         * currently there is no strategy - we simple accept this always as a code
         * upload
         */
        TargetType codeUploadType = TargetType.CODE_UPLOAD;
        return new Target(path, codeUploadType);
    }

    @Override
    public Target resolveTarget(URI uri) {
        ensureInitialized();

        if (uri == null) {
            return new Target(uri, TargetType.UNKNOWN);
        }
        if (illegalURItargetDetector != null) {
            if (illegalURItargetDetector.isIllegal(uri)) {
                return new Target(uri, TargetType.ILLEGAL);
            }
        }
        Target resolved = null;
        if (usedUriTargetResolveStrategy != null) {
            resolved = usedUriTargetResolveStrategy.resolveTargetFor(uri);
        }
        if (resolved == null) {
            resolved = new Target(uri, TargetType.INTERNET);
        }
        return resolved;
    }

    @Override
    public Target resolveTarget(InetAddress inetAdress) {
        ensureInitialized();

        if (inetAdress == null) {
            return new Target(inetAdress, TargetType.UNKNOWN);
        }
        if (illegalInetAddressTargetDetector != null) {
            if (illegalInetAddressTargetDetector.isIllegal(inetAdress)) {
                return new Target(inetAdress, TargetType.ILLEGAL);
            }
        }
        Target resolved = null;
        if (usedInetAddressTargetResolveStrategy != null) {
            resolved = usedInetAddressTargetResolveStrategy.resolveTargetFor(inetAdress);
        }
        if (resolved == null) {
            resolved = new Target(inetAdress, TargetType.INTERNET);
        }
        return resolved;
    }

    private void ensureInitialized() {
        if (initialized) {
            return;
        }
        initInetAddressStrategy();
        initURIStrategy();
    }

    private void initURIStrategy() {
        for (URITargetResolveStrategy strategy : uriTargetResolveStrategies) {
            if (strategy.initialize(definedUriStrategy)) {
                usedUriTargetResolveStrategy = strategy;
                break;
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
