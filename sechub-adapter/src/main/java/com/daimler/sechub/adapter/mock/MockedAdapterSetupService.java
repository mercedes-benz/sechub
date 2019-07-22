// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter.mock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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

	private MockedAdapterSetup setup;

	public MockedAdapterSetupEntry getSetupFor(String adapterId) {
		ensureSetupLoaded();
		return setup.getEntryFor(adapterId);
	}

	private void ensureSetupLoaded() {
		if (setup != null) {
			return;
		}
		loadConfiguredSetup();
	}

	private void loadConfiguredSetup() {

		try {
			String json = mockSupport.loadResourceString(filePath);
			setup = JSONAdapterSupport.FOR_UNKNOWN_ADAPTER.fromJSON(MockedAdapterSetup.class, json);
		} catch (Exception e) {
			LOG.error("FATAL: cannot setup mocked adapters because not able to load json:\n{})", e);
			throw new IllegalStateException(e);
		}

	}
}
