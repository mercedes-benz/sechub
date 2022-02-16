// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter.testclasses;

import com.daimler.sechub.adapter.AbstractInfraScanAdapterConfigBuilder;

public class TestInfraScanAdapterConfigBuilder extends AbstractInfraScanAdapterConfigBuilder<TestInfraScanAdapterConfigBuilder, TestInfraScanAdapterConfigInterface> {

    @Override
    protected TestInfraScanAdapterConfigInterface buildInitialConfig() {
        return new TestInfraScanAdapterConfig();
    }

    @Override
    protected void customValidate() {

    }

    @Override
    protected void customBuild(TestInfraScanAdapterConfigInterface config) {
    }
}