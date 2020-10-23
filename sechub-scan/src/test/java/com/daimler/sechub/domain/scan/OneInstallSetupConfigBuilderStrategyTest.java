// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Test;

import com.daimler.sechub.adapter.AbstractAdapterConfig;
import com.daimler.sechub.adapter.AbstractAdapterConfigBuilder;

public class OneInstallSetupConfigBuilderStrategyTest {

	@Test
	public void setup_data_used() {
		/* prepare */
		OneInstallSetup setup = mock(OneInstallSetup.class);
		when(setup.getBaseURL()).thenReturn("http://www.example.com/baseurl");
		when(setup.getPassword()).thenReturn("mypwd");
		when(setup.getUserId()).thenReturn("myuser");
		when(setup.isHavingUntrustedCertificate()).thenReturn(true);

		OneInstallSetupConfigBuilderStrategy strategyToTest = new OneInstallSetupConfigBuilderStrategy(setup);
		TestAdapterConfigBuilder configBuilder = new TestAdapterConfigBuilder();

		/* execute */
		strategyToTest.configure(configBuilder);

		/* test */
		TestAdapterConfig result = configBuilder.build();
		assertEquals("myuser",  result.getUser());
		assertEquals("mypwd", result.getPasswordOrAPIToken());
		assertNotNull(result.getCredentialsBase64Encoded());
		assertEquals("http://www.example.com/baseurl", result.getProductBaseURL());
		assertTrue(result.isTrustAllCertificatesEnabled());

	}


	private class TestAdapterConfigBuilder
			extends AbstractAdapterConfigBuilder<TestAdapterConfigBuilder, TestAdapterConfig> {

		@Override
		protected void customBuild(TestAdapterConfig config) {

		}

		@Override
		protected TestAdapterConfig buildInitialConfig() {
			return new TestAdapterConfig();
		}

		@Override
		protected void customValidate() {

		}

	}

	private class TestAdapterConfig extends AbstractAdapterConfig {

	}
}
