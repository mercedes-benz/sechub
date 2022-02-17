// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan;

import com.mercedesbenz.sechub.test.SechubTestComponent;
import com.mercedesbenz.sechub.test.TestFileSupport;

@SechubTestComponent
public class ScanDomainTestFileSupport extends TestFileSupport {
    private static final ScanDomainTestFileSupport TESTFILE_SUPPORT = new ScanDomainTestFileSupport();

    public static TestFileSupport getTestfileSupport() {
        return TESTFILE_SUPPORT;
    }

    ScanDomainTestFileSupport() {
        super("sechub-scan/src/test/resources");
    }

}
