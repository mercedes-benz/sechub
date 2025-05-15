// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.configuration;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.net.InetAddress;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.mercedesbenz.sechub.commons.model.JSONConverter;
import com.mercedesbenz.sechub.commons.model.SecHubInfrastructureScanConfiguration;
import com.mercedesbenz.sechub.commons.model.SecHubLicenseScanConfiguration;
import com.mercedesbenz.sechub.commons.model.SecHubSecretScanConfiguration;
import com.mercedesbenz.sechub.commons.model.SecHubTimeUnit;
import com.mercedesbenz.sechub.commons.model.SecHubWebScanConfiguration;
import com.mercedesbenz.sechub.commons.model.WebScanDurationConfiguration;
import com.mercedesbenz.sechub.commons.model.login.Action;
import com.mercedesbenz.sechub.commons.model.login.ActionType;
import com.mercedesbenz.sechub.commons.model.login.BasicLoginConfiguration;
import com.mercedesbenz.sechub.commons.model.login.FormLoginConfiguration;
import com.mercedesbenz.sechub.commons.model.login.Page;
import com.mercedesbenz.sechub.commons.model.login.Script;
import com.mercedesbenz.sechub.commons.model.login.WebLoginConfiguration;
import com.mercedesbenz.sechub.sharedkernel.TestSharedKernelFileSupport;
import com.mercedesbenz.sechub.test.PojoTester;

public class SecHubConfigurationTest {

    private SecHubConfiguration configurationToTest;
    private static final SecHubConfiguration SECHUB_CONFIG = new SecHubConfiguration();

    @Before
    public void before() {
        configurationToTest = new SecHubConfiguration();
    }

    @Test
    public void webscan_login_basic_json_has_webconfig_as_expected() throws Exception {
        /* prepare */
        String json = TestSharedKernelFileSupport.getTestfileSupport().loadTestFile("webscan/webscan_login_basic.json");

        /* execute */
        SecHubConfiguration result = SECHUB_CONFIG.fromJSON(json);

        /* test */
        Optional<SecHubWebScanConfiguration> webScanOption = result.getWebScan();
        assertTrue("webscan config must be present", webScanOption.isPresent());

        SecHubWebScanConfiguration secHubWebScanConfiguration = webScanOption.get();
        Optional<WebLoginConfiguration> loginOption = secHubWebScanConfiguration.getLogin();
        assertTrue("login config must be present", loginOption.isPresent());
        WebLoginConfiguration loginConfiguration = loginOption.get();
        assertEquals(new URL("https://productfailure.demo.example.org/login"), loginConfiguration.getUrl());

        /*-- basic --*/
        Optional<BasicLoginConfiguration> basic = loginConfiguration.getBasic();
        assertTrue("basic login config must be present", basic.isPresent());
        assertEquals("realm0", basic.get().getRealm().get());
        assertEquals("user0", new String(basic.get().getUser()));
        assertEquals("pwd0", new String(basic.get().getPassword()));

        /*-- form --*/
        Optional<FormLoginConfiguration> form = loginConfiguration.getForm();
        assertFalse("form login config must NOT be present", form.isPresent());
    }

    @Test
    public void webscan_login_form_script_json_has_webconfig_as_expected() throws Exception {
        /* prepare */
        String json = TestSharedKernelFileSupport.getTestfileSupport().loadTestFile("webscan/webscan_login_form_script.json");

        /* execute */
        SecHubConfiguration result = SECHUB_CONFIG.fromJSON(json);

        /* test */
        Optional<SecHubWebScanConfiguration> webScanOption = result.getWebScan();
        assertTrue("webscan config must be present", webScanOption.isPresent());

        SecHubWebScanConfiguration secHubWebScanConfiguration = webScanOption.get();
        Optional<WebLoginConfiguration> loginOption = secHubWebScanConfiguration.getLogin();
        assertTrue("login config must be present", loginOption.isPresent());
        WebLoginConfiguration loginConfiguration = loginOption.get();
        assertEquals(new URL("https://productfailure.demo.example.org/login"), loginConfiguration.getUrl());

        Optional<BasicLoginConfiguration> basic = loginConfiguration.getBasic();
        assertFalse("basic login config must NOT be present", basic.isPresent());

        /*-- form --*/
        Optional<FormLoginConfiguration> form = loginConfiguration.getForm();
        assertTrue("form login config must be present", form.isPresent());

        /*-- form: script --*/
        Optional<Script> script = form.get().getScript();
        assertTrue("script config must be present", script.isPresent());

        Optional<List<Page>> pages = script.get().getPages();
        assertTrue("pages must be present", pages.isPresent());
        assertEquals("must have 1 pages", 1, pages.get().size());

        /*-- page 1 --*/
        Optional<List<Action>> page1 = pages.get().get(0).getActions();
        assertTrue("actions must be present", page1.isPresent());
        assertEquals("must have 3 action entries", 3, page1.get().size());

        Action action1 = page1.get().get(0);
        Action action2 = page1.get().get(1);
        Action action3 = page1.get().get(2);

        assertEquals(ActionType.USERNAME, action1.getType());
        assertEquals("#example_login_userid", action1.getSelector().get());
        assertEquals("user2", action1.getValue().get());

        assertEquals(ActionType.PASSWORD, action2.getType());
        assertEquals("#example_login_pwd", action2.getSelector().get());
        assertEquals("pwd2", action2.getValue().get());

        assertEquals(ActionType.CLICK, action3.getType());
        assertEquals("#example_login_login_button", action3.getSelector().get());
    }

    @Test
    public void webscan_login_form_script_with_descriptions_json_has_webconfig_as_expected() throws Exception {
        /* prepare */
        String json = TestSharedKernelFileSupport.getTestfileSupport().loadTestFile("webscan/webscan_login_form_script_with_descriptions.json");

        /* execute */
        SecHubConfiguration result = SECHUB_CONFIG.fromJSON(json);

        /* test */
        Optional<SecHubWebScanConfiguration> webScanOption = result.getWebScan();
        assertTrue("webscan config must be present", webScanOption.isPresent());

        SecHubWebScanConfiguration secHubWebScanConfiguration = webScanOption.get();
        Optional<WebLoginConfiguration> loginOption = secHubWebScanConfiguration.getLogin();
        assertTrue("login config must be present", loginOption.isPresent());
        WebLoginConfiguration loginConfiguration = loginOption.get();
        assertEquals(new URL("https://productfailure.demo.example.org/login"), loginConfiguration.getUrl());

        Optional<BasicLoginConfiguration> basic = loginConfiguration.getBasic();
        assertFalse("basic login config must NOT be present", basic.isPresent());

        /*-- form --*/
        Optional<FormLoginConfiguration> form = loginConfiguration.getForm();
        assertTrue("form login config must be present", form.isPresent());

        /*-- form: script --*/
        Optional<Script> script = form.get().getScript();
        assertTrue("script config must be present", script.isPresent());

        Optional<List<Page>> pages = script.get().getPages();
        assertTrue("pages must be present", pages.isPresent());
        assertEquals("must have 1 pages", 1, pages.get().size());

        /*-- page 1 --*/
        Optional<List<Action>> page1 = pages.get().get(0).getActions();
        assertTrue("actions must be present", page1.isPresent());
        assertEquals("must have 4 action entries", 4, page1.get().size());

        Action action1 = page1.get().get(0);
        Action action2 = page1.get().get(1);
        Action action3 = page1.get().get(2);
        Action action4 = page1.get().get(3);

        assertEquals(ActionType.USERNAME, action1.getType());
        assertEquals("#example_login_userid", action1.getSelector().get());
        assertEquals("user2", action1.getValue().get());
        assertEquals("The username is different from the email address", action1.getDescription().get());

        assertEquals(ActionType.INPUT, action2.getType());
        assertEquals("#example_login_email", action2.getSelector().get());
        assertEquals("user2@example.com", action2.getValue().get());
        assertEquals("The website has a separate field for the email address", action2.getDescription().get());

        assertEquals(ActionType.PASSWORD, action3.getType());
        assertEquals("#example_login_pwd", action3.getSelector().get());
        assertEquals("pwd2", action3.getValue().get());

        assertEquals(ActionType.CLICK, action4.getType());
        assertEquals("#example_login_login_button", action4.getSelector().get());
    }

    @Test
    public void webscan_login_form_script_with_wait_json_has_webconfig_as_expected() throws Exception {
        /* prepare */
        String json = TestSharedKernelFileSupport.getTestfileSupport().loadTestFile("webscan/webscan_login_form_script_with_wait.json");

        /* execute */
        SecHubConfiguration result = SECHUB_CONFIG.fromJSON(json);

        /* test */
        Optional<SecHubWebScanConfiguration> webScanOption = result.getWebScan();
        assertTrue("webscan config must be present", webScanOption.isPresent());

        SecHubWebScanConfiguration secHubWebScanConfiguration = webScanOption.get();
        Optional<WebLoginConfiguration> loginOption = secHubWebScanConfiguration.getLogin();
        assertTrue("login config must be present", loginOption.isPresent());
        WebLoginConfiguration loginConfiguration = loginOption.get();
        assertEquals(new URL("https://productfailure.demo.example.org/login"), loginConfiguration.getUrl());

        Optional<BasicLoginConfiguration> basic = loginConfiguration.getBasic();
        assertFalse("basic login config must NOT be present", basic.isPresent());

        /*-- form --*/
        Optional<FormLoginConfiguration> form = loginConfiguration.getForm();
        assertTrue("form login config must be present", form.isPresent());

        /*-- form : script --*/
        Optional<Script> script = form.get().getScript();
        assertTrue("script config must be present", script.isPresent());

        Optional<List<Page>> pages = script.get().getPages();
        assertTrue("pages must be present", pages.isPresent());
        assertEquals("must have 1 pages", 1, pages.get().size());

        /*-- page 1 --*/
        Optional<List<Action>> page1 = pages.get().get(0).getActions();
        assertTrue("actions must be present", page1.isPresent());
        assertEquals("must have 4 action entries", 4, page1.get().size());

        Action action1 = page1.get().get(0);
        Action action2 = page1.get().get(1);
        Action action3 = page1.get().get(2);
        Action action4 = page1.get().get(3);

        assertEquals(ActionType.INPUT, action1.getType());
        assertEquals("#example_login_userid", action1.getSelector().get());
        assertEquals("user2", action1.getValue().get());

        assertEquals(ActionType.WAIT, action2.getType());
        assertEquals("1458", action2.getValue().get());
        assertEquals(SecHubTimeUnit.MILLISECOND, action2.getUnit().get());

        assertEquals(ActionType.INPUT, action3.getType());
        assertEquals("#example_login_pwd", action3.getSelector().get());
        assertEquals("pwd2", action3.getValue().get());

        assertEquals(ActionType.CLICK, action4.getType());
        assertEquals("#example_login_login_button", action4.getSelector().get());
    }

    @Test
    public void webscan_alloptions_json_has_webconfig_with_all_examples() throws Exception {
        /* prepare */
        String json = TestSharedKernelFileSupport.getTestfileSupport().loadTestFile("webscan/webscan_alloptions.json");

        /* execute */
        SecHubConfiguration result = SECHUB_CONFIG.fromJSON(json);

        /* test */
        Optional<SecHubWebScanConfiguration> webScanOption = result.getWebScan();
        assertTrue("webscan config must be present", webScanOption.isPresent());

        SecHubWebScanConfiguration secHubWebScanConfiguration = webScanOption.get();
        assertEquals(URI.create("https://productfailure.demo.example.org"), secHubWebScanConfiguration.getUrl());

        Optional<List<String>> includes = secHubWebScanConfiguration.getIncludes();
        assertTrue("includes must be present", includes.isPresent());
        List<String> expectedIncludes = Arrays.asList("/portal/admin", "/abc.html", "/hidden");
        assertEquals(expectedIncludes, includes.get());

        Optional<List<String>> excludes = secHubWebScanConfiguration.getExcludes();
        assertTrue("excludes must be present", excludes.isPresent());
        List<String> expectedExcludes = Arrays.asList("/public/media", "/contact.html", "/static");
        assertEquals(expectedExcludes, excludes.get());

        Optional<WebScanDurationConfiguration> maxScanDuration = secHubWebScanConfiguration.getMaxScanDuration();
        assertTrue("max san duration config must be present", maxScanDuration.isPresent());
        assertEquals(2, maxScanDuration.get().getDuration());
        assertEquals(SecHubTimeUnit.HOUR, maxScanDuration.get().getUnit());

        Optional<WebLoginConfiguration> loginOption = secHubWebScanConfiguration.getLogin();
        assertTrue("login config must be present", loginOption.isPresent());
        WebLoginConfiguration loginConfiguration = loginOption.get();
        assertEquals(new URL("https://productfailure.demo.example.org/login"), loginConfiguration.getUrl());

        /*-- basic --*/
        Optional<BasicLoginConfiguration> basic = loginConfiguration.getBasic();
        assertTrue("basic login config must be present", basic.isPresent());
        assertEquals("realm0", basic.get().getRealm().get());
        assertEquals("user0", new String(basic.get().getUser()));
        assertEquals("pwd0", new String(basic.get().getPassword()));

        /*-- form --*/
        Optional<FormLoginConfiguration> form = loginConfiguration.getForm();
        assertTrue("form login config must be present", form.isPresent());

        /*-- form : script --*/
        Optional<Script> script = form.get().getScript();
        assertTrue("script config must be present", script.isPresent());

        Optional<List<Page>> pages = script.get().getPages();
        assertTrue("pages must be present", pages.isPresent());
        assertEquals("must have 2 pages", 2, pages.get().size());

        /*-- page 1 --*/
        Optional<List<Action>> page1 = pages.get().get(0).getActions();
        assertTrue("actions must be present", page1.isPresent());
        assertEquals("must have 2 action entries", 2, page1.get().size());

        Action action1 = page1.get().get(0);
        Action action2 = page1.get().get(1);

        assertEquals(ActionType.USERNAME, action1.getType());
        assertEquals("#example_login_userid", action1.getSelector().get());
        assertEquals("user2", action1.getValue().get());
        assertEquals("This is an example description", action1.getDescription().get());

        assertEquals(ActionType.CLICK, action2.getType());
        assertEquals("#next_button", action2.getSelector().get());
        assertEquals("Click the next button to go to the password field", action2.getDescription().get());

        /*-- page 2 --*/
        Optional<List<Action>> page2 = pages.get().get(1).getActions();
        assertTrue("actions must be present", page2.isPresent());
        assertEquals("must have 4 action entries", 4, page2.get().size());

        Action action3 = page2.get().get(0);
        Action action4 = page2.get().get(1);
        Action action5 = page2.get().get(2);
        Action action6 = page2.get().get(3);

        assertEquals(ActionType.WAIT, action3.getType());
        assertEquals("3200", action3.getValue().get());
        assertEquals(SecHubTimeUnit.MILLISECOND, action3.getUnit().get());

        assertEquals(ActionType.INPUT, action4.getType());
        assertEquals("#email_field", action4.getSelector().get());
        assertEquals("user@example.org", action4.getValue().get());
        assertEquals("The user's email address.", action4.getDescription().get());

        assertEquals(ActionType.PASSWORD, action5.getType());
        assertEquals("#example_login_pwd", action5.getSelector().get());
        assertEquals("pwd2", action5.getValue().get());

        assertEquals(ActionType.CLICK, action6.getType());
        assertEquals("#example_login_login_button", action6.getSelector().get());

    }

    @Test
    public void sechub_config0_json_file_from_json_has_no_webconfig_or_infraconfig_but_api_version_1() throws Exception {
        /* prepare */
        String json = TestSharedKernelFileSupport.getTestfileSupport().loadTestFile("sechub_config0.json");

        /* execute */
        SecHubConfiguration result = SECHUB_CONFIG.fromJSON(json);

        /* test */
        assertFalse("webscan config must NOT be present", result.getWebScan().isPresent());
        assertFalse("infracan config must NOT be present", result.getInfraScan().isPresent());
        assertEquals("1.0", result.getApiVersion());
    }

    @Test
    public void sechub_config1_json_file_from_json_has_webconfig_with_url() throws Exception {
        /* prepare */
        String json = TestSharedKernelFileSupport.getTestfileSupport().loadTestFile("sechub_config1.json");

        /* execute */
        SecHubConfiguration result = SECHUB_CONFIG.fromJSON(json);

        /* test */
        assertTrue("webscan config must be present", result.getWebScan().isPresent());
        assertEquals(result.getWebScan().get().getUrl(), new URI("https://fscan.intranet.example.org/"));
    }

    @Test
    public void sechub_config2_json_file_from_json_has_infraconfig_with_url() throws Exception {
        /* prepare */
        String json = TestSharedKernelFileSupport.getTestfileSupport().loadTestFile("sechub_config2.json");

        /* execute */
        SecHubConfiguration result = SECHUB_CONFIG.fromJSON(json);

        /* test */
        assertTrue("infrascan config must be present", result.getInfraScan().isPresent());
        assertTrue(result.getInfraScan().get().getUris().contains(new URI("https://fscan.intranet.example.org/")));
    }

    @Test
    public void sechub_config2_json_file_from_json_has_infraconfig_with_ips() throws Exception {
        /* prepare */
        String json = TestSharedKernelFileSupport.getTestfileSupport().loadTestFile("sechub_config2.json");

        /* execute */
        SecHubConfiguration result = SECHUB_CONFIG.fromJSON(json);

        /* test */
        assertTrue("infrascan config must be present", result.getInfraScan().isPresent());
        List<InetAddress> ips = result.getInfraScan().get().getIps();
        assertTrue(ips.contains(InetAddress.getByName("192.168.1.1")));
        assertTrue(ips.contains(InetAddress.getByName("58.112.44.32")));
    }

    @Test
    public void sechub_config2_json_file_from_json_has_no_codescanconfig() throws Exception {
        /* prepare */
        String json = TestSharedKernelFileSupport.getTestfileSupport().loadTestFile("sechub_config2.json");

        /* execute */
        SecHubConfiguration result = SECHUB_CONFIG.fromJSON(json);

        /* test */
        assertFalse("codescan config must NOT be present", result.getCodeScan().isPresent());

    }

    @Test
    public void sechub_config4_json_file_from_json_has_codescanconfig_with_folders() throws Exception {
        /* prepare */
        String json = TestSharedKernelFileSupport.getTestfileSupport().loadTestFile("sechub_config4.json");

        /* execute */
        SecHubConfiguration result = SECHUB_CONFIG.fromJSON(json);

        /* test */
        assertTrue("codescan config must be present", result.getCodeScan().isPresent());
        List<String> ips = result.getCodeScan().get().getFileSystem().get().getFolders();
        assertTrue(ips.contains("src/main/java"));
        assertTrue(ips.contains("src/main/resources"));
    }

    @Test
    public void new_instance_returns_not_null_for_asJSON() throws Exception {
        assertNotNull(configurationToTest.toJSON());
    }

    @Test
    public void new_instance_returns_null_for_getApiVersion() {
        assertNull(configurationToTest.getApiVersion());
    }

    @Test
    public void uses_json_converter_when_toJSON_is_called() throws Exception {
        /* prepare */
        JSONConverter mockedConverter = mock(JSONConverter.class);

        // - integrate mocked converter not possible otherwise
        SecHubConfiguration specialConfigurationToTest = new SecHubConfiguration() {
            @Override
            public JSONConverter getConverter() {
                return mockedConverter;
            }
        };
        when(mockedConverter.toJSON(specialConfigurationToTest)).thenReturn("mockedJSONResult");

        /* execute */
        String result = specialConfigurationToTest.toJSON();

        /* test */
        verify(mockedConverter).toJSON(specialConfigurationToTest);
        assertEquals("mockedJSONResult", result);
    }

    @Test
    public void configuration_setter_getter_testing() throws Exception {

        PojoTester.testSetterAndGetter(new SecHubConfiguration());

    }

    @Test
    public void when_webscan_set_its_present() {
        /* prepare */
        configurationToTest.setWebScan(mock(SecHubWebScanConfiguration.class));

        /* test */
        assertTrue(configurationToTest.getWebScan().isPresent());
    }

    @Test
    public void when_infracan_set_its_present() {
        /* prepare */
        configurationToTest.setInfraScan(mock(SecHubInfrastructureScanConfiguration.class));

        /* test */
        assertTrue(configurationToTest.getInfraScan().isPresent());
    }

    @Test
    public void webscan_max_scan_duration_wrong_unit_results_in_null() {
        /* prepare */
        String json = TestSharedKernelFileSupport.getTestfileSupport().loadTestFile("webscan/webscan_max_scan_duration_wrong_unit.json");

        /* execute */
        SecHubConfiguration result = SECHUB_CONFIG.fromJSON(json);

        /* test */
        /*
         * custom JSON mapper will not throw an exception, but will set wrong values to
         * null
         */
        assertNotNull(result);
        assertTrue(result.getWebScan().isPresent());
        SecHubWebScanConfiguration webscan = result.getWebScan().get();
        assertTrue(webscan.getMaxScanDuration().isPresent());
        assertEquals(1, webscan.getMaxScanDuration().get().getDuration());
        assertNull(webscan.getMaxScanDuration().get().getUnit());
    }

