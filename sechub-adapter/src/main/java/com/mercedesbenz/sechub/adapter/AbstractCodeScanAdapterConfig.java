// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.adapter;

public abstract class AbstractCodeScanAdapterConfig extends AbstractAdapterConfig implements CodeScanAdapterConfig {

    String sourceScanTargetString;

    @Override
    public String getTargetAsString() {
        return sourceScanTargetString;
    }
}
