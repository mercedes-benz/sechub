// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter;

import static org.junit.Assert.*;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;

public class AbstractWebScanAdapterConfigBuilderTest {

    @Test
    public void login_url() throws MalformedURLException {
        /* prepare */
        URL targetURI = new URL("https://example.org/login");
        
        /* execute */
        /* @formatter:off */
        TestWebScanAdapterConfig webScanConfig = new TestAbstractWebScanAdapterConfigBuilder().
                login().
                    url(targetURI).
                        basic().
                endLogin().
                build();
        /* @formatter:on */
        
        /* test */
        LoginConfig config = webScanConfig.getLoginConfig();
        assertNotNull(config);
        assertTrue(config.isBasic());
        assertEquals(targetURI, config.getLoginURL());
    }
    
    @Test
    public void login_basic() {
        /* execute */
        /* @formatter:off */
		TestWebScanAdapterConfig webScanConfig = new TestAbstractWebScanAdapterConfigBuilder().
				login().
					basic().
						username("user1").
						password("passwd1").
						realm("realm1").
				endLogin().
				build();
		/* @formatter:on */

        /* test */
        LoginConfig config = webScanConfig.getLoginConfig();
        assertNotNull(config);
        assertTrue(config.isBasic());
        assertEquals("user1", config.asBasic().getUser());
        assertEquals("passwd1", config.asBasic().getPassword());
        assertEquals("realm1", config.asBasic().getRealm());
    }

    @Test
    public void login_form_automated() {
        /* execute */
        /* @formatter:off */
		TestWebScanAdapterConfig webScanConfig = new TestAbstractWebScanAdapterConfigBuilder().
				login().
					form().autoDetect().
						username("user1").
						password("passwd1").
				endLogin().
				build();
		/* @formatter:on */

        /* test */
        assertNotNull(webScanConfig);
        LoginConfig config = webScanConfig.getLoginConfig();
        assertNotNull(config);
        assertTrue(null, config.isFormAutoDetect());
        assertEquals("user1", config.asFormAutoDetect().getUser());
        assertEquals("passwd1", config.asFormAutoDetect().getPassword());
    }

    @Test
    public void login_form_scripted() {
        /* execute */
        /* @formatter:off */
		TestWebScanAdapterConfig testAdapterConfig = new TestAbstractWebScanAdapterConfigBuilder().
				login().
					form().
					    script().
    					    addPage().
        					    addAction(ActionType.USERNAME).select("#user_id").enterValue("user1").endStep().
        					    addAction(ActionType.PASSWORD).select("#pwd_id").enterValue("pwd1").endStep().
        					    addAction(ActionType.INPUT).
        					        select("#loginForm > label > input['email']").
        					        enterValue("user1@example.org").
        					        description("Email field").
        					        endStep().
        					    addAction(ActionType.WAIT).unit(SecHubTimeUnit.SECOND).enterValue("2").endStep().
        					    addAction(ActionType.CLICK).select("#login_button_id").enterValue(null).endStep().
        					doEndPage().
        			endLogin().
				build();
		/* @formatter:on */

        /* test */
        assertNotNull(testAdapterConfig);
        LoginConfig config = testAdapterConfig.getLoginConfig();
        assertNotNull(config);
        assertTrue(null, config.isFormScript());
        List<LoginScriptAction> actions = config.asFormScript().getPages().get(0).getActions();
        assertNotNull(actions);
        assertEquals(5, actions.size());

        Iterator<LoginScriptAction> it = actions.iterator();
        LoginScriptAction action = it.next();

        assertEquals(ActionType.USERNAME, action.getActionType());
        assertTrue(action.isUserName());
        assertEquals("user1", action.getValue());
        assertEquals("#user_id", action.getSelector());
        assertNull(action.getUnit());
        assertNull(action.getDescription());
        
        action = it.next();
        assertEquals(ActionType.PASSWORD, action.getActionType());
        assertTrue(action.isPassword());
        assertEquals("pwd1", action.getValue());
        assertEquals("#pwd_id", action.getSelector());
        assertNull(action.getUnit());
        assertNull(action.getDescription());

        action = it.next();
        assertEquals(ActionType.INPUT, action.getActionType());
        assertTrue(action.isInput());
        assertEquals("user1@example.org", action.getValue());
        assertEquals("#loginForm > label > input['email']", action.getSelector());
        assertEquals("Email field", action.getDescription());
        assertNull(action.getUnit());
        
        action = it.next();
        assertEquals(ActionType.WAIT, action.getActionType());
        assertTrue(action.isWait());
        assertEquals("2", action.getValue());
        assertEquals(SecHubTimeUnit.SECOND, action.getUnit());
        assertNull(action.getSelector());
        assertNull(action.getDescription());
        
        action = it.next();
        assertEquals(ActionType.CLICK, action.getActionType());
        assertTrue(action.isClick());
        assertEquals("#login_button_id", action.getSelector());
        assertNull(action.getValue());
        assertNull(action.getUnit());
        assertNull(action.getDescription());
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
