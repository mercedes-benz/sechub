// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter.testclasses;

import com.daimler.sechub.adapter.AbstractAdapterConfigBuilder;

public class TestAdapterConfigBuilder extends AbstractAdapterConfigBuilder<TestAdapterConfigBuilder, TestAdapterConfigInterface>{

	@Override
	protected void customBuild(TestAdapterConfigInterface config) {
		
	}

	@Override
	protected TestAdapterConfigInterface buildInitialConfig() {
		return new TestAdapterConfig();
	}

	@Override
	protected void customValidate() {
		
	}

}