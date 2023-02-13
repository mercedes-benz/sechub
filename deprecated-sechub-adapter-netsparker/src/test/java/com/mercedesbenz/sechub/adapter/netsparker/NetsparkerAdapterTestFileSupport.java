// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.adapter.netsparker;

import com.mercedesbenz.sechub.test.SechubTestComponent;
import com.mercedesbenz.sechub.test.TestFileSupport;

@SechubTestComponent
public class NetsparkerAdapterTestFileSupport extends TestFileSupport {
    private static final NetsparkerAdapterTestFileSupport TESTFILE_SUPPORT = new NetsparkerAdapterTestFileSupport();

    public static NetsparkerAdapterTestFileSupport getTestfileSupport() {
        return TESTFILE_SUPPORT;
    }

    NetsparkerAdapterTestFileSupport() {
        super("sechub-adapter-netsparker/src/test/resources");
    }

}
