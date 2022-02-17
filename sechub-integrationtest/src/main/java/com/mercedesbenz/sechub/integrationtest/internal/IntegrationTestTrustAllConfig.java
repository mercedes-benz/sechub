// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.internal;

import com.mercedesbenz.sechub.adapter.TrustAllConfig;

public class IntegrationTestTrustAllConfig implements TrustAllConfig {

    @Override
    public String getProxyHostname() {
        return null;
    }

    @Override
    public int getProxyPort() {
        return 0;
    }

    @Override
    public boolean isProxyDefined() {
        return false;
    }

    @Override
    public String getTraceID() {
        return "INTEGRATIONTEST." + hashCode();
    }

    @Override
    public boolean isTrustAllCertificatesEnabled() {
        return true;
    }

}
