// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel;

import com.mercedesbenz.sechub.test.SechubTestComponent;
import com.mercedesbenz.sechub.test.TestFileSupport;

@SechubTestComponent
public class TestSharedKernelFileSupport extends TestFileSupport {
    private static final TestSharedKernelFileSupport TESTFILE_SUPPORT = new TestSharedKernelFileSupport();

    public static TestSharedKernelFileSupport getTestfileSupport() {
        return TESTFILE_SUPPORT;
    }

    TestSharedKernelFileSupport() {
        super("sechub-shared-kernel/src/test/resources");
    }

}
