// SPDX-License-Identifier: MIT
package com.daimler.sechub.integrationtest.internal;

import java.io.File;

import com.daimler.sechub.test.SechubTestComponent;
import com.daimler.sechub.test.TestFileSupport;

@SechubTestComponent
public class IntegrationTestFileSupport extends TestFileSupport {
	private static final IntegrationTestFileSupport TESTFILE_SUPPORT = new IntegrationTestFileSupport();

	public static IntegrationTestFileSupport getTestfileSupport() {
		return TESTFILE_SUPPORT;
	}

    private File integrationTestDataFolder;

	IntegrationTestFileSupport() {
		super("sechub-integrationtest/src/test/resources");
		integrationTestDataFolder = new File(getRootFolder(), "sechub-integrationtest/build/sechub/integrationtest/");
	}
	
	public File getIntegrationTestDataFolder() {
        return integrationTestDataFolder;
    }

	
}
