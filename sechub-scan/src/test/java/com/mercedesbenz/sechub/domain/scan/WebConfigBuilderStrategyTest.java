// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan;

import static org.junit.jupiter.api.Assertions.*;

import java.net.URI;
import java.net.URL;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.adapter.AbstractWebScanAdapterConfig;
import com.mercedesbenz.sechub.adapter.AbstractWebScanAdapterConfigBuilder;
import com.mercedesbenz.sechub.adapter.LoginConfig;
import com.mercedesbenz.sechub.adapter.LoginScriptAction;
import com.mercedesbenz.sechub.adapter.LoginScriptPage;
import com.mercedesbenz.sechub.adapter.SecHubTimeUnitData;
import com.mercedesbenz.sechub.commons.model.SecHubTimeUnit;
import com.mercedesbenz.sechub.sharedkernel.configuration.SecHubConfiguration;

public class WebConfigBuilderStrategyTest {

    private static final SecHubConfiguration SECHUB_CONFIG = new SecHubConfiguration();

    @Test
    public void no_authentication() throws Exception {
        /* prepare */
        WebConfigBuilderStrategy strategyToTest = createStrategy("sechub_config/webscan_no_auth.json");
        URI expectedUrl = URI.create("https://productfailure.demo.example.org");
        TestAbstractWebScanAdapterConfigBuilder configBuilder = new TestAbstractWebScanAdapterConfigBuilder();

        /* execute */
        strategyToTest.configure(configBuilder);

        /* test */
        TestWebScanAdapterConfig result = configBuilder.build();

        assertEquals(expectedUrl, result.getTargetURI());
    }

    @Test
    public void includes_excludes() throws Exception {
        /* prepare */
        WebConfigBuilderStrategy strategyToTest = createStrategy("sechub_config/webscan_no_auth_includes_excludes.json");
        URI expectedUrl = URI.create("https://productfailure.demo.example.org");
        TestAbstractWebScanAdapterConfigBuilder configBuilder = new TestAbstractWebScanAdapterConfigBuilder();

        Set<String> expectedIncludes = new HashSet<>();
        expectedIncludes.add("/admin");
        expectedIncludes.add("/support/hidden.html");
        expectedIncludes.add("/hidden");

        Set<String> expectedExcludes = new HashSet<>();
        expectedExcludes.add("/contact.html");
        expectedExcludes.add("/public");
        expectedExcludes.add("/static");

        /* execute */
        strategyToTest.configure(configBuilder);

        /* test */
        TestWebScanAdapterConfig result = configBuilder.build();

        assertEquals(expectedUrl, result.getTargetURI());
        assertEquals(expectedExcludes, result.getExcludes());
        assertEquals(expectedIncludes, result.getIncludes());
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
    public void basic_authentication_without_optional_realm() throws Exception {
        /* prepare */
        WebConfigBuilderStrategy strategyToTest = createStrategy("sechub_config/webscan_login_basic_without_optional_realm.json");
        TestAbstractWebScanAdapterConfigBuilder configBuilder = new TestAbstractWebScanAdapterConfigBuilder();

        /* execute */
        strategyToTest.configure(configBuilder);

        /* test */
        TestWebScanAdapterConfig result = configBuilder.build();
        LoginConfig loginConfig = result.getLoginConfig();
        assertTrue(loginConfig.isBasic());
        assertEquals("user0", loginConfig.asBasic().getUser());
        assertEquals("pwd0", loginConfig.asBasic().getPassword());
        assertEquals(null, loginConfig.asBasic().getRealm());

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
        String json = TestScanDomainFileSupport.getTestfileSupport().loadTestFile(pathToTestConfig);
        SecHubConfiguration configuration = SECHUB_CONFIG.fromJSON(json);

        return new SecHubExecutionContext(UUID.randomUUID(), configuration, "test", UUID.randomUUID());

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
