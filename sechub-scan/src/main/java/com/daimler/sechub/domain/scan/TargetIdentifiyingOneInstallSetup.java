// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan;

/**
 * Setup where a product is installed only ONE time, but needs to differ between
 * different targets at runtime
 *
 * @author Albert Tregnaghi
 *
 */
public interface TargetIdentifiyingOneInstallSetup extends OneInstallSetup {

    /**
     * Support the identifier which is used by the one and only installation to
     * provide access to different targets
     *
     * @param target
     * @return identifier
     */
    String getIdentifier(TargetType target);

}