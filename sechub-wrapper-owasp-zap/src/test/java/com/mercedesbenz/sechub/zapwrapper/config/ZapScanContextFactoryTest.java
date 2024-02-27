// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.zapwrapper.config;

import static com.mercedesbenz.sechub.zapwrapper.util.EnvironmentVariableConstants.PDS_JOB_EVENTS_FOLDER;
import static com.mercedesbenz.sechub.zapwrapper.util.EnvironmentVariableConstants.PDS_JOB_EXTRACTED_SOURCES_FOLDER;
import static com.mercedesbenz.sechub.zapwrapper.util.EnvironmentVariableConstants.PDS_JOB_USER_MESSAGES_FOLDER;
import static com.mercedesbenz.sechub.zapwrapper.util.EnvironmentVariableConstants.PROXY_HOST_ENV_VARIABLE_NAME;
import static com.mercedesbenz.sechub.zapwrapper.util.EnvironmentVariableConstants.PROXY_PORT_ENV_VARIABLE_NAME;
import static com.mercedesbenz.sechub.zapwrapper.util.EnvironmentVariableConstants.ZAP_API_KEY_ENV_VARIABLE_NAME;
import static com.mercedesbenz.sechub.zapwrapper.util.EnvironmentVariableConstants.ZAP_DEACTIVATED_RULE_REFERENCES;
import static com.mercedesbenz.sechub.zapwrapper.util.EnvironmentVariableConstants.ZAP_HOST_ENV_VARIABLE_NAME;
import static com.mercedesbenz.sechub.zapwrapper.util.EnvironmentVariableConstants.ZAP_PORT_ENV_VARIABLE_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.net.URI;
import java.nio.file.Path;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import com.mercedesbenz.sechub.zapwrapper.cli.CommandLineSettings;
import com.mercedesbenz.sechub.zapwrapper.cli.ZapWrapperRuntimeException;
import com.mercedesbenz.sechub.zapwrapper.config.auth.AuthenticationType;
import com.mercedesbenz.sechub.zapwrapper.config.data.DeactivatedRuleReferences;
import com.mercedesbenz.sechub.zapwrapper.config.data.ZapFullRuleset;
import com.mercedesbenz.sechub.zapwrapper.helper.SecHubWebScanConfigurationHelper;
import com.mercedesbenz.sechub.zapwrapper.util.EnvironmentVariableReader;

class ZapScanContextFactoryTest {

    private ZapScanContextFactory factoryToTest;

    private SecHubWebScanConfigurationHelper sechubWebConfigHelper;
    private EnvironmentVariableReader environmentVariableReader;

    private RuleProvider ruleProvider;

    private File fullRulesetFile;
    private File deactivationFile;

    @TempDir
    private File tempDir;

    @BeforeEach
    void beforeEach() {

        // create object to test
        factoryToTest = new ZapScanContextFactory();

        // create mocks
        sechubWebConfigHelper = mock(SecHubWebScanConfigurationHelper.class);
        environmentVariableReader = mock(EnvironmentVariableReader.class);
        ruleProvider = mock(RuleProvider.class);

        // connect mocks with test object
        factoryToTest.sechubWebConfigHelper = sechubWebConfigHelper;
        factoryToTest.environmentVariableReader = environmentVariableReader;
        factoryToTest.ruleProvider = ruleProvider;

        // create test data
        fullRulesetFile = new File("src/test/resources/zap-available-rules/zap-full-ruleset.json");
        deactivationFile = new File("src/test/resources/wrapper-deactivated-rule-examples/zap-rules-to-deactivate.json");

        when(environmentVariableReader.readAsString(PDS_JOB_USER_MESSAGES_FOLDER)).thenReturn(tempDir.getAbsolutePath());
        when(environmentVariableReader.readAsString(PDS_JOB_EVENTS_FOLDER)).thenReturn("");
    }

    @Test
    void commandLineSettings_object_is_null_results_in_mustexitruntimeexception() {
        /* execute + test */
        assertThrows(ZapWrapperRuntimeException.class, () -> factoryToTest.create(null));
    }

    @Test
    void created_configuration_has_max_scan_duration_from_sechub_webconfig() {
        /* prepare */
        CommandLineSettings settings = createSettingsMockWithNecessaryParts();
        long maxScanDuration = 4711L;
        when(sechubWebConfigHelper.fetchMaxScanDurationInMillis(any())).thenReturn(maxScanDuration);
        when(ruleProvider.fetchDeactivatedRuleReferences(any())).thenReturn(new DeactivatedRuleReferences());

        /* execute */
        ZapScanContext result = factoryToTest.create(settings);

        /* test */
        assertEquals(result.getMaxScanDurationInMilliSeconds(), maxScanDuration);

    }

    @Test
    void context_name_is_used_from_settings_when_defined() {
        /* prepare */
        CommandLineSettings settings = createSettingsMockWithNecessaryParts();

        String jobUUID = "12345";
        when(settings.getJobUUID()).thenReturn(jobUUID);
        when(ruleProvider.fetchDeactivatedRuleReferences(any())).thenReturn(new DeactivatedRuleReferences());

        /* execute */
        ZapScanContext result = factoryToTest.create(settings);

        /* test */
        assertEquals(result.getContextName(), jobUUID);

    }

    @Test
    void context_name_is_created_as_UUID_when_not_defined() {
        /* prepare */
        CommandLineSettings settings = createSettingsMockWithNecessaryParts();
        when(settings.getJobUUID()).thenReturn(null);
        when(ruleProvider.fetchDeactivatedRuleReferences(any())).thenReturn(new DeactivatedRuleReferences());

        /* execute */
        ZapScanContext result = factoryToTest.create(settings);

        /* test */
        String contextName = result.getContextName();
        assertNotNull(contextName);
        UUID.fromString(contextName);// just check it is a UUID
    }

    @ParameterizedTest
    @CsvSource({ "https://zaproxy.example.com,8080,api-key,https://proxy.example.com,3333", "host,4711,secret,proxy,5312" })
    void result_contains_server_config_with_arguments_from_command_line_settings_no_env_variables(String host, int port, String apiKey, String proxy,
            int proxyPort) {
        /* prepare */
        CommandLineSettings settings = createSettingsMockWithNecessaryParts();
        when(settings.getZapHost()).thenReturn(host);
        when(settings.getZapPort()).thenReturn(port);
        when(settings.getZapApiKey()).thenReturn(apiKey);
        when(settings.getProxyHost()).thenReturn(proxy);
        when(settings.getProxyPort()).thenReturn(proxyPort);
        when(ruleProvider.fetchDeactivatedRuleReferences(any())).thenReturn(new DeactivatedRuleReferences());

        /* execute */
        ZapScanContext result = factoryToTest.create(settings);

        /* test */
        ZapServerConfiguration serverConfig = result.getServerConfig();
        assertNotNull(serverConfig);
        assertEquals(host, serverConfig.getZaproxyHost());
        assertEquals(port, serverConfig.getZaproxyPort());
        assertEquals(apiKey, serverConfig.getZaproxyApiKey());

        assertEquals(proxy, result.getProxyInformation().getHost());
        assertEquals(proxyPort, result.getProxyInformation().getPort());

        verify(environmentVariableReader, never()).readAsInt(ZAP_PORT_ENV_VARIABLE_NAME);
        verify(environmentVariableReader, never()).readAsInt(PROXY_PORT_ENV_VARIABLE_NAME);

        verify(environmentVariableReader, never()).readAsString(ZAP_HOST_ENV_VARIABLE_NAME);
        verify(environmentVariableReader, never()).readAsString(ZAP_API_KEY_ENV_VARIABLE_NAME);
        verify(environmentVariableReader, never()).readAsString(PROXY_HOST_ENV_VARIABLE_NAME);
    }

    @ParameterizedTest
    @CsvSource({ "https://zaproxy.example.com,8080,api-key,https://proxy.example.com,3333", "host,4711,secret,proxy,5312" })
    void result_contains_server_config_with_arguments_from_environment_when_command_line_settings_not_set(String host, int port, String apiKey, String proxy,
            int proxyPort) {
        /* prepare */
        CommandLineSettings settings = mock(CommandLineSettings.class);
        when(settings.getTargetURL()).thenReturn("https://www.targeturl.com");
        when(ruleProvider.fetchDeactivatedRuleReferences(any())).thenReturn(new DeactivatedRuleReferences());
        when(environmentVariableReader.readAsString(ZAP_HOST_ENV_VARIABLE_NAME)).thenReturn(host);
        when(environmentVariableReader.readAsString(ZAP_API_KEY_ENV_VARIABLE_NAME)).thenReturn(apiKey);
        when(environmentVariableReader.readAsInt(ZAP_PORT_ENV_VARIABLE_NAME)).thenReturn(port);

        when(environmentVariableReader.readAsString(PROXY_HOST_ENV_VARIABLE_NAME)).thenReturn(proxy);
        when(environmentVariableReader.readAsInt(PROXY_PORT_ENV_VARIABLE_NAME)).thenReturn(proxyPort);

        /* execute */
        ZapScanContext result = factoryToTest.create(settings);

        /* test */
        ZapServerConfiguration serverConfig = result.getServerConfig();
        assertNotNull(serverConfig);
        assertEquals(host, serverConfig.getZaproxyHost());
        assertEquals(port, serverConfig.getZaproxyPort());
        assertEquals(apiKey, serverConfig.getZaproxyApiKey());
        assertEquals(proxy, result.getProxyInformation().getHost());
        assertEquals(proxyPort, result.getProxyInformation().getPort());
    }

    @Test
    void proxy_set_or_not_is_valid_result_returned_contains_null_as_proxyinformation() {
        /* prepare */
        CommandLineSettings settings = createSettingsMockWithNecessaryParts();
        when(settings.getProxyHost()).thenReturn(null);
        when(settings.getProxyPort()).thenReturn(0);
        when(ruleProvider.fetchDeactivatedRuleReferences(any())).thenReturn(new DeactivatedRuleReferences());

        when(environmentVariableReader.readAsString(PROXY_HOST_ENV_VARIABLE_NAME)).thenReturn(null);
        when(environmentVariableReader.readAsInt(PROXY_PORT_ENV_VARIABLE_NAME)).thenReturn(0);

        /* execute */
        ZapScanContext result = factoryToTest.create(settings);

        /* test */
        assertNotNull(result);
        assertNull(result.getProxyInformation());
    }

    /* @formatter:off*/
	@ParameterizedTest
	@CsvSource({
		"host,4711,",
		"host,,secret",
		",4711,secret",
	})
	/* @formatter:on*/
    void fails_when_host_port_or_apikey_not_in_commandline_and_also_not_in_env(String host, Integer port, String apiKey) {
        /* prepare */
        CommandLineSettings settings = mock(CommandLineSettings.class);
        when(settings.getZapHost()).thenReturn(host);
        when(ruleProvider.fetchDeactivatedRuleReferences(any())).thenReturn(new DeactivatedRuleReferences());
        if (port != null) {
            // when not defined in CSV, null is used. The mock does use int primitive,
            // so we just do not set it.
            when(settings.getZapPort()).thenReturn(port);
        }
        when(settings.getZapApiKey()).thenReturn(apiKey);

        /* execute + test */
        assertThrows(ZapWrapperRuntimeException.class, () -> factoryToTest.create(settings));

    }

    @Test
    void authentication_type_from_config_is_in_result() {
        /* prepare */
        CommandLineSettings settings = createSettingsMockWithNecessaryParts();
        AuthenticationType type = AuthenticationType.FORM_BASED_AUTHENTICATION;
        when(sechubWebConfigHelper.determineAuthenticationType(any())).thenReturn(type);
        when(ruleProvider.fetchDeactivatedRuleReferences(any())).thenReturn(new DeactivatedRuleReferences());

        /* execute */
        ZapScanContext result = factoryToTest.create(settings);

        /* test */
        assertEquals(result.getAuthenticationType(), type);

    }

    @Test
    void targetURI_calculated_by_factory_is_in_result() {
        /* prepare */
        CommandLineSettings settings = mock(CommandLineSettings.class);
        when(settings.getZapHost()).thenReturn("https://zaphot.example.com");
        when(settings.getZapPort()).thenReturn(815);
        when(settings.getZapApiKey()).thenReturn("secret-key");

        String targetUri = "https://fromfactory.example.com";
        when(settings.getTargetURL()).thenReturn(targetUri);

        URI createdUri = URI.create("https://fromfactory.example.com");
        when(ruleProvider.fetchDeactivatedRuleReferences(any())).thenReturn(new DeactivatedRuleReferences());

        /* execute */
        ZapScanContext result = factoryToTest.create(settings);

        /* test */
        assertEquals(result.getTargetUrl().toString(), createdUri.toString());

    }

    @ParameterizedTest
    @CsvSource({ "true", "false" })
    void verbose_from_settings_is_in_result(boolean verboseEnabled) {
        /* prepare */
        CommandLineSettings settings = createSettingsMockWithNecessaryParts();
        when(settings.isVerboseEnabled()).thenReturn(verboseEnabled);
        when(ruleProvider.fetchDeactivatedRuleReferences(any())).thenReturn(new DeactivatedRuleReferences());

        /* execute */
        ZapScanContext result = factoryToTest.create(settings);

        /* test */
        assertEquals(result.isVerboseOutput(), verboseEnabled);

    }

    @ParameterizedTest
    @CsvSource({ "true", "false" })
    void ajaxspider_enabled_from_settings_is_in_result(boolean enabled) {
        /* prepare */
        CommandLineSettings settings = createSettingsMockWithNecessaryParts();
        when(settings.isAjaxSpiderEnabled()).thenReturn(enabled);
        when(ruleProvider.fetchDeactivatedRuleReferences(any())).thenReturn(new DeactivatedRuleReferences());

        /* execute */
        ZapScanContext result = factoryToTest.create(settings);

        /* test */
        assertEquals(result.isAjaxSpiderEnabled(), enabled);

    }

    @ParameterizedTest
    @CsvSource({ "true", "false" })
    void active_scan_enabled_from_settings_is_in_result(boolean enabled) {
        /* prepare */
        CommandLineSettings settings = createSettingsMockWithNecessaryParts();
        when(settings.isActiveScanEnabled()).thenReturn(enabled);
        when(ruleProvider.fetchDeactivatedRuleReferences(any())).thenReturn(new DeactivatedRuleReferences());

        /* execute */
        ZapScanContext result = factoryToTest.create(settings);

        /* test */
        assertEquals(result.isActiveScanEnabled(), enabled);

    }

    @Test
    void report_file_from_setting_is_used_in_result() {
        /* prepare */
        CommandLineSettings settings = createSettingsMockWithNecessaryParts();
        Path path = new File("not-existing").toPath();
        when(settings.getReportFile()).thenReturn(path);
        when(ruleProvider.fetchDeactivatedRuleReferences(any())).thenReturn(new DeactivatedRuleReferences());

        /* execute */
        ZapScanContext result = factoryToTest.create(settings);

        /* test */
        assertEquals(result.getReportFile(), path);

    }

    @Test
    void commandline_settings_null_throws_zap_wrapper_runtime_exception() {
        /* execute + test */
        assertThrows(ZapWrapperRuntimeException.class, () -> factoryToTest.create(null));
    }

