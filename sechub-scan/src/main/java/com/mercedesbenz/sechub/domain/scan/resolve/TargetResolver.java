// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.resolve;

import java.net.InetAddress;
import java.net.URI;

import com.mercedesbenz.sechub.domain.scan.Target;

public interface TargetResolver {

    /**
     * Resolves target for given URI
     *
     * @param uri
     * @return target, never <code>null</code>
     */
    public Target resolveTarget(URI uri);

    /**
     * Resolves target for given IP adress
     *
     * @param inetAdress
     * @return target, never <code>null</code>
     */
    public Target resolveTarget(InetAddress inetAdress);

    /**
     * Resolves target for given path
     *
     * @param path
     * @return target, never <code>null</code>
     * @throws IllegalArgumentException if identifier cannot be handled
     */
    public Target resolveTargetForPath(String path);
}
