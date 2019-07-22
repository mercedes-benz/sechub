// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter.netsparker;

import com.daimler.sechub.test.SechubTestComponent;
import com.daimler.sechub.test.TestFileSupport;

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
