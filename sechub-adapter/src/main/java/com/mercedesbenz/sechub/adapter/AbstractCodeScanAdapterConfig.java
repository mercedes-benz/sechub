// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.adapter;

public abstract class AbstractCodeScanAdapterConfig extends AbstractAdapterConfig implements CodeScanAdapterConfig {

    String mockDataIdentifier;

    @Override
    public String getTargetAsString() {
        return null; // for code we cannot define a target as a string
    }
}
