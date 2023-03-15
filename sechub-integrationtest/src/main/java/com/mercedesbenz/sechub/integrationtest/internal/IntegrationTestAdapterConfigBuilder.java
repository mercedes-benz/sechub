// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.internal;

import com.mercedesbenz.sechub.adapter.AbstractAdapterConfigBuilder;

public class IntegrationTestAdapterConfigBuilder
        extends AbstractAdapterConfigBuilder<IntegrationTestAdapterConfigBuilder, IntegrationTestAdapterConfigInterface> {

    @Override
    protected void customBuild(IntegrationTestAdapterConfigInterface config) {

    }

    @Override
    protected IntegrationTestAdapterConfigInterface buildInitialConfig() {
        return new IntegrationTestAdapterConfig();
    }

    @Override
    protected void customValidate() {

    }

}