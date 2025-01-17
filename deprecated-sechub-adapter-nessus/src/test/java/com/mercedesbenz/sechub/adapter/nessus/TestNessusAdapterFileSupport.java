// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.adapter.nessus;

import com.mercedesbenz.sechub.test.SechubTestComponent;
import com.mercedesbenz.sechub.test.TestFileSupport;

@SechubTestComponent
public class TestNessusAdapterFileSupport extends TestFileSupport {
    private static final TestNessusAdapterFileSupport TESTFILE_SUPPORT = new TestNessusAdapterFileSupport();

    public static TestNessusAdapterFileSupport getTestfileSupport() {
        return TESTFILE_SUPPORT;
    }

    TestNessusAdapterFileSupport() {
        super("deprecated-sechub-adapter-nessus/src/test/resources");
    }

}
