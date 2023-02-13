// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.adapter.nessus;

import com.mercedesbenz.sechub.test.SechubTestComponent;
import com.mercedesbenz.sechub.test.TestFileSupport;

@SechubTestComponent
public class NessusAdapterTestFileSupport extends TestFileSupport {
    private static final NessusAdapterTestFileSupport TESTFILE_SUPPORT = new NessusAdapterTestFileSupport();

    public static NessusAdapterTestFileSupport getTestfileSupport() {
        return TESTFILE_SUPPORT;
    }

    NessusAdapterTestFileSupport() {
        super("deprecated-sechub-adapter-nessus/src/test/resources");
    }

}
