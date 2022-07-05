// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.adapter;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import com.mercedesbenz.sechub.adapter.support.URIShrinkSupport;
import com.mercedesbenz.sechub.adapter.testclasses.TestWebScanAdapterConfigBuilder;
import com.mercedesbenz.sechub.adapter.testclasses.TestWebScanAdapterConfigInterface;
import com.mercedesbenz.sechub.commons.model.SecHubTimeUnit;
import com.mercedesbenz.sechub.commons.model.login.ActionType;

public class AbstractWebScanAdapterConfigBuilderTest {

    @Test
    public void target_type_from_builder_is_in_config() throws Exception {

        /* execute */
        /* @formatter:off */
        TestWebScanAdapterConfig webScanConfig = new TestAbstractWebScanAdapterConfigBuilder().
        		setTargetType("the-type").build();
        /* @formatter:on */

        /* test */
        String targetType = webScanConfig.getTargetType();
        assertEquals("the-type", targetType);
    }

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

    @Test
    public void when_one_target_uri_is_set__target_uri_is_as_expected() throws Exception {

        /* prepare */
        String uriString = "http://www.my.cool.stuff.com";
        URI uri = new URI(uriString);

        /* execute */
        TestWebScanAdapterConfigInterface configToTest = validConfigAnd().setTargetURI(uri).build();

        /* test */
        assertEquals(uri, configToTest.getTargetURI());
        assertEquals(uriString, configToTest.getTargetAsString());
    }

    @Test
    public void when_no_target_url_set_the_config_has_null_as_target_uri() throws Exception {

        /* execute */
        TestWebScanAdapterConfigInterface configToTest = validConfigAnd().build();

        /* test */
        assertNull(configToTest.getTargetURI());
        assertNull(configToTest.getTargetAsString());

    }

    @Test
    public void when_target_uri_is_set_by_string__target_uri_is_as_expected() throws Exception {
        /* prepare */
        String uriString = "http://www.my.cool.stuff.com";
        URI uri = new URI(uriString);
        /* execute */

        TestWebScanAdapterConfigInterface configToTest = validConfigAnd().setTargetURI(uri).build();

        /* test */
        assertEquals(uri, configToTest.getTargetURI());
        assertEquals(uriString, configToTest.getTargetAsString());
    }

    @Test
    public void test_includes() {
        /* prepare */
        Set<String> includes = new HashSet<>();
        includes.add("/abc");
        includes.add("/hidden");
        includes.add("/bca/gda#ab");
        includes.add("/cba/abc/cdb?abc=3");

        /* execute */
        TestWebScanAdapterConfig testAdapterConfig = new TestAbstractWebScanAdapterConfigBuilder().setIncludes(includes).build();

        /* test */
        assertNotNull(testAdapterConfig);
        assertEquals(includes, testAdapterConfig.getIncludes());
    }

    @Test
    public void test_excludes() {
        /* prepare */
        Set<String> excludes = new HashSet<>();
        excludes.add("/abc");
        excludes.add("/hidden");
        excludes.add("/bca/gda#ab");
        excludes.add("/cba/abc/cdb?abc=3");

        /* execute */
        TestWebScanAdapterConfig testAdapterConfig = new TestAbstractWebScanAdapterConfigBuilder().setExcludes(excludes).build();

        /* test */
        assertNotNull(testAdapterConfig);
        assertEquals(excludes, testAdapterConfig.getExcludes());
    }

    @Test
    public void rootURIShrinker_is_used_when_building() throws Exception {

        /* prepare */
        URIShrinkSupport shrinker = mock(URIShrinkSupport.class);
        TestWebScanAdapterConfigBuilder builderToTest = new TestWebScanAdapterConfigBuilder() {
            protected URIShrinkSupport createURIShrinkSupport() {
                return shrinker;
            }
        };

        builderToTest.setProductBaseUrl("baseUrl");
        URI targetURI = new URI("http://www.mycoolstuff.com/app1");

        URI mockedShrink = new URI("http://www.shrinked.com");

        when(shrinker.shrinkToRootURI(eq(targetURI))).thenReturn(mockedShrink);

        /* execute */
        TestWebScanAdapterConfigInterface config = builderToTest.setTargetURI(targetURI).build();

        /* test */
        verify(shrinker).shrinkToRootURI(eq(targetURI));
        assertEquals(mockedShrink, config.getRootTargetURI());
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

    private TestWebScanAdapterConfigBuilder validConfigAnd() {
        return new TestWebScanAdapterConfigBuilder().setProductBaseUrl("baseUrl");
    }
}
