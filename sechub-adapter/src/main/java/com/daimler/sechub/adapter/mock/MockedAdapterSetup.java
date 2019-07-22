// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter.mock;

import java.util.ArrayList;
import java.util.List;

/**
 * Just a list - to have an object for json marshalling...
 * @author Albert Tregnaghi
 *
 */
public class MockedAdapterSetup{

	private List<MockedAdapterSetupEntry> entries= new ArrayList<>();
	
	public List<MockedAdapterSetupEntry> getEntries() {
		return entries;
	}

	/**
	 * Search method
	 * @param adapterId
	 * @return configured setup entry for adapter id or <code>null</code> if none found
	 */
	public MockedAdapterSetupEntry getEntryFor(String adapterId) {
		if (adapterId==null) {
			return null;
		}
		for (MockedAdapterSetupEntry entry: entries) {
			if (entry==null) {
				continue;
			}
			if (adapterId.equals(entry.getAdapterId())){
				return entry;
			}
		}
		return null;
	}
	
}
