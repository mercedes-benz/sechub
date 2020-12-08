// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Test;

import com.daimler.sechub.adapter.AbstractAdapterConfig;
import com.daimler.sechub.adapter.AbstractAdapterConfigBuilder;

public class TargetIdentifyingMultiInstallSetupConfigBuilderStrategyTest {

	@Test
	public void setup_data_used_from_selected_target_type() {
		/* prepare */
		TargetIdentifyingMultiInstallSetup setup = mock(TargetIdentifyingMultiInstallSetup.class);
		TargetType type = TargetType.INTRANET;
		when(setup.getBaseURL(type)).thenReturn("http://192.168.178.131/baseurl/intranet");
		when(setup.getPassword(type)).thenReturn("mypwd1");
		when(setup.getUserId(type)).thenReturn("myuser1");
		when(setup.isHavingUntrustedCertificate(type)).thenReturn(true);

		type = TargetType.INTERNET;
		when(setup.getBaseURL(type)).thenReturn("http://www.example.com/baseurl");
		when(setup.getPassword(type)).thenReturn("mypwd666");
		when(setup.getUserId(type)).thenReturn("myuser666");
		when(setup.isHavingUntrustedCertificate(type)).thenReturn(false);

		TargetIdentifyingMultiInstallSetupConfigBuilderStrategy strategyToTest = new TargetIdentifyingMultiInstallSetupConfigBuilderStrategy(setup,TargetType.INTRANET);
		TestAdapterConfigBuilder configBuilder = new TestAdapterConfigBuilder();

		/* execute */
		strategyToTest.configure(configBuilder);

		/* test */
		TestAdapterConfig result = configBuilder.build();
		assertEquals("myuser1",  result.getUser());
		assertEquals("mypwd1", result.getPasswordOrAPIToken());
		assertNotNull(result.getCredentialsBase64Encoded());
		assertEquals("http://192.168.178.131/baseurl/intranet", result.getProductBaseURL());
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
