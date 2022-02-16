// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan;

import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.net.URI;
import java.net.URL;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

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

    @Test
    public void excludes_no_slash_infront_single() {
        /* prepare */
        List<String> excludes = new LinkedList<>();
        excludes.add("contact.html");

        String json = createExcludesJson(excludes);
        SecHubConfiguration configuration = SECHUB_CONFIG.fromJSON(json);
        SecHubExecutionContext context = new SecHubExecutionContext(UUID.randomUUID(), configuration, "test");
        WebConfigBuilderStrategy strategyToTest = new WebConfigBuilderStrategy(context);
        TestAbstractWebScanAdapterConfigBuilder configBuilder = new TestAbstractWebScanAdapterConfigBuilder();

        /* execute */
        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            strategyToTest.configure(configBuilder);
        });

        /* test */
        assertEquals("The URL does not start with a slash '/'. URL: contact.html", exception.getMessage());
    }

    @Test
    public void excludes_no_slash_infront_multiple() {
        /* prepare */
        List<String> excludes = new LinkedList<>();

        excludes.add("/abc");
        excludes.add("contact.html");
        excludes.add("/bca");
        excludes.add("ccb/bca");

        String json = createExcludesJson(excludes);
        SecHubConfiguration configuration = SECHUB_CONFIG.fromJSON(json);
        SecHubExecutionContext context = new SecHubExecutionContext(UUID.randomUUID(), configuration, "test");
        WebConfigBuilderStrategy strategyToTest = new WebConfigBuilderStrategy(context);
        TestAbstractWebScanAdapterConfigBuilder configBuilder = new TestAbstractWebScanAdapterConfigBuilder();

        /* execute */
        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            strategyToTest.configure(configBuilder);
        });

        /* test */
        assertEquals("The URL does not start with a slash '/'. URL: contact.html", exception.getMessage());
    }

    @Test
    public void includes_no_slash_infront_single() {
        /* prepare */
        List<String> includes = new LinkedList<>();
        includes.add("contact.html");

        String json = createIncludesJson(includes);
        SecHubConfiguration configuration = SECHUB_CONFIG.fromJSON(json);
        SecHubExecutionContext context = new SecHubExecutionContext(UUID.randomUUID(), configuration, "test");
        WebConfigBuilderStrategy strategyToTest = new WebConfigBuilderStrategy(context);
        TestAbstractWebScanAdapterConfigBuilder configBuilder = new TestAbstractWebScanAdapterConfigBuilder();

        /* execute */
        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            strategyToTest.configure(configBuilder);
        });

        /* test */
        assertEquals("The URL does not start with a slash '/'. URL: contact.html", exception.getMessage());
    }

    @Test
    public void includes_no_slash_infront_multiple() {
        /* prepare */
        List<String> includes = new LinkedList<>();
        includes.add("/abc");
        includes.add("contact.html");
        includes.add("/hidden");
        includes.add("ccb/bca");

        String json = createIncludesJson(includes);
        SecHubConfiguration configuration = SECHUB_CONFIG.fromJSON(json);
        SecHubExecutionContext context = new SecHubExecutionContext(UUID.randomUUID(), configuration, "test");
        WebConfigBuilderStrategy strategyToTest = new WebConfigBuilderStrategy(context);
        TestAbstractWebScanAdapterConfigBuilder configBuilder = new TestAbstractWebScanAdapterConfigBuilder();

        /* execute */
        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            strategyToTest.configure(configBuilder);
        });

        /* test */
        assertEquals("The URL does not start with a slash '/'. URL: contact.html", exception.getMessage());
    }

    @Test
    public void too_many_excludes() {
        /* prepare */
        List<String> excludes = new LinkedList<>();
        for (int i = 1; i <= 501; i++) {
            excludes.add("/myapp" + i);
        }

        String json = createExcludesJson(excludes);
        SecHubConfiguration configuration = SECHUB_CONFIG.fromJSON(json);
        SecHubExecutionContext context = new SecHubExecutionContext(UUID.randomUUID(), configuration, "test");
        WebConfigBuilderStrategy strategyToTest = new WebConfigBuilderStrategy(context);
        TestAbstractWebScanAdapterConfigBuilder configBuilder = new TestAbstractWebScanAdapterConfigBuilder();

        /* execute */
        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            strategyToTest.configure(configBuilder);
        });

        /* test */
        assertEquals("A maximum of 500 excludes are allowed.", exception.getMessage());
    }

    @Test
    public void too_many_includes() {
        /* prepare */
        List<String> includes = new LinkedList<>();
        for (int i = 1; i <= 501; i++) {
            includes.add("/myapp" + i);
        }

        String json = createIncludesJson(includes);
        SecHubConfiguration configuration = SECHUB_CONFIG.fromJSON(json);
        SecHubExecutionContext context = new SecHubExecutionContext(UUID.randomUUID(), configuration, "test");
        WebConfigBuilderStrategy strategyToTest = new WebConfigBuilderStrategy(context);
        TestAbstractWebScanAdapterConfigBuilder configBuilder = new TestAbstractWebScanAdapterConfigBuilder();

        /* execute */
        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            strategyToTest.configure(configBuilder);
        });

        /* test */
        assertEquals("A maximum of 500 includes are allowed.", exception.getMessage());
    }

    @Test
    public void exclude_too_long() {
        /* prepare */
        // create long string
        StringBuilder sb = new StringBuilder();
        sb.append("/");

        for (int i = 0; i < 64; i++) {
            sb.append("abcdefghijklmnopqrstuvwxyz012345");
        }

        List<String> excludes = new LinkedList<>();
        excludes.add(sb.toString());

        String json = createExcludesJson(excludes);
        SecHubConfiguration configuration = SECHUB_CONFIG.fromJSON(json);
        SecHubExecutionContext context = new SecHubExecutionContext(UUID.randomUUID(), configuration, "test");
        WebConfigBuilderStrategy strategyToTest = new WebConfigBuilderStrategy(context);
        TestAbstractWebScanAdapterConfigBuilder configBuilder = new TestAbstractWebScanAdapterConfigBuilder();

        /* execute */
        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            strategyToTest.configure(configBuilder);
        });

        /* test */
        assertThat(exception.getMessage(),
                startsWith("Maximum URL length is 2048 characters. The first 2048 characters of the URL in question: /abcdefghijklmnopqrst"));
    }

    @Test
    public void include_too_long() {
        /* prepare */
        // create long string
        StringBuilder sb = new StringBuilder();
        sb.append("/");

        for (int i = 0; i < 64; i++) {
            sb.append("abcdefghijklmnopqrstuvwxyz012345");
        }

        List<String> includes = new LinkedList<>();
        includes.add(sb.toString());

        String json = createIncludesJson(includes);
        SecHubConfiguration configuration = SECHUB_CONFIG.fromJSON(json);
        SecHubExecutionContext context = new SecHubExecutionContext(UUID.randomUUID(), configuration, "test");
        WebConfigBuilderStrategy strategyToTest = new WebConfigBuilderStrategy(context);
        TestAbstractWebScanAdapterConfigBuilder configBuilder = new TestAbstractWebScanAdapterConfigBuilder();

        /* execute */
        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            strategyToTest.configure(configBuilder);
        });

        /* test */
        assertThat(exception.getMessage(),
                startsWith("Maximum URL length is 2048 characters. The first 2048 characters of the URL in question: /abcdefghijklmnopqrst"));
    }

    private WebConfigBuilderStrategy createStrategy(String path) {
        return new WebConfigBuilderStrategy(createContext(path));
    }

    private String createExcludesJson(List<String> excludes) {
        return createIncludeOrExcludeJson(excludes, false);
    }

    private String createIncludesJson(List<String> includes) {
        return createIncludeOrExcludeJson(includes, true);
    }

    private String createIncludeOrExcludeJson(List<String> items, boolean include) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"apiVersion\": \"1.0\", ");
        sb.append("\"webScan\": {");
        sb.append("\"uri\": \"https://productfailure.demo.example.org\", ");

        if (include) {
            sb.append("\"includes\": [");
        } else {
            sb.append("\"excludes\": [");
        }

        int itemNumber = 1;
        for (String item : items) {
            sb.append("\"").append(item).append("\"");

            if (itemNumber < items.size()) {
                sb.append(",");
            }
            itemNumber++;
        }

        sb.append("]");
        sb.append("}");
        sb.append("}");

        return sb.toString();
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
