// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter.nessus;

import com.daimler.sechub.adapter.AbstractAdapterConfigBuilder;
import com.daimler.sechub.adapter.AbstractInfraScanAdapterConfig;

public class NessusConfig extends AbstractInfraScanAdapterConfig implements NessusAdapterConfig{

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
