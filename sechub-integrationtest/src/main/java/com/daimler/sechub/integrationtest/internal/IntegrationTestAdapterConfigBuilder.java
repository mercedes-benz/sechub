// SPDX-License-Identifier: MIT
package com.daimler.sechub.integrationtest.internal;

import com.daimler.sechub.adapter.AbstractAdapterConfigBuilder;

public class IntegrationTestAdapterConfigBuilder extends AbstractAdapterConfigBuilder<IntegrationTestAdapterConfigBuilder, IntegrationTestAdapterConfigInterface>{

	@Override
	protected void customBuild(IntegrationTestAdapterConfigInterface config) {
		
	}

	@Override
	protected IntegrationTestAdapterConfigInterface buildInitialConfig() {
		return new IntegrationTestAdapterConfig();
	}

	@Override
	protected void customValidate() {
		
	}

}