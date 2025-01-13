// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan;

import com.mercedesbenz.sechub.test.SechubTestComponent;
import com.mercedesbenz.sechub.test.TestFileSupport;

@SechubTestComponent
public class TestScanDomainFileSupport extends TestFileSupport {
    private static final TestScanDomainFileSupport TESTFILE_SUPPORT = new TestScanDomainFileSupport();

    public static TestFileSupport getTestfileSupport() {
        return TESTFILE_SUPPORT;
    }

    TestScanDomainFileSupport() {
        super("sechub-scan/src/test/resources");
    }

}
