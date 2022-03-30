// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.resolve;

import java.net.InetAddress;
import java.net.URI;

import com.mercedesbenz.sechub.domain.scan.NetworkTarget;

public interface TargetResolver {

    /**
     * Resolves target for given URI
     *
     * @param uri
     * @return target, never <code>null</code>
     */
    public NetworkTarget resolveTarget(URI uri);

    /**
     * Resolves target for given IP adress
     *
     * @param inetAdress
     * @return target, never <code>null</code>
     */
    public NetworkTarget resolveTarget(InetAddress inetAdress);

}
