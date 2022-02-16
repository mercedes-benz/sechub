// SPDX-License-Identifier: MIT
package com.daimler.sechub.sharedkernel;

import com.daimler.sechub.test.SechubTestComponent;
import com.daimler.sechub.test.TestFileSupport;

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
