// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.schedule;

import com.mercedesbenz.sechub.test.SechubTestComponent;
import com.mercedesbenz.sechub.test.TestFileSupport;

@SechubTestComponent
public class TestScheduleFileSupport extends TestFileSupport {
    private static final TestScheduleFileSupport TESTFILE_SUPPORT = new TestScheduleFileSupport();

    public static TestScheduleFileSupport getTestfileSupport() {
        return TESTFILE_SUPPORT;
    }

    TestScheduleFileSupport() {
        super("sechub-schedule/src/test/resources");
    }

}