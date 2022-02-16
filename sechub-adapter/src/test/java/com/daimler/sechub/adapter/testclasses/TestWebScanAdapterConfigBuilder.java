// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter.testclasses;

import com.daimler.sechub.adapter.AbstractWebScanAdapterConfigBuilder;

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