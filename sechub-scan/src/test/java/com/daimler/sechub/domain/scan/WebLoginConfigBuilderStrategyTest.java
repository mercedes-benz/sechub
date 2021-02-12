// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan;

import static org.junit.Assert.*;

import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.junit.Test;

import com.daimler.sechub.adapter.AbstractWebScanAdapterConfig;
import com.daimler.sechub.adapter.AbstractWebScanAdapterConfigBuilder;
import com.daimler.sechub.adapter.LoginConfig;
import com.daimler.sechub.adapter.LoginScriptStep;
import com.daimler.sechub.adapter.SecHubTimeUnit;
import com.daimler.sechub.sharedkernel.configuration.SecHubConfiguration;
import com.daimler.sechub.sharedkernel.execution.SecHubExecutionContext;

public class WebLoginConfigBuilderStrategyTest {

	private static final SecHubConfiguration SECHUB_CONFIG = new SecHubConfiguration();

	@Test
	public void basic_login_data_transfered() throws Exception{
		/* prepare */
		WebLoginConfigBuilderStrategy strategyToTest = createStrategy("sechub_config/webscan_login_basic.json");
		TestAbstractWebScanAdapterConfigBuilder configBuilder = new TestAbstractWebScanAdapterConfigBuilder();

		/* execute */
		strategyToTest.configure(configBuilder);

		/* test */
		TestWebScanAdapterConfig result = configBuilder.build();
		LoginConfig loginConfig = result.getLoginConfig();
		assertTrue(loginConfig.isBasic());
		assertEquals("user0", loginConfig.asBasic().getUser());
		assertEquals("pwd0", loginConfig.asBasic().getPassword());
		assertEquals("realm0", loginConfig.asBasic().getRealm());
		assertEquals(new URL("https://productfailure.demo.example.org/login"), loginConfig.asBasic().getLoginURL());

	}

	@Test
	public void form_autodetect_login_data_transfered() {
		/* prepare */
		WebLoginConfigBuilderStrategy strategyToTest = createStrategy("sechub_config/webscan_login_form_autodetect.json");
		TestAbstractWebScanAdapterConfigBuilder configBuilder = new TestAbstractWebScanAdapterConfigBuilder();

		/* execute */
		strategyToTest.configure(configBuilder);

		/* test */
		TestWebScanAdapterConfig result = configBuilder.build();
		LoginConfig loginConfig = result.getLoginConfig();
		assertTrue(loginConfig.isFormAutoDetect());
		assertEquals("user1", loginConfig.asFormAutoDetect().getUser());
		assertEquals("pwd1", loginConfig.asFormAutoDetect().getPassword());
	}

	@Test
	public void form_script_login_data_transfered() {
		/* prepare */
		WebLoginConfigBuilderStrategy strategyToTest = createStrategy("sechub_config/webscan_login_form_script.json");
		TestAbstractWebScanAdapterConfigBuilder configBuilder = new TestAbstractWebScanAdapterConfigBuilder();

		/* execute */
		strategyToTest.configure(configBuilder);

		/* test */
		TestWebScanAdapterConfig result = configBuilder.build();
		LoginConfig loginConfig = result.getLoginConfig();
		assertTrue(loginConfig.isFormScript());
		List<LoginScriptStep> steps = loginConfig.asFormScript().getSteps();
		assertEquals(5,steps.size());
		Iterator<LoginScriptStep> it = steps.iterator();

		LoginScriptStep step = it.next();
		assertEquals("username", step.getAction().toString());
		assertEquals("#example_login_userid", step.getSelector());
		assertEquals("user2", step.getValue());

        step = it.next();
        assertEquals("click", step.getAction().toString());
        assertEquals("#next_button", step.getSelector());
        assertEquals(null, step.getValue());

        step = it.next();
        assertEquals("wait", step.getAction().toString());
        assertEquals("2456", step.getValue());
        assertEquals(SecHubTimeUnit.MILLISECOND, step.getUnit());

        step = it.next();
        assertEquals("password", step.getAction().toString());
        assertEquals("#example_login_pwd", step.getSelector());
        assertEquals("pwd2", step.getValue());

        step = it.next();
        assertEquals("click", step.getAction().toString());
        assertEquals("#example_login_login_button", step.getSelector());
        assertEquals(null, step.getValue());
	}

	private WebLoginConfigBuilderStrategy createStrategy(String path) {
		return new WebLoginConfigBuilderStrategy(createContext(path));
	}

	private SecHubExecutionContext createContext(String pathToTestConfig) {
		String json = ScanDomainTestFileSupport.getTestfileSupport().loadTestFile(pathToTestConfig);
		SecHubConfiguration configuration = SECHUB_CONFIG.fromJSON(json);

		return new SecHubExecutionContext(UUID.randomUUID(), configuration, "test");

	}

	private class TestAbstractWebScanAdapterConfigBuilder
			extends AbstractWebScanAdapterConfigBuilder<TestAbstractWebScanAdapterConfigBuilder, TestWebScanAdapterConfig> {

		@Override
		protected void customBuild(TestWebScanAdapterConfig config) {

		}

		@Override
		protected TestWebScanAdapterConfig buildInitialConfig() {
			return new TestWebScanAdapterConfig();
		}

		@Override
		protected void customValidate() {

		}

	}

	private class TestWebScanAdapterConfig extends AbstractWebScanAdapterConfig {

	}
}
