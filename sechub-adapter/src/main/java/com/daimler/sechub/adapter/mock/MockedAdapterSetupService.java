// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter.mock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.daimler.sechub.adapter.AdapterConfig;
import com.daimler.sechub.adapter.AdapterContext;
import com.daimler.sechub.adapter.AdapterOptionKey;
import com.daimler.sechub.adapter.support.JSONAdapterSupport;
import com.daimler.sechub.adapter.support.MockSupport;

@Service
public class MockedAdapterSetupService {

	private static final Logger LOG = LoggerFactory.getLogger(MockedAdapterSetupService.class);

	public static final String DEFAULT_FILE_PATH = "./../sechub-other/mockdata/mockdata_setup.json";

	/**
	 * Time to wait for creating workspace in milliseconds - when not defined
	 * default is used
	 */
	@Value("${sechub.adapter.mock.setup.filepath:" + DEFAULT_FILE_PATH + "}")
	String filePath = DEFAULT_FILE_PATH;// set here default too when not in spring
										// application context

	private MockSupport mockSupport = new MockSupport();

	private MockedAdapterSetup staticMockSetup;

	public <A extends AdapterContext<C>, C extends AdapterConfig> MockedAdapterSetupEntry getSetupFor(AbstractMockedAdapter<A, C> adapter, C config) {
		boolean configuredResult = config.getOptions().get(AdapterOptionKey.MOCK_CONFIGURATION_RESULT)!=null;
		
		if (configuredResult) {
			return createDynamicResultByAdapter(adapter,config);
		}
		return createStaticResultByTargets(adapter);

	}
	private <A extends AdapterContext<C>, C extends AdapterConfig> MockedAdapterSetupEntry createDynamicResultByAdapter(AbstractMockedAdapter<A, C> adapter, C config) {
		String result = config.getOptions().get(AdapterOptionKey.MOCK_CONFIGURATION_RESULT);
		String pathToResult = adapter.getPathToMockResultFile(result);
		
		MockedAdapterSetupEntry entry = new MockedAdapterSetupEntry();
		entry.setAdapterId(adapter.createAdapterId());
		
		/* we create the mock setup dynamically:*/
		MockedAdapterSetupCombination combination = new MockedAdapterSetupCombination();
		combination.setTarget(config.getTargetAsString());// we provide same target as in config, so always okay
		combination.setThrowsAdapterException(false);// maybe in future this could be configured as well ?
		combination.setTimeToElapseInMilliseconds(1000L);
		combination.setFilePath(pathToResult);
		
		entry.getCombinations().add(combination);
		
		
		return entry;
	}

	private <A extends AdapterContext<C>, C extends AdapterConfig> MockedAdapterSetupEntry createStaticResultByTargets(AbstractMockedAdapter<A, C> adapter) {
		String adapterId = adapter.createAdapterId();
		ensureSetupLoaded();
		return staticMockSetup.getEntryFor(adapterId);
	}

	private void ensureSetupLoaded() {
		if (staticMockSetup != null) {
			return;
		}
		loadConfiguredSetup();
	}

	private void loadConfiguredSetup() {

		try {
			String json = mockSupport.loadResourceString(filePath);
			staticMockSetup = JSONAdapterSupport.FOR_UNKNOWN_ADAPTER.fromJSON(MockedAdapterSetup.class, json);
		} catch (Exception e) {
			LOG.error("FATAL: cannot setup mocked adapters because not able to load json:\n{})", e);
			throw new IllegalStateException(e);
		}

	}

}
