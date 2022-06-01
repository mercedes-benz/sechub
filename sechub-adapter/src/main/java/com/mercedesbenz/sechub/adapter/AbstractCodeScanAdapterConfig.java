// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.adapter;

public abstract class AbstractCodeScanAdapterConfig extends AbstractAdapterConfig implements CodeScanAdapterConfig {

    /**
     * Only necessary for mocked adapters in integration tests. For PDS we do not
     * need this because a reald PDS instance runs always.
     */
    @Deprecated
    String sourceScanTargetString;

    @Override
    public String getTargetAsString() {
        return sourceScanTargetString;
    }
}
