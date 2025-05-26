// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.net.InetAddress;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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

class SecHubConfigurationTest {

    private SecHubConfiguration configurationToTest;
    private static final SecHubConfiguration SECHUB_CONFIG = new SecHubConfiguration();

    @BeforeEach
    void before() {
        configurationToTest = new SecHubConfiguration();
    }

    @Test
    void webscan_login_basic_json_has_webconfig_as_expected() throws Exception {
        /* prepare */
        String json = TestSharedKernelFileSupport.getTestfileSupport().loadTestFile("webscan/webscan_login_basic.json");

        /* execute */
        SecHubConfiguration result = SECHUB_CONFIG.fromJSON(json);

        /* test */
        Optional<SecHubWebScanConfiguration> webScanOption = result.getWebScan();
        assertThat(webScanOption).isPresent();

        SecHubWebScanConfiguration secHubWebScanConfiguration = webScanOption.get();
        Optional<WebLoginConfiguration> loginOption = secHubWebScanConfiguration.getLogin();
        assertThat(loginOption).isPresent();
        WebLoginConfiguration loginConfiguration = loginOption.get();
        assertThat(loginConfiguration.getUrl()).isEqualTo(new URL("https://productfailure.demo.example.org/login"));

        /*-- basic --*/
        Optional<BasicLoginConfiguration> basic = loginConfiguration.getBasic();
        assertThat(basic).as("basic login config must be present").isPresent().hasValueSatisfying(b -> {
            assertThat(b.getRealm()).hasValue("realm0");
            assertThat(new String(b.getUser())).isEqualTo("user0");
            assertThat(new String(b.getPassword())).isEqualTo("pwd0");
        });

        Optional<FormLoginConfiguration> form = loginConfiguration.getForm();
        assertThat(form).as("form login config must NOT be present").isNotPresent();
    }

    @Test
    void webscan_login_form_script_json_has_webconfig_as_expected() throws Exception {
        /* prepare */
        String json = TestSharedKernelFileSupport.getTestfileSupport().loadTestFile("webscan/webscan_login_form_script.json");

        /* execute */
        SecHubConfiguration result = SECHUB_CONFIG.fromJSON(json);

        /* test */
        Optional<SecHubWebScanConfiguration> webScanOption = result.getWebScan();
        assertThat(webScanOption).isPresent();

        SecHubWebScanConfiguration secHubWebScanConfiguration = webScanOption.get();
        Optional<WebLoginConfiguration> loginOption = secHubWebScanConfiguration.getLogin();
        assertThat(loginOption).isPresent();
        WebLoginConfiguration loginConfiguration = loginOption.get();
        assertThat(loginConfiguration.getUrl()).isEqualTo(new URL("https://productfailure.demo.example.org/login"));

        Optional<BasicLoginConfiguration> basic = loginConfiguration.getBasic();
        assertThat(basic).isNotPresent();

        /*-- form --*/
        Optional<FormLoginConfiguration> form = loginConfiguration.getForm();
        assertThat(form).isPresent();

        /*-- form: script --*/
        Optional<Script> script = form.get().getScript();
        assertThat(script).isPresent();

        Optional<List<Page>> pages = script.get().getPages();
        assertThat(pages).isPresent();
        assertThat(pages.get()).hasSize(1);

        /*-- page 1 --*/
        Optional<List<Action>> page1 = pages.get().get(0).getActions();
        assertThat(page1).isPresent();
        assertThat(page1.get()).hasSize(3);

        Action action1 = page1.get().get(0);
        Action action2 = page1.get().get(1);
        Action action3 = page1.get().get(2);

        assertThat(action1.getType()).isEqualTo(ActionType.USERNAME);
        assertThat(action1.getSelector()).hasValue("#example_login_userid");
        assertThat(action1.getValue()).hasValue("user2");

        assertThat(action2.getType()).isEqualTo(ActionType.PASSWORD);
        assertThat(action2.getSelector()).hasValue("#example_login_pwd");
        assertThat(action2.getValue()).hasValue("pwd2");

        assertThat(action3.getType()).isEqualTo(ActionType.CLICK);
        assertThat(action3.getSelector()).hasValue("#example_login_login_button");
    }

