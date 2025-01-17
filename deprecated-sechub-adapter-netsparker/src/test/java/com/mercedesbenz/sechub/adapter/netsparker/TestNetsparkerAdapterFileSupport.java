// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.adapter.netsparker;

import com.mercedesbenz.sechub.test.SechubTestComponent;
import com.mercedesbenz.sechub.test.TestFileSupport;

@SechubTestComponent
public class TestNetsparkerAdapterFileSupport extends TestFileSupport {
    private static final TestNetsparkerAdapterFileSupport TESTFILE_SUPPORT = new TestNetsparkerAdapterFileSupport();

    public static TestNetsparkerAdapterFileSupport getTestfileSupport() {
        return TESTFILE_SUPPORT;
    }

    TestNetsparkerAdapterFileSupport() {
        super("deprecated-sechub-adapter-netsparker/src/test/resources");
    }

}
