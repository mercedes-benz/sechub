// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.developertools;

import com.mercedesbenz.sechub.test.TestFileSupport;

public class TestDeveloperToolsFileSupport extends TestFileSupport {
    private static final TestDeveloperToolsFileSupport TESTFILE_SUPPORT = new TestDeveloperToolsFileSupport();

    public static TestDeveloperToolsFileSupport getTestfileSupport() {
        return TESTFILE_SUPPORT;
    }

    protected TestDeveloperToolsFileSupport() {
        super("sechub-developertools/src/test/resources");
    }

}