    @Test
    void fullruleset_returned_by_provider_is_in_result() {
        /* prepare */
        CommandLineSettings settings = createSettingsMockWithNecessaryParts();
        when(ruleProvider.fetchFullRuleset(fullRulesetFile)).thenReturn(createZapFullRuleset());
        when(ruleProvider.fetchDeactivatedRuleReferences(any())).thenReturn(new DeactivatedRuleReferences());

        /* execute */
        ZapScanContext result = factoryToTest.create(settings);

        ZapFullRuleset fullRuleset = result.getFullRuleset();

        /* test */
        verify(ruleProvider, times(1)).fetchFullRuleset(any());
        assertNotNull(fullRuleset);
        assertNotNull(fullRuleset.getRules());
        assertEquals("https://www.zaproxy.org/docs/alerts/", fullRuleset.getOrigin());
        assertEquals("2022-05-13 14:44:00.635104", fullRuleset.getTimestamp());
        assertEquals(146, fullRuleset.getRules().size());
    }

    @Test
    void rules_to_deactivate_returned_by_provider_is_inside_result() {
        /* prepare */
        CommandLineSettings settings = createSettingsMockWithNecessaryParts();
        when(ruleProvider.fetchDeactivatedRuleReferences(deactivationFile)).thenReturn(createDeactivatedRuleReferences());

        /* execute */
        ZapScanContext result = factoryToTest.create(settings);
        DeactivatedRuleReferences deactivatedRuleReferences = result.getDeactivatedRuleReferences();

        /* test */
        verify(ruleProvider, times(1)).fetchDeactivatedRuleReferences(any());
        assertNotNull(deactivatedRuleReferences);
        assertNotNull(deactivatedRuleReferences.getDeactivatedRuleReferences());
        assertEquals(2, deactivatedRuleReferences.getDeactivatedRuleReferences().size());

    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = { "", "Cross-Site-Scripting-(DOM-Based)-40026,Timestamp-Disclosure-10096",
            "Cross-Site-Scripting-(DOM-Based)-40026,Timestamp-Disclosure-10096,Cross-Domain-Misconfiguration-10098" })
    void rules_to_deactivate_returned_by_env_variable_is_inside_result(String value) {
        /* prepare */
        CommandLineSettings settings = createSettingsMockWithNecessaryPartsWithoutRuleFiles();
        when(environmentVariableReader.readAsString(ZAP_DEACTIVATED_RULE_REFERENCES)).thenReturn(value);
        when(ruleProvider.fetchDeactivatedRuleReferences(any())).thenReturn(new DeactivatedRuleReferences());

        String[] arrayToTestExpectedLength = {};
        if (value != null) {
            arrayToTestExpectedLength = value.split(",");
        }

        /* execute */
        ZapScanContext result = factoryToTest.create(settings);
        DeactivatedRuleReferences deactivatedRuleReferences = result.getDeactivatedRuleReferences();

        /* test */
        assertNotNull(deactivatedRuleReferences);
        assertNotNull(deactivatedRuleReferences.getDeactivatedRuleReferences());
        assertEquals(arrayToTestExpectedLength.length, deactivatedRuleReferences.getDeactivatedRuleReferences().size());
    }