    @Test
    public void webscan_empty_includes_excludes() {
        /* prepare */
        String json = TestSharedKernelFileSupport.getTestfileSupport().loadTestFile("webscan/webscan_empty_includes_excludes.json");

        /* execute */
        SecHubConfiguration result = SECHUB_CONFIG.fromJSON(json);

        /* test */
        Optional<SecHubWebScanConfiguration> webScanOption = result.getWebScan();
        assertTrue("webscan config must be present", webScanOption.isPresent());

        SecHubWebScanConfiguration secHubWebScanConfiguration = webScanOption.get();
        assertEquals(URI.create("https://productfailure.demo.example.org"), secHubWebScanConfiguration.getUrl());

        Optional<List<String>> includes = secHubWebScanConfiguration.getIncludes();
        assertTrue("includes must be present", includes.isPresent());
        List<String> expectedIncludes = new LinkedList<>();
        assertTrue("includes are empty", includes.get().isEmpty());
        assertEquals(expectedIncludes, includes.get());

        Optional<List<String>> excludes = secHubWebScanConfiguration.getExcludes();
        assertTrue("excludes must be present", excludes.isPresent());
        List<String> expectedExcludes = new LinkedList<>();
        assertTrue("excludes are empty", excludes.get().isEmpty());
        assertEquals(expectedExcludes, excludes.get());
    }

    @Test
    public void a_sechub_configuration_JSON_with_license_scan_can_be_read_and_license_scan_has_correct_data_configuration_reference() {
        /* prepare */
        String expectedDataConfigName = "build-artifacts";
        String json = TestSharedKernelFileSupport.getTestfileSupport().loadTestFile("licensescan/license_scan.json");

        /* execute */
        SecHubConfiguration result = SECHUB_CONFIG.fromJSON(json);

        /* test */
        Optional<SecHubLicenseScanConfiguration> licenseScan = result.getLicenseScan();
        assertTrue("license scan must be present", licenseScan.isPresent());

        Set<String> usedDataConfigs = licenseScan.get().getNamesOfUsedDataConfigurationObjects();
        assertEquals(1, usedDataConfigs.size());
        assertEquals(expectedDataConfigName, usedDataConfigs.iterator().next());
    }

    @Test
    public void a_sechub_configuration_JSON_with_secret_scan_can_be_read_and_secret_scan_has_correct_data_configuration_reference() {
        /* prepare */
        String expectedDataConfigName = "files";
        String json = TestSharedKernelFileSupport.getTestfileSupport().loadTestFile("secretscan/secret_scan.json");

        /* execute */
        SecHubConfiguration result = SECHUB_CONFIG.fromJSON(json);

        /* test */
        Optional<SecHubSecretScanConfiguration> secretScan = result.getSecretScan();
        assertTrue("secret scan must be present", secretScan.isPresent());

        Set<String> usedDataConfigs = secretScan.get().getNamesOfUsedDataConfigurationObjects();
        assertEquals(1, usedDataConfigs.size());
        assertEquals(expectedDataConfigName, usedDataConfigs.iterator().next());
    }

    @Test
    public void a_sechub_configuration_JSON_with_data_section_containing_unknown_excludes_can_be_read() {
        /* prepare */
        String json = TestSharedKernelFileSupport.getTestfileSupport().loadTestFile("codescan/code_scan-with-datasections-and-unknown-excludes.json");

        /* execute */
        SecHubConfiguration result = SECHUB_CONFIG.fromJSON(json);

        /* test */
        assertNotNull(result);
        assertEquals("1.2.3", result.getApiVersion());
    }

    @Test
    public void a_sechub_configuration_JSON_with_combined_unknown_properties_can_be_read() {
        /* prepare */
        String json = TestSharedKernelFileSupport.getTestfileSupport().loadTestFile("combined_config_with_unknown_parts_everywhere.json");

        /* execute */
        SecHubConfiguration result = SECHUB_CONFIG.fromJSON(json);

        /* test */
        assertNotNull(result);
        assertEquals("2.1.0", result.getApiVersion());
    }
}
