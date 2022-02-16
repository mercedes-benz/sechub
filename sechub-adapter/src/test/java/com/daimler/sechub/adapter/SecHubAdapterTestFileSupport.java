// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter;

import com.daimler.sechub.test.TestFileSupport;

public class SecHubAdapterTestFileSupport extends TestFileSupport {

    public static final SecHubAdapterTestFileSupport INSTANCE = new SecHubAdapterTestFileSupport();

    protected SecHubAdapterTestFileSupport() {
        super("sechub-adapter");
    }
}
