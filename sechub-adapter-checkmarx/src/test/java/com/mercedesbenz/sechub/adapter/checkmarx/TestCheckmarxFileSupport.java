// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.adapter.checkmarx;

import com.mercedesbenz.sechub.test.TestFileSupport;

public class TestCheckmarxFileSupport extends TestFileSupport {

    private static final TestCheckmarxFileSupport TESTFILE_SUPPORT = new TestCheckmarxFileSupport();

    public static TestCheckmarxFileSupport getTestfileSupport() {
        return TESTFILE_SUPPORT;
    }

    TestCheckmarxFileSupport() {
        super("sechub-adapter-checkmarx/src/test/resources");
    }
}
