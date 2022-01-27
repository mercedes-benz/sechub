// SPDX-License-Identifier: MIT
package com.daimler.sechub.integrationtest.internal;

import com.daimler.sechub.adapter.AbstractAdapterConfig;

public class IntegrationTestAdapterConfig extends AbstractAdapterConfig implements IntegrationTestAdapterConfigInterface{

    @Override
    public String getTargetAsString() {
        return null;
    }
	
}