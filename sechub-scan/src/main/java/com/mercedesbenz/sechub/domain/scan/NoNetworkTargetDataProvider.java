// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan;

public class NoNetworkTargetDataProvider implements NetworkTargetDataProvider{

    @Override
    public String getIdentifierWhenInternetTarget() {
        return null;
    }

    @Override
    public String getIdentifierWhenIntranetTarget() {
        return null;
    }

    @Override
    public String getBaseURLWhenInternetTarget() {
        return null;
    }

    @Override
    public String getBaseURLWhenIntranetTarget() {
        return null;
    }

    @Override
    public String getUsernameWhenInternetTarget() {
        return null;
    }

    @Override
    public String getUsernameWhenIntranetTarget() {
        return null;
    }

    @Override
    public String getPasswordWhenInternetTarget() {
        return null;
    }

    @Override
    public String getPasswordWhenIntranetTarget() {
        return null;
    }

    @Override
    public boolean isHavingUntrustedCertificateForIntranet() {
        return false;
    }

    @Override
    public boolean isHavingUntrustedCertificateForInternet() {
        return false;
    }

}
