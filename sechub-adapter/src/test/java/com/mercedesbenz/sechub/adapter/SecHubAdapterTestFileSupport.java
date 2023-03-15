// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.adapter;

import com.mercedesbenz.sechub.test.TestFileSupport;

public class SecHubAdapterTestFileSupport extends TestFileSupport {

    public static final SecHubAdapterTestFileSupport INSTANCE = new SecHubAdapterTestFileSupport();

    protected SecHubAdapterTestFileSupport() {
        super("sechub-adapter");
    }
}
