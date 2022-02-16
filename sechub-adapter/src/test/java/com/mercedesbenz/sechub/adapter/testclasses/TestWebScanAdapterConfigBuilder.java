// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.adapter.testclasses;

import com.mercedesbenz.sechub.adapter.AbstractWebScanAdapterConfigBuilder;

public class TestWebScanAdapterConfigBuilder extends AbstractWebScanAdapterConfigBuilder<TestWebScanAdapterConfigBuilder, TestWebScanAdapterConfigInterface> {

    @Override
    protected TestWebScanAdapterConfigInterface buildInitialConfig() {
        return new TestWebScanAdapterConfig();
    }

    @Override
    protected void customValidate() {

    }

    @Override
    protected void customBuild(TestWebScanAdapterConfigInterface config) {
    }

}