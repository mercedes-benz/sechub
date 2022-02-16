// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan;

/**
 * Setup where a product is installed multiple times - target dependent. (E.g.
 * for <b>INTRANET</b> there is another installation than for <b>INTERNET</b>)
 *
 * @author Albert Tregnaghi
 *
 */
public interface TargetIdentifyingMultiInstallSetup extends InstallSetup {

    /**
     * Get base url for given target type
     *
     * @param type
     * @return base url
     */
    String getBaseURL(TargetType type);

    /**
     * Get user id for login at product suitable for target type
     *
     * @param type
     * @return
     */
    String getUserId(TargetType type);

    /**
     * Get password for login at product suitable for target type
     *
     * @param type
     * @return
     */
    String getPassword(TargetType target);

    /**
     * Returns <code>true</code> when installation has not a certificate from a
     * wellknown CA
     *
     * @return <code>true</code> when having untrusted certificate
     */
    boolean isHavingUntrustedCertificate(TargetType target);

}