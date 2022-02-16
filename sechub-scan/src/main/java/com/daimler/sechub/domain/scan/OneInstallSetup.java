// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan;

public interface OneInstallSetup extends InstallSetup {

    /**
     * User name for product to login
     *
     * @return user id
     */
    String getUserId();

    /**
     * Password for user to login
     *
     * @return password
     */
    String getPassword();

    /**
     * Base url for the product
     *
     * @return base url
     */
    String getBaseURL();

    /**
     * Returns <code>true</code> when installation has not a certificate from a
     * wellknown CA
     *
     * @return <code>true</code> when having untrusted certificate
     */
    boolean isHavingUntrustedCertificate();

}