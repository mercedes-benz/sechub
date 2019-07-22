// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.schedule;

import com.daimler.sechub.test.SechubTestComponent;
import com.daimler.sechub.test.TestFileSupport;

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