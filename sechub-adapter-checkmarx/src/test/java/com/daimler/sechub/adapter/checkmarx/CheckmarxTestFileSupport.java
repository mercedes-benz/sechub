// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter.checkmarx;

import com.daimler.sechub.test.TestFileSupport;

public class CheckmarxTestFileSupport extends TestFileSupport {

	private static final CheckmarxTestFileSupport TESTFILE_SUPPORT = new CheckmarxTestFileSupport();

	public static CheckmarxTestFileSupport getTestfileSupport() {
		return TESTFILE_SUPPORT;
	}

	CheckmarxTestFileSupport() {
		super("sechub-adapter-checkmarx/src/test/resources");
	}
}