    @ParameterizedTest
    @ValueSource(strings = { "", "Cross-Site-Scripting-(DOM-Based)-40026,Timestamp-Disclosure-10096",
            "Cross-Site-Scripting-(DOM-Based)-40026,Timestamp-Disclosure-10096,Cross-Domain-Misconfiguration-10098" })
    void rules_to_deactivate_returned_by_command_line_parameter_is_inside_result(String value) {
        /* prepare */
        CommandLineSettings settings = createSettingsMockWithNecessaryPartsWithoutRuleFiles();
        when(settings.getDeactivatedRuleReferences()).thenReturn(value);
        when(ruleProvider.fetchDeactivatedRuleReferences(any())).thenReturn(new DeactivatedRuleReferences());

        String[] arrayToTestExpectedLength = {};
        if (value != null) {
            arrayToTestExpectedLength = value.split(",");
        }

        /* execute */
        ZapScanContext result = factoryToTest.create(settings);
        DeactivatedRuleReferences deactivatedRuleReferences = result.getDeactivatedRuleReferences();

        /* test */
        assertNotNull(deactivatedRuleReferences);
        assertNotNull(deactivatedRuleReferences.getDeactivatedRuleReferences());
        assertEquals(arrayToTestExpectedLength.length, deactivatedRuleReferences.getDeactivatedRuleReferences().size());

        verify(environmentVariableReader, never()).readAsString(ZAP_DEACTIVATED_RULE_REFERENCES);
    }

    @ParameterizedTest
    @NullSource
    void rules_to_deactivate_returned_by_command_line_is_null_environment_varibale_reader_is_called_as_fallback(String value) {
        /* prepare */
        CommandLineSettings settings = createSettingsMockWithNecessaryPartsWithoutRuleFiles();
        when(settings.getDeactivatedRuleReferences()).thenReturn(value);
        when(ruleProvider.fetchDeactivatedRuleReferences(any())).thenReturn(new DeactivatedRuleReferences());

        /* execute */
        factoryToTest.create(settings);

        /* test */
        verify(environmentVariableReader, times(1)).readAsString(ZAP_DEACTIVATED_RULE_REFERENCES);
    }

    @Test
    void api_definition_file_from_sechub_scan_config_is_inside_result() {
        /* prepare */
        when(ruleProvider.fetchDeactivatedRuleReferences(any())).thenReturn(new DeactivatedRuleReferences());
        CommandLineSettings settings = createSettingsMockWithNecessaryPartsWithoutRuleFiles();

        File sechubScanConfigFile = new File("src/test/resources/sechub-config-examples/no-auth-with-openapi-file.json");
        String extractedSourcesPath = "path/to/extracted/sources";
        when(settings.getSecHubConfigFile()).thenReturn(sechubScanConfigFile);
        when(environmentVariableReader.readAsString(PDS_JOB_EXTRACTED_SOURCES_FOLDER)).thenReturn(extractedSourcesPath);

        /* execute */
        ZapScanContext result = factoryToTest.create(settings);

        /* test */
        verify(environmentVariableReader, atLeast(1)).readAsString(PDS_JOB_EXTRACTED_SOURCES_FOLDER);
        assertEquals(1, result.getApiDefinitionFiles().size());
    }

    @Test
    void client_certificate_file_from_sechub_scan_config_is_inside_result() {
        /* prepare */
        when(ruleProvider.fetchDeactivatedRuleReferences(any())).thenReturn(new DeactivatedRuleReferences());
        CommandLineSettings settings = createSettingsMockWithNecessaryPartsWithoutRuleFiles();

        File sechubScanConfigFile = new File("src/test/resources/sechub-config-examples/client-certificate-auth.json");
        String extractedSourcesPath = "path/to/extracted/sources";
        when(settings.getSecHubConfigFile()).thenReturn(sechubScanConfigFile);
        when(environmentVariableReader.readAsString(PDS_JOB_EXTRACTED_SOURCES_FOLDER)).thenReturn(extractedSourcesPath);

        /* execute */
        ZapScanContext result = factoryToTest.create(settings);

        /* test */
        verify(environmentVariableReader, atLeast(1)).readAsString(PDS_JOB_EXTRACTED_SOURCES_FOLDER);
        assertNotNull(result.getClientCertificateFile());
    }

