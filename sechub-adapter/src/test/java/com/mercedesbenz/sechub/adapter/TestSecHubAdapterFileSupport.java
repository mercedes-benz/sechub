// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.adapter;

import com.mercedesbenz.sechub.test.TestFileSupport;

public class TestSecHubAdapterFileSupport extends TestFileSupport {

    public static final TestSecHubAdapterFileSupport INSTANCE = new TestSecHubAdapterFileSupport();

    protected TestSecHubAdapterFileSupport() {
        super("sechub-adapter");
    }
}
