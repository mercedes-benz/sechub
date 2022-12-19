// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.internal;

import com.mercedesbenz.sechub.adapter.AbstractAdapterConfig;

public class IntegrationTestAdapterConfig extends AbstractAdapterConfig implements IntegrationTestAdapterConfigInterface {
    @Override
    public String getTargetAsString() {
        return null;
    }

}