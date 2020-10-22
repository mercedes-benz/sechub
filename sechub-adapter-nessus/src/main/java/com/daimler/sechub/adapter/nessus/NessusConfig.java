// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter.nessus;

import com.daimler.sechub.adapter.AbstractAdapterConfig;
import com.daimler.sechub.adapter.AbstractAdapterConfigBuilder;

public class NessusConfig extends AbstractAdapterConfig implements NessusAdapterConfig{

	private NessusConfig() {
	}

	public static NessusConfigBuilder builder() {
		return new NessusConfigBuilder();
	}

	public static class NessusConfigBuilder extends AbstractAdapterConfigBuilder<NessusConfigBuilder, NessusAdapterConfig>{

		@Override
		protected void customBuild(NessusAdapterConfig config) {
			
		}

		@Override
		protected NessusAdapterConfig buildInitialConfig() {
			return new NessusConfig();
		}

		@Override
		protected void customValidate() {
			assertUserSet();
			assertPasswordSet();
			
		}

	}

}
