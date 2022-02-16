// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.resolve;

import com.daimler.sechub.domain.scan.Target;

public interface TargetResolveStrategy<T> {

    /**
     * Initialize strategy (if possible) and return initialization result
     *
     * @param definedUriStrategy
     * @return @return <code>true</code> when this strategy can be initialized by
     *         given string
     */
    boolean initialize(String uriPattern);

    /**
     * Resolves target for given type
     *
     * @param type
     * @return target, or <code>null</code>
     */
    Target resolveTargetFor(T type);

}