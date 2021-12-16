// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan;

import static org.junit.Assert.*;

import java.net.URI;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.junit.Test;

import com.daimler.sechub.adapter.AbstractWebScanAdapterConfig;
import com.daimler.sechub.adapter.AbstractWebScanAdapterConfigBuilder;
import com.daimler.sechub.adapter.LoginConfig;
import com.daimler.sechub.adapter.LoginScriptAction;
import com.daimler.sechub.adapter.LoginScriptPage;
import com.daimler.sechub.adapter.SecHubTimeUnitData;
import com.daimler.sechub.commons.model.SecHubTimeUnit;
import com.daimler.sechub.sharedkernel.configuration.SecHubConfiguration;
import com.daimler.sechub.sharedkernel.execution.SecHubExecutionContext;

public class WebConfigBuilderStrategyTest {

    private static final SecHubConfiguration SECHUB_CONFIG = new SecHubConfiguration();

    @Test
    public void no_authentication() throws Exception {
        /* prepare */
        WebConfigBuilderStrategy strategyToTest = createStrategy("sechub_config/webscan_no_auth.json");
        String expectedUrl = URI.create("https://productfailure.demo.example.org/").toString();
        TestAbstractWebScanAdapterConfigBuilder configBuilder = new TestAbstractWebScanAdapterConfigBuilder();

        /* execute */
        strategyToTest.configure(configBuilder);

        /* test */
        TestWebScanAdapterConfig result = configBuilder.build();

        assertEquals(expectedUrl, result.getTargetURI());
    }
    
    @Test
    public void basic_login_data_transfered() throws Exception {
        /* prepare */
        WebConfigBuilderStrategy strategyToTest = createStrategy("sechub_config/webscan_login_basic.json");
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

        // we test external forms - reason: Depending on the JDK implementation URL
        // equals compares also content so extreme slow. So we use external form
        // to compare with each other - it is much faster.
        String fetchedUrlExternalFrom = loginConfig.asBasic().getLoginURL().toExternalForm();
        String expectedUrlExternalForm = new URL("https://productfailure.demo.example.org/login").toExternalForm();
        assertEquals(expectedUrlExternalForm, fetchedUrlExternalFrom);

    }

    @Test
    public void webscan_max_scan_duration() {
        /* prepare */
        WebConfigBuilderStrategy strategyToTest = createStrategy("sechub_config/webscan_max_scan_duration.json");
        TestAbstractWebScanAdapterConfigBuilder configBuilder = new TestAbstractWebScanAdapterConfigBuilder();
        SecHubTimeUnitData expectedMaxScanDuration = SecHubTimeUnitData.of(1, SecHubTimeUnit.HOUR);

        /* execute */
        strategyToTest.configure(configBuilder);

        /* test */
        TestWebScanAdapterConfig result = configBuilder.build();
        SecHubTimeUnitData maxScanDuration = result.getMaxScanDuration();
        assertNotNull(maxScanDuration);
        assertEquals(expectedMaxScanDuration, maxScanDuration);
    }

    @Test
    public void form_script_login_data_transfered() {
        /* prepare */
        WebConfigBuilderStrategy strategyToTest = createStrategy("sechub_config/webscan_login_form_script.json");
        TestAbstractWebScanAdapterConfigBuilder configBuilder = new TestAbstractWebScanAdapterConfigBuilder();

        /* execute */
        strategyToTest.configure(configBuilder);

        /* test */
        TestWebScanAdapterConfig result = configBuilder.build();
        LoginConfig loginConfig = result.getLoginConfig();
        assertTrue(loginConfig.isFormScript());

        List<LoginScriptPage> pages = loginConfig.asFormScript().getPages();
        assertEquals(2, pages.size());

        /* page 1 */
        List<LoginScriptAction> actions = loginConfig.asFormScript().getPages().get(0).getActions();
        assertEquals(2, actions.size());
        Iterator<LoginScriptAction> iterator = actions.iterator();

        LoginScriptAction action = iterator.next();
        assertEquals("username", action.getActionType().toString());
        assertEquals("#example_login_userid", action.getSelector());
        assertEquals("user2", action.getValue());

        action = iterator.next();
        assertEquals("click", action.getActionType().toString());
        assertEquals("#next_button", action.getSelector());
        assertEquals(null, action.getValue());

        /* page 2 */
        List<LoginScriptAction> actions2 = loginConfig.asFormScript().getPages().get(1).getActions();
        assertEquals(3, actions2.size());
        Iterator<LoginScriptAction> iterator2 = actions2.iterator();

        action = iterator2.next();
        assertEquals("wait", action.getActionType().toString());
        assertEquals("2456", action.getValue());
        assertEquals(SecHubTimeUnit.MILLISECOND, action.getUnit());

        action = iterator2.next();
        assertEquals("password", action.getActionType().toString());
        assertEquals("#example_login_pwd", action.getSelector());
        assertEquals("pwd2", action.getValue());

        action = iterator2.next();
        assertEquals("click", action.getActionType().toString());
        assertEquals("#example_login_login_button", action.getSelector());
        assertEquals(null, action.getValue());
    }

    private WebConfigBuilderStrategy createStrategy(String path) {
        return new WebConfigBuilderStrategy(createContext(path));
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
