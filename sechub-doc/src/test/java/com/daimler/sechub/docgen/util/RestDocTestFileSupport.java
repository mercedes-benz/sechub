// SPDX-License-Identifier: MIT
package com.daimler.sechub.docgen.util;

import com.daimler.sechub.test.SechubTestComponent;
import com.daimler.sechub.test.TestFileSupport;

@SechubTestComponent
public class RestDocTestFileSupport extends TestFileSupport {

    private static final RestDocTestFileSupport TESTFILE_SUPPORT = new RestDocTestFileSupport();

    public static RestDocTestFileSupport getTestfileSupport() {
        return TESTFILE_SUPPORT;
    }

    RestDocTestFileSupport() {
        super("sechub-doc/src/test/resources");
    }

}
