// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.docgen.util;

import com.mercedesbenz.sechub.test.SechubTestComponent;
import com.mercedesbenz.sechub.test.TestFileSupport;

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
