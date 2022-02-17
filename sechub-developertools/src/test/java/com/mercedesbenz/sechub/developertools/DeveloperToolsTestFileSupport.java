// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.developertools;

import com.mercedesbenz.sechub.test.TestFileSupport;

public class DeveloperToolsTestFileSupport extends TestFileSupport {
    private static final DeveloperToolsTestFileSupport TESTFILE_SUPPORT = new DeveloperToolsTestFileSupport();

    public static DeveloperToolsTestFileSupport getTestfileSupport() {
        return TESTFILE_SUPPORT;
    }

    protected DeveloperToolsTestFileSupport() {
        super("sechub-developertools/src/test/resources");
    }

}