    @Test
    void webscan_login_form_script_with_descriptions_json_has_webconfig_as_expected() throws Exception {
        /* prepare */
        String json = TestSharedKernelFileSupport.getTestfileSupport().loadTestFile("webscan/webscan_login_form_script_with_descriptions.json");

        /* execute */
        SecHubConfiguration result = SECHUB_CONFIG.fromJSON(json);

        /* test */
        Optional<SecHubWebScanConfiguration> webScanOption = result.getWebScan();
        assertThat(webScanOption).isPresent();

        SecHubWebScanConfiguration secHubWebScanConfiguration = webScanOption.get();
        Optional<WebLoginConfiguration> loginOption = secHubWebScanConfiguration.getLogin();
        assertThat(loginOption).isPresent();
        WebLoginConfiguration loginConfiguration = loginOption.get();
        assertThat(loginConfiguration.getUrl()).isEqualTo(new URL("https://productfailure.demo.example.org/login"));

        Optional<BasicLoginConfiguration> basic = loginConfiguration.getBasic();
        assertThat(basic).isNotPresent();

        /*-- form --*/
        Optional<FormLoginConfiguration> form = loginConfiguration.getForm();
        assertThat(form).isPresent();

        /*-- form: script --*/
        Optional<Script> script = form.get().getScript();
        assertThat(script).isPresent();

        Optional<List<Page>> pages = script.get().getPages();
        assertThat(pages).isPresent();
        assertThat(pages.get()).hasSize(1);

        /*-- page 1 --*/
        Optional<List<Action>> page1 = pages.get().get(0).getActions();
        assertThat(page1).isPresent();
        assertThat(page1.get()).hasSize(4);

        Action action1 = page1.get().get(0);
        Action action2 = page1.get().get(1);
        Action action3 = page1.get().get(2);
        Action action4 = page1.get().get(3);

        assertThat(action1.getType()).isEqualTo(ActionType.USERNAME);
        assertThat(action1.getSelector()).hasValue("#example_login_userid");
        assertThat(action1.getValue()).hasValue("user2");
        assertThat(action1.getDescription()).hasValue("The username is different from the email address");

        assertThat(action2.getType()).isEqualTo(ActionType.INPUT);
        assertThat(action2.getSelector()).hasValue("#example_login_email");
        assertThat(action2.getValue()).hasValue("user2@example.com");
        assertThat(action2.getDescription()).hasValue("The website has a separate field for the email address");

        assertThat(action3.getType()).isEqualTo(ActionType.PASSWORD);
        assertThat(action3.getSelector()).hasValue("#example_login_pwd");
        assertThat(action3.getValue()).hasValue("pwd2");

        assertThat(action4.getType()).isEqualTo(ActionType.CLICK);
        assertThat(action4.getSelector()).hasValue("#example_login_login_button");
    }

    @Test
    void webscan_login_form_script_with_wait_json_has_webconfig_as_expected() throws Exception {
        /* prepare */
        String json = TestSharedKernelFileSupport.getTestfileSupport().loadTestFile("webscan/webscan_login_form_script_with_wait.json");

        /* execute */
        SecHubConfiguration result = SECHUB_CONFIG.fromJSON(json);

        /* test */
        Optional<SecHubWebScanConfiguration> webScanOption = result.getWebScan();
        assertThat(webScanOption).isPresent();

        SecHubWebScanConfiguration secHubWebScanConfiguration = webScanOption.get();
        Optional<WebLoginConfiguration> loginOption = secHubWebScanConfiguration.getLogin();
        assertThat(loginOption).isPresent();
        WebLoginConfiguration loginConfiguration = loginOption.get();
        assertThat(loginConfiguration.getUrl()).isEqualTo(new URL("https://productfailure.demo.example.org/login"));

        Optional<BasicLoginConfiguration> basic = loginConfiguration.getBasic();
        assertThat(basic).isNotPresent();

        /*-- form --*/
        Optional<FormLoginConfiguration> form = loginConfiguration.getForm();
        assertThat(form).isPresent();

        /*-- form : script --*/
        Optional<Script> script = form.get().getScript();
        assertThat(script).isPresent();

        Optional<List<Page>> pages = script.get().getPages();
        assertThat(pages).isPresent();
        assertThat(pages.get()).hasSize(1);

        /*-- page 1 --*/
        Optional<List<Action>> page1 = pages.get().get(0).getActions();
        assertThat(page1).isPresent();
        assertThat(page1.get()).hasSize(4);

        Action action1 = page1.get().get(0);
        Action action2 = page1.get().get(1);
        Action action3 = page1.get().get(2);
        Action action4 = page1.get().get(3);

        assertThat(action1.getType()).isEqualTo(ActionType.INPUT);
        assertThat(action1.getSelector()).hasValue("#example_login_userid");
        assertThat(action1.getValue()).hasValue("user2");

        assertThat(action2.getType()).isEqualTo(ActionType.WAIT);
        assertThat(action2.getValue()).hasValue("1458");
        assertThat(action2.getUnit()).hasValue(SecHubTimeUnit.MILLISECOND);

        assertThat(action3.getType()).isEqualTo(ActionType.INPUT);
        assertThat(action3.getSelector()).hasValue("#example_login_pwd");
        assertThat(action3.getValue()).hasValue("pwd2");

        assertThat(action4.getType()).isEqualTo(ActionType.CLICK);
        assertThat(action4.getSelector()).hasValue("#example_login_login_button");
    }

    @Test
    void webscan_alloptions_json_has_webconfig_with_all_examples() throws Exception {
        /* prepare */
        String json = TestSharedKernelFileSupport.getTestfileSupport().loadTestFile("webscan/webscan_alloptions.json");

        /* execute */
        SecHubConfiguration result = SECHUB_CONFIG.fromJSON(json);

        /* test */
        Optional<SecHubWebScanConfiguration> webScanOption = result.getWebScan();
        assertThat(webScanOption).isPresent();

        SecHubWebScanConfiguration secHubWebScanConfiguration = webScanOption.get();
        assertThat(secHubWebScanConfiguration.getUrl()).isEqualTo(URI.create("https://productfailure.demo.example.org"));

        Optional<List<String>> includes = secHubWebScanConfiguration.getIncludes();
        assertThat(includes).isPresent();
        List<String> expectedIncludes = Arrays.asList("/portal/admin", "/abc.html", "/hidden");
        assertThat(includes.get()).isEqualTo(expectedIncludes);

        Optional<List<String>> excludes = secHubWebScanConfiguration.getExcludes();
        assertThat(excludes).isPresent();
        List<String> expectedExcludes = Arrays.asList("/public/media", "/contact.html", "/static");
        assertThat(excludes.get()).isEqualTo(expectedExcludes);

        Optional<WebScanDurationConfiguration> maxScanDuration = secHubWebScanConfiguration.getMaxScanDuration();
        assertThat(maxScanDuration).isPresent();
        assertThat(maxScanDuration.get().getDuration()).isEqualTo(2);
        assertThat(maxScanDuration.get().getUnit()).isEqualTo(SecHubTimeUnit.HOUR);

        Optional<WebLoginConfiguration> loginOption = secHubWebScanConfiguration.getLogin();
        assertThat(loginOption).isPresent();
        WebLoginConfiguration loginConfiguration = loginOption.get();
        assertThat(loginConfiguration.getUrl()).isEqualTo(new URL("https://productfailure.demo.example.org/login"));

        /*-- basic --*/
        Optional<BasicLoginConfiguration> basic = loginConfiguration.getBasic();
        assertThat(basic).isPresent();
        assertThat(basic.get().getRealm()).hasValue("realm0");
        assertThat(new String(basic.get().getUser())).isEqualTo("user0");
        assertThat(new String(basic.get().getPassword())).isEqualTo("pwd0");

        /*-- form --*/
        Optional<FormLoginConfiguration> form = loginConfiguration.getForm();
        assertThat(form).isPresent();

        /*-- form : script --*/
        Optional<Script> script = form.get().getScript();
        assertThat(script).isPresent();

        Optional<List<Page>> pages = script.get().getPages();
        assertThat(pages).isPresent();
        assertThat(pages.get()).hasSize(2);

        /*-- page 1 --*/
        Optional<List<Action>> page1 = pages.get().get(0).getActions();
        assertThat(page1).isPresent();
        assertThat(page1.get()).hasSize(2);

        Action action1 = page1.get().get(0);
        Action action2 = page1.get().get(1);

        assertThat(action1.getType()).isEqualTo(ActionType.USERNAME);
        assertThat(action1.getSelector()).hasValue("#example_login_userid");
        assertThat(action1.getValue()).hasValue("user2");
        assertThat(action1.getDescription()).hasValue("This is an example description");

        assertThat(action2.getType()).isEqualTo(ActionType.CLICK);
        assertThat(action2.getSelector()).hasValue("#next_button");
        assertThat(action2.getDescription()).hasValue("Click the next button to go to the password field");

        /*-- page 2 --*/
        Optional<List<Action>> page2 = pages.get().get(1).getActions();
        assertThat(page2).isPresent();
        assertThat(page2.get()).hasSize(4);

        Action action3 = page2.get().get(0);
        Action action4 = page2.get().get(1);
        Action action5 = page2.get().get(2);
        Action action6 = page2.get().get(3);

        assertThat(action3.getType()).isEqualTo(ActionType.WAIT);
        assertThat(action3.getValue()).hasValue("3200");
        assertThat(action3.getUnit()).hasValue(SecHubTimeUnit.MILLISECOND);

        assertThat(action4.getType()).isEqualTo(ActionType.INPUT);
        assertThat(action4.getSelector()).hasValue("#email_field");
        assertThat(action4.getValue()).hasValue("user@example.org");
        assertThat(action4.getDescription()).hasValue("The user's email address.");

        assertThat(action5.getType()).isEqualTo(ActionType.PASSWORD);
        assertThat(action5.getSelector()).hasValue("#example_login_pwd");
        assertThat(action5.getValue()).hasValue("pwd2");

        assertThat(action6.getType()).isEqualTo(ActionType.CLICK);
        assertThat(action6.getSelector()).hasValue("#example_login_login_button");
    }

    @Test
    void sechub_config0_json_file_from_json_has_no_webconfig_or_infraconfig_but_api_version_1() throws Exception {
        /* prepare */
        String json = TestSharedKernelFileSupport.getTestfileSupport().loadTestFile("sechub_config0.json");

        /* execute */
        SecHubConfiguration result = SECHUB_CONFIG.fromJSON(json);

        /* test */
        assertThat(result.getWebScan()).isNotPresent();
        assertThat(result.getInfraScan()).isNotPresent();
        assertThat(result.getApiVersion()).isEqualTo("1.0");
    }

    @Test
    void sechub_config1_json_file_from_json_has_webconfig_with_url() throws Exception {
        /* prepare */
        String json = TestSharedKernelFileSupport.getTestfileSupport().loadTestFile("sechub_config1.json");

        /* execute */
        SecHubConfiguration result = SECHUB_CONFIG.fromJSON(json);

        /* test */
        assertThat(result.getWebScan()).isPresent();
        assertThat(result.getWebScan().get().getUrl()).isEqualTo(new URI("https://fscan.intranet.example.org/"));
    }

    @Test
    void sechub_config2_json_file_from_json_has_infraconfig_with_url() throws Exception {
        /* prepare */
        String json = TestSharedKernelFileSupport.getTestfileSupport().loadTestFile("sechub_config2.json");

        /* execute */
        SecHubConfiguration result = SECHUB_CONFIG.fromJSON(json);

        /* test */
        assertThat(result.getInfraScan()).isPresent();
        assertThat(result.getInfraScan().get().getUris()).contains(new URI("https://fscan.intranet.example.org/"));
    }

    @Test
    void sechub_config2_json_file_from_json_has_infraconfig_with_ips() throws Exception {
        /* prepare */
        String json = TestSharedKernelFileSupport.getTestfileSupport().loadTestFile("sechub_config2.json");

        /* execute */
        SecHubConfiguration result = SECHUB_CONFIG.fromJSON(json);

        /* test */
        assertThat(result.getInfraScan()).isPresent();
        List<InetAddress> ips = result.getInfraScan().get().getIps();
        assertThat(ips).contains(InetAddress.getByName("192.168.1.1"), InetAddress.getByName("58.112.44.32"));
    }

    @Test
    void sechub_config2_json_file_from_json_has_no_codescanconfig() throws Exception {
        /* prepare */
        String json = TestSharedKernelFileSupport.getTestfileSupport().loadTestFile("sechub_config2.json");

        /* execute */
        SecHubConfiguration result = SECHUB_CONFIG.fromJSON(json);

        /* test */
        assertThat(result.getCodeScan()).isNotPresent();
    }

    @Test
    void sechub_config4_json_file_from_json_has_codescanconfig_with_folders() {
        /* prepare */
        String json = TestSharedKernelFileSupport.getTestfileSupport().loadTestFile("sechub_config4.json");

        /* execute */
        SecHubConfiguration result = SECHUB_CONFIG.fromJSON(json);

        /* test */
        assertThat(result.getCodeScan()).isPresent();
        List<String> folders = result.getCodeScan().get().getFileSystem().get().getFolders();
        assertThat(folders).contains("src/main/java", "src/main/resources");
    }

    @Test
    void new_instance_returns_not_null_for_asJSON() {
        assertThat(configurationToTest.toJSON()).isNotNull();
    }

    @Test
    void new_instance_returns_null_for_getApiVersion() {
        assertThat(configurationToTest.getApiVersion()).isNull();
    }

    @Test
    void uses_json_converter_when_toJSON_is_called() {
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
        assertThat(result).isEqualTo("mockedJSONResult");
    }

    @Test
    void configuration_setter_getter_testing() {
        PojoTester.testSetterAndGetter(new SecHubConfiguration());
    }

    @Test
    void when_webscan_set_its_present() {
        /* prepare */
        configurationToTest.setWebScan(mock(SecHubWebScanConfiguration.class));

        /* test */
        assertThat(configurationToTest.getWebScan()).isPresent();
    }

    @Test
    void when_infracan_set_its_present() {
        /* prepare */
        configurationToTest.setInfraScan(mock(SecHubInfrastructureScanConfiguration.class));

        /* test */
        assertThat(configurationToTest.getInfraScan()).isPresent();
    }

    @Test
    void webscan_max_scan_duration_wrong_unit_results_in_null() {
        /* prepare */
        String json = TestSharedKernelFileSupport.getTestfileSupport().loadTestFile("webscan/webscan_max_scan_duration_wrong_unit.json");

        /* execute */
        SecHubConfiguration result = SECHUB_CONFIG.fromJSON(json);

        /* test */
        /*
         * custom JSON mapper will not throw an exception, but will set wrong values to
         * null
         */
        assertThat(result).isNotNull();
        assertThat(result.getWebScan()).isPresent();
        SecHubWebScanConfiguration webscan = result.getWebScan().get();
        assertThat(webscan.getMaxScanDuration()).isPresent();
        assertThat(webscan.getMaxScanDuration().get().getDuration()).isEqualTo(1);
        assertThat(webscan.getMaxScanDuration().get().getUnit()).isNull();
    }

