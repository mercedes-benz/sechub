// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.schedule;

import com.mercedesbenz.sechub.test.SechubTestComponent;
import com.mercedesbenz.sechub.test.TestFileSupport;

@SechubTestComponent
public class ScheduleTestFileSupport extends TestFileSupport {
    private static final ScheduleTestFileSupport TESTFILE_SUPPORT = new ScheduleTestFileSupport();

    public static ScheduleTestFileSupport getTestfileSupport() {
        return TESTFILE_SUPPORT;
    }

    ScheduleTestFileSupport() {
        super("sechub-schedule/src/test/resources");
    }

}