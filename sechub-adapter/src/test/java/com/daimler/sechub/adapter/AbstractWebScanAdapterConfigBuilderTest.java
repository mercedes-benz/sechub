// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter;

import static org.junit.Assert.*;

import java.util.Iterator;
import java.util.List;

import org.junit.Test;

public class AbstractWebScanAdapterConfigBuilderTest {

	@Test
	public void login_basic() {
		/* @formatter:off */
		TestWebScanAdapterConfig webScanConfig = new TestAbstractWebScanAdapterConfigBuilder().
				login().
					basic().
						username("user1").
						password("passwd1").
						realm("realm1").
				endLogin()
				.build();
		/* @formatter:on */

		/* test */
		LoginConfig config = webScanConfig.getLoginConfig();
		assertNotNull(config);
		assertTrue(null, config.isBasic());
		assertEquals("user1",config.asBasic().getUser());
		assertEquals("passwd1",config.asBasic().getPassword());
		assertEquals("realm1",config.asBasic().getRealm());
	}

	@Test
	public void login_form_automated() {
		/* @formatter:off */
		TestWebScanAdapterConfig x = new TestAbstractWebScanAdapterConfigBuilder().
				login().
					form().autoDetect().
						username("user1").
						password("passwd1").
				endLogin()
				.build();
		/* @formatter:on */

		/* test */
		assertNotNull(x);
		LoginConfig config = x.getLoginConfig();
		assertNotNull(config);
		assertTrue(null, config.isFormAutoDetect());
		assertEquals("user1",config.asFormAutoDetect().getUser());
		assertEquals("passwd1",config.asFormAutoDetect().getPassword());
	}

	@Test
	public void login_form_scripted() {
		/* @formatter:off */
		TestWebScanAdapterConfig testAdapterConfig = new TestAbstractWebScanAdapterConfigBuilder().
				login().
					form().
					    script().
    					    addPage().
        					    addAction(ActionType.INPUT).select("#user_id").enterValue("user1").endStep().
        					    addAction(ActionType.INPUT).select("#pwd_id").enterValue("pwd1").endStep().
        					    addAction(ActionType.CLICK).select("#login_butotn_id").enterValue(null).endStep().
        					doEndPage().
        			endLogin()
				.build();
		/* @formatter:on */

		/* test */
		assertNotNull(testAdapterConfig);
		LoginConfig config = testAdapterConfig.getLoginConfig();
		assertNotNull(config);
		assertTrue(null, config.isFormScript());
		List<LoginScriptAction> actions = config.asFormScript().getPages().get(0).getActions();
		assertNotNull(actions);
		assertEquals(3,actions.size());

		Iterator<LoginScriptAction> it = actions.iterator();
		LoginScriptAction action = it.next();

		assertTrue(action.isInput());
		assertFalse(action.isClick());
		assertEquals("user1",action.getValue());
		assertEquals("#user_id",action.getSelector());

		action = it.next();
		assertTrue(action.isInput());
		assertFalse(action.isClick());
		assertEquals("pwd1",action.getValue());
		assertEquals("#pwd_id",action.getSelector());

		action = it.next();
		assertFalse(action.isInput());
		assertTrue(action.isClick());
		assertEquals("#login_butotn_id",action.getSelector());
		assertEquals(null,action.getValue());
	}



	private class TestAbstractWebScanAdapterConfigBuilder extends
			AbstractWebScanAdapterConfigBuilder<TestAbstractWebScanAdapterConfigBuilder, TestWebScanAdapterConfig> {

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
