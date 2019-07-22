// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter.nessus;

import com.daimler.sechub.test.SechubTestComponent;
import com.daimler.sechub.test.TestFileSupport;

@SechubTestComponent
public class NessusAdapterTestFileSupport extends TestFileSupport {
	private static final NessusAdapterTestFileSupport TESTFILE_SUPPORT = new NessusAdapterTestFileSupport();

	public static NessusAdapterTestFileSupport getTestfileSupport() {
		return TESTFILE_SUPPORT;
	}

	NessusAdapterTestFileSupport() {
		super("sechub-adapter-nessus/src/test/resources");
	}

}
