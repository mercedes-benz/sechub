// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter.testclasses;

import com.daimler.sechub.adapter.AbstractAdapter;
import com.daimler.sechub.adapter.AdapterException;

public class TestAdapter extends AbstractAdapter<TestAdapterContextInterface, TestAdapterConfigInterface> implements TestAdapterInterface{

	String apiPrefix;
	
	public void setApiPrefix(String apiPrefix) {
		this.apiPrefix = apiPrefix;
	}
	
	@Override
	protected String getAPIPrefix() {
		return apiPrefix;
	}

	@Override
	public String start(TestAdapterConfigInterface config) throws AdapterException {
		return null;
	}

}