    @Test
    void includes_and_excludes_from_sechub_json_are_inside_result() {
        /* prepare */
        when(ruleProvider.fetchDeactivatedRuleReferences(any())).thenReturn(new DeactivatedRuleReferences());
        CommandLineSettings settings = createSettingsMockWithNecessaryPartsWithoutRuleFiles();

        File sechubScanConfigFile = new File("src/test/resources/sechub-config-examples/no-auth-include-exclude.json");
        when(settings.getSecHubConfigFile()).thenReturn(sechubScanConfigFile);

        /* execute */
        ZapScanContext result = factoryToTest.create(settings);

        /* test */
        assertEquals(12, result.getZapURLsIncludeSet().size());
        assertTrue(result.getZapURLsIncludeSet().contains("https://www.targeturl.com"));
        assertEquals(11, result.getZapURLsExcludeSet().size());
    }

    @Test
    void includes_and_excludes_empty_from_sechub_json_result_in_empty_exclude_and_target_url_as_single_include() {
        /* prepare */
        when(ruleProvider.fetchDeactivatedRuleReferences(any())).thenReturn(new DeactivatedRuleReferences());
        CommandLineSettings settings = createSettingsMockWithNecessaryPartsWithoutRuleFiles();

        File sechubScanConfigFile = new File("src/test/resources/sechub-config-examples/no-auth-without-includes-or-excludes.json");
        when(settings.getSecHubConfigFile()).thenReturn(sechubScanConfigFile);

        /* execute */
        ZapScanContext result = factoryToTest.create(settings);

        /* test */
        assertEquals(2, result.getZapURLsIncludeSet().size());
        assertTrue(result.getZapURLsIncludeSet().contains("https://www.targeturl.com.*"));
        assertTrue(result.getZapURLsIncludeSet().contains("https://www.targeturl.com"));
        assertEquals(0, result.getZapURLsExcludeSet().size());
    }

    @ParameterizedTest
    @CsvSource({ "true", "false" })
    void connection_check_from_settings_is_in_result(boolean enabled) {
        /* prepare */
        CommandLineSettings settings = createSettingsMockWithNecessaryParts();
        when(settings.isConnectionCheckEnabled()).thenReturn(enabled);
        when(ruleProvider.fetchDeactivatedRuleReferences(any())).thenReturn(new DeactivatedRuleReferences());

        /* execute */
        ZapScanContext result = factoryToTest.create(settings);

        /* test */
        assertEquals(result.connectionCheckEnabled(), enabled);

    }

    private CommandLineSettings createSettingsMockWithNecessaryParts() {
        CommandLineSettings settings = mock(CommandLineSettings.class);
        when(settings.getTargetURL()).thenReturn("https://www.targeturl.com");
        when(settings.getZapHost()).thenReturn("https://zaphot.example.com");
        when(settings.getZapPort()).thenReturn(815);
        when(settings.getZapApiKey()).thenReturn("secret-key");

        when(settings.getFullRulesetFile()).thenReturn(fullRulesetFile);
        when(settings.getRulesDeactvationFile()).thenReturn(deactivationFile);

        return settings;
    }

    private CommandLineSettings createSettingsMockWithNecessaryPartsWithoutRuleFiles() {
        CommandLineSettings settings = mock(CommandLineSettings.class);
        when(settings.getTargetURL()).thenReturn("https://www.targeturl.com");
        when(settings.getZapHost()).thenReturn("https://zaphot.example.com");
        when(settings.getZapPort()).thenReturn(815);
        when(settings.getZapApiKey()).thenReturn("secret-key");

        when(settings.getFullRulesetFile()).thenReturn(null);
        when(settings.getRulesDeactvationFile()).thenReturn(null);
        when(settings.getPDSUserMessageFolder()).thenReturn("");
        when(settings.getPDSEventFolder()).thenReturn("");

        return settings;
    }

    private ZapFullRuleset createZapFullRuleset() {
        RuleProvider provider = new RuleProvider();
        return provider.fetchFullRuleset(fullRulesetFile);
    }

    private DeactivatedRuleReferences createDeactivatedRuleReferences() {
        RuleProvider provider = new RuleProvider();
        return provider.fetchDeactivatedRuleReferences(deactivationFile);
    }

}