    @Test
    void webscan_empty_includes_excludes() {
        /* prepare */
        String json = TestSharedKernelFileSupport.getTestfileSupport().loadTestFile("webscan/webscan_empty_includes_excludes.json");

        /* execute */
        SecHubConfiguration result = SECHUB_CONFIG.fromJSON(json);

        /* test */
        Optional<SecHubWebScanConfiguration> webScanOption = result.getWebScan();
        assertThat(webScanOption).isPresent();

        SecHubWebScanConfiguration secHubWebScanConfiguration = webScanOption.get();
        assertThat(secHubWebScanConfiguration.getUrl()).isEqualTo(URI.create("https://productfailure.demo.example.org"));

        Optional<List<String>> includes = secHubWebScanConfiguration.getIncludes();
        assertThat(includes).isPresent();
        assertThat(includes.get()).isEmpty();

        Optional<List<String>> excludes = secHubWebScanConfiguration.getExcludes();
        assertThat(excludes).isPresent();
        assertThat(excludes.get()).isEmpty();
    }

    @Test
    void a_sechub_configuration_JSON_with_license_scan_can_be_read_and_license_scan_has_correct_data_configuration_reference() {
        /* prepare */
        String expectedDataConfigName = "build-artifacts";
        String json = TestSharedKernelFileSupport.getTestfileSupport().loadTestFile("licensescan/license_scan.json");

        /* execute */
        SecHubConfiguration result = SECHUB_CONFIG.fromJSON(json);

        /* test */
        Optional<SecHubLicenseScanConfiguration> licenseScan = result.getLicenseScan();
        assertThat(licenseScan).isPresent();

        Set<String> usedDataConfigs = licenseScan.get().getNamesOfUsedDataConfigurationObjects();
        assertThat(usedDataConfigs).hasSize(1);
        assertThat(usedDataConfigs.iterator().next()).isEqualTo(expectedDataConfigName);
    }

    @Test
    void a_sechub_configuration_JSON_with_secret_scan_can_be_read_and_secret_scan_has_correct_data_configuration_reference() {
        /* prepare */
        String expectedDataConfigName = "files";
        String json = TestSharedKernelFileSupport.getTestfileSupport().loadTestFile("secretscan/secret_scan.json");

        /* execute */
        SecHubConfiguration result = SECHUB_CONFIG.fromJSON(json);

        /* test */
        Optional<SecHubSecretScanConfiguration> secretScan = result.getSecretScan();
        assertThat(secretScan).isPresent();

        Set<String> usedDataConfigs = secretScan.get().getNamesOfUsedDataConfigurationObjects();
        assertThat(usedDataConfigs).hasSize(1);
        assertThat(usedDataConfigs.iterator().next()).isEqualTo(expectedDataConfigName);
    }

    @Test
    void a_sechub_configuration_JSON_with_data_section_containing_unknown_excludes_can_be_read() {
        /* prepare */
        String json = TestSharedKernelFileSupport.getTestfileSupport().loadTestFile("codescan/code_scan-with-datasections-and-unknown-excludes.json");

        /* execute */
        SecHubConfiguration result = SECHUB_CONFIG.fromJSON(json);

        /* test */
        assertThat(result).isNotNull();
        assertThat(result.getApiVersion()).isEqualTo("1.2.3");
    }

    @Test
    void a_sechub_configuration_JSON_with_combined_unknown_properties_can_be_read() {
        /* prepare */
        String json = TestSharedKernelFileSupport.getTestfileSupport().loadTestFile("combined_config_with_unknown_parts_everywhere.json");

        /* execute */
        SecHubConfiguration result = SECHUB_CONFIG.fromJSON(json);

        /* test */
        assertThat(result).isNotNull();
        assertThat(result.getApiVersion()).isEqualTo("2.1.0");
    }
}
