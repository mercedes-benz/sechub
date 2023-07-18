// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.adapter;

public abstract class AbstractAnalyticsAdapterConfig extends AbstractAdapterConfig implements AnalyticsAdapterConfig {

    @Override
    public String getTargetAsString() {
        return null; // for analyze we cannot define a target as a string
    }
}
