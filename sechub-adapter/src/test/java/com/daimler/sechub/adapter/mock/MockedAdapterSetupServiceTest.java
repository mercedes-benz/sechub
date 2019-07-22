// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter.mock;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Test;

import com.daimler.sechub.adapter.SecHubAdapterTestFileSupport;

public class MockedAdapterSetupServiceTest {

	@Test
	public void read_default_setup_possible_and_has_atleast_checkmarx_and_netsparker_data_for_ANY_OTHER_TARGET_inside() {
		/* prepare */
		File gradleSafeTestFile = SecHubAdapterTestFileSupport.INSTANCE
				.createFileFromResourcePath(MockedAdapterSetupService.DEFAULT_FILE_PATH);
	

		MockedAdapterSetupService service = new MockedAdapterSetupService();
		service.filePath = gradleSafeTestFile.getAbsolutePath();

		/* execute */
		MockedAdapterSetupEntry checkmarxSetup = service.getSetupFor("MockedCheckmarxAdapter");
		MockedAdapterSetupEntry netsparkerSetup = service.getSetupFor("MockedNetsparkerAdapter");
		
		/* test */
		assertNotNull(checkmarxSetup);
		assertNotNull(checkmarxSetup.getCombination(MockedAdapterSetupCombination.ANY_OTHER_TARGET));

		assertNotNull(netsparkerSetup);
		assertNotNull(netsparkerSetup.getCombination(MockedAdapterSetupCombination.ANY_OTHER_TARGET));
	}

}
