// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan;

import static com.mercedesbenz.sechub.commons.core.util.SimpleStringUtils.*;

public class NetworkTargetProductServerDataSuppport {

    private NetworkTargetProductServerDataProvider networkTargetDataProvider;

    public NetworkTargetProductServerDataSuppport(NetworkTargetProductServerDataProvider networkTargetDataProvider) {
        if (networkTargetDataProvider == null) {
            throw new IllegalArgumentException("Initializer may not be null");
        }
        this.networkTargetDataProvider = networkTargetDataProvider;
    }

    public final String getIdentifier(NetworkTargetType target) {
        if (isIntranet(target)) {
            return networkTargetDataProvider.getIdentifierWhenIntranetTarget();
        }
        if (isInternet(target)) {
            return networkTargetDataProvider.getIdentifierWhenInternetTarget();
        }
        throw createUnsupportedTargetTypeException(target);
    }

    public final boolean isAbleToScan(NetworkTargetType type) {
        if (type == null) {
            return false;
        }
        if (isIntranet(type)) {
            return canScanIntranet();
        } else if (isInternet(type)) {
            return canScanInternet();
        } else {
            return false;
        }
    }

    public final String getBaseURL(NetworkTargetType type) {
        if (isIntranet(type)) {
            return networkTargetDataProvider.getBaseURLWhenIntranetTarget();
        }
        if (isInternet(type)) {
            return networkTargetDataProvider.getBaseURLWhenInternetTarget();
        }
        throw createUnsupportedTargetTypeException(type);
    }

    public final String getUserId(NetworkTargetType type) {
        if (isIntranet(type)) {
            return networkTargetDataProvider.getUsernameWhenIntranetTarget();
        }
        if (isInternet(type)) {
            return networkTargetDataProvider.getUsernameWhenInternetTarget();
        }
        throw createUnsupportedTargetTypeException(type);
    }

    public final String getPassword(NetworkTargetType target) {
        if (isIntranet(target)) {
            return networkTargetDataProvider.getPasswordWhenIntranetTarget();
        }
        if (isInternet(target)) {
            return networkTargetDataProvider.getPasswordWhenInternetTarget();
        }
        throw createUnsupportedTargetTypeException(target);
    }

    /**
     * Returns <code>true</code> when installation has not a certificate from a
     * wellknown CA
     *
     * @return <code>true</code> when having untrusted certificate
     */
    boolean hasUntrustedCertificate(NetworkTargetType target) {
        if (isIntranet(target)) {
            return networkTargetDataProvider.hasUntrustedCertificateWhenIntranetTarget();
        }
        if (isInternet(target)) {
            return networkTargetDataProvider.hasUntrustedCertificateWhenInternetTarget();
        }
        throw createUnsupportedTargetTypeException(target);
    }

    private static IllegalStateException createUnsupportedTargetTypeException(NetworkTargetType type) {
        return new IllegalStateException("target type not supported:" + type);
    }

    private static boolean isInternet(NetworkTargetType type) {
        if (type == null) {
            return false;
        }
        return type.isInternet();
    }

    private static boolean isIntranet(NetworkTargetType type) {
        if (type == null) {
            return false;
        }
        return type.isIntranet();
    }

    private boolean canScanIntranet() {
        boolean canScanIntranet = isNotEmpty(networkTargetDataProvider.getBaseURLWhenIntranetTarget());
        canScanIntranet = canScanIntranet && isNotEmpty(networkTargetDataProvider.getUsernameWhenIntranetTarget());
        canScanIntranet = canScanIntranet && isNotEmpty(networkTargetDataProvider.getPasswordWhenIntranetTarget());

        return canScanIntranet;
    }

    private boolean canScanInternet() {
        boolean canScanInternet = isNotEmpty(networkTargetDataProvider.getBaseURLWhenInternetTarget());
        canScanInternet = canScanInternet && isNotEmpty(networkTargetDataProvider.getUsernameWhenInternetTarget());
        canScanInternet = canScanInternet && isNotEmpty(networkTargetDataProvider.getPasswordWhenInternetTarget());
        return canScanInternet;
    }

}
