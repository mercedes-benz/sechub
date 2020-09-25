// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter.mock;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.daimler.sechub.adapter.AbstractAdapterConfig;
import com.daimler.sechub.adapter.AdapterOptionKey;
import com.daimler.sechub.adapter.SecHubAdapterTestFileSupport;

public class MockedAdapterSetupServiceTest {

	
	private MockedAdapterSetupService serviceToTest;

	@Before
	public void before() {
		serviceToTest = new MockedAdapterSetupService();
		
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void static_read_default_setup_possible_and_has_atleast_checkmarx_and_netsparker_data_for_ANY_OTHER_TARGET_inside() {
		/* prepare */
		File gradleSafeTestFile = SecHubAdapterTestFileSupport.INSTANCE
				.createFileFromResourcePath(MockedAdapterSetupService.DEFAULT_FILE_PATH);

		serviceToTest.filePath = gradleSafeTestFile.getAbsolutePath();

		AbstractMockedAdapter mockedCheckmarxAdapter = mock(AbstractMockedAdapter.class);
		AbstractMockedAdapter mockedNetsparkerAdapter = mock(AbstractMockedAdapter.class);
		AbstractAdapterConfig config = mock(AbstractAdapterConfig.class);
		
		when(mockedCheckmarxAdapter.createAdapterId()).thenReturn("MockedNessusAdapter");
		when(mockedNetsparkerAdapter.createAdapterId()).thenReturn("MockedNetsparkerAdapter");
		
		/* execute */
		MockedAdapterSetupEntry checkmarxSetup = serviceToTest.getSetupFor(mockedCheckmarxAdapter,config);
		MockedAdapterSetupEntry netsparkerSetup = serviceToTest.getSetupFor(mockedNetsparkerAdapter,config);
		
		/* test */
		assertNotNull(checkmarxSetup);
		assertNotNull(checkmarxSetup.getCombination(MockedAdapterSetupCombination.ANY_OTHER_TARGET));

		assertNotNull(netsparkerSetup);
		assertNotNull(netsparkerSetup.getCombination(MockedAdapterSetupCombination.ANY_OTHER_TARGET));
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void dynamic_when_config_has_option_set_expected_dynamic_setup_combination_is_returned() {
		/* prepare */
		AbstractMockedAdapter mockedCheckmarxAdapter = mock(AbstractMockedAdapter.class);
		AbstractAdapterConfig config = mock(AbstractAdapterConfig.class);
		
		when(mockedCheckmarxAdapter.createAdapterId()).thenReturn("MockedNessusAdapter");
		
		Map<AdapterOptionKey, String> options = new HashMap<>();
		options.put(AdapterOptionKey.MOCK_CONFIGURATION_RESULT, "yellow");
		when(config.getOptions()).thenReturn(options);
		when(config.getTargetAsString()).thenReturn("target1");
		when(mockedCheckmarxAdapter.getPathToMockResultFile("yellow")).thenReturn("pathFromAdapter");
		
		/* execute */
		MockedAdapterSetupEntry checkmarxSetup = serviceToTest.getSetupFor(mockedCheckmarxAdapter,config);
		
		/* test */
		assertNotNull(checkmarxSetup);
		MockedAdapterSetupCombination combination = checkmarxSetup.getCombination("target1");
		assertNotNull(combination);
		assertEquals("target1",combination.getTarget());
		assertEquals("Filepath differs","pathFromAdapter",combination.getFilePath());
		assertFalse(combination.isThrowsAdapterException());
		assertEquals(1000L, combination.getTimeToElapseInMilliseconds());

	}

}
