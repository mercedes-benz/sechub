// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter;

public class AbstractWebScanAdapterConfig extends AbstractAdapterConfig implements WebScanAdapterConfig {

    AbstractLoginConfig loginConfig;
    SecHubTimeUnitData maxScanDuration;

    public SecHubTimeUnitData getMaxScanDuration() {
        return maxScanDuration;
    }

    public LoginConfig getLoginConfig() {
        return loginConfig;
    }

    @Override
    public boolean hasMaxScanDuration() {
        return maxScanDuration != null;
    }

}
