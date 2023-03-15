// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel;

import com.mercedesbenz.sechub.test.SechubTestComponent;
import com.mercedesbenz.sechub.test.TestFileSupport;

@SechubTestComponent
public class SharedKernelTestFileSupport extends TestFileSupport {
    private static final SharedKernelTestFileSupport TESTFILE_SUPPORT = new SharedKernelTestFileSupport();

    public static SharedKernelTestFileSupport getTestfileSupport() {
        return TESTFILE_SUPPORT;
    }

    SharedKernelTestFileSupport() {
        super("sechub-shared-kernel/src/test/resources");
    }

}
