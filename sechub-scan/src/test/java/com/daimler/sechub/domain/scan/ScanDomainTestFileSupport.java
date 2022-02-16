// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan;

import com.daimler.sechub.test.SechubTestComponent;
import com.daimler.sechub.test.TestFileSupport;

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
