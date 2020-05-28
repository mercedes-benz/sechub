// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter.testclasses;

import com.daimler.sechub.adapter.AbstractAdapter;
import com.daimler.sechub.adapter.AdapterException;
import com.daimler.sechub.adapter.AdapterRuntimeContext;

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
	public String execute(TestAdapterConfigInterface config, AdapterRuntimeContext runtimeContext) throws AdapterException {
		return null;
	}
	
	@Override
	public int getAdapterVersion() {
		return 1;
	}

}
