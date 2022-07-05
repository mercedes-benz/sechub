// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.owaspzapwrapper.config;

import static com.mercedesbenz.sechub.owaspzapwrapper.util.EnvironmentVariableConstants.PROXY_HOST_ENV_VARIABLE_NAME;
import static com.mercedesbenz.sechub.owaspzapwrapper.util.EnvironmentVariableConstants.PROXY_PORT_ENV_VARIABLE_NAME;
import static com.mercedesbenz.sechub.owaspzapwrapper.util.EnvironmentVariableConstants.ZAP_API_KEY_ENV_VARIABLE_NAME;
import static com.mercedesbenz.sechub.owaspzapwrapper.util.EnvironmentVariableConstants.ZAP_DEACTIVATED_RULE_REFERENCES;
import static com.mercedesbenz.sechub.owaspzapwrapper.util.EnvironmentVariableConstants.ZAP_HOST_ENV_VARIABLE_NAME;
import static com.mercedesbenz.sechub.owaspzapwrapper.util.EnvironmentVariableConstants.ZAP_PORT_ENV_VARIABLE_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.net.URI;
import java.nio.file.Path;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import com.mercedesbenz.sechub.commons.model.SecHubWebScanConfiguration;
import com.mercedesbenz.sechub.owaspzapwrapper.cli.CommandLineSettings;
import com.mercedesbenz.sechub.owaspzapwrapper.cli.MustExitRuntimeException;
import com.mercedesbenz.sechub.owaspzapwrapper.config.auth.AuthenticationType;
import com.mercedesbenz.sechub.owaspzapwrapper.config.data.DeactivatedRuleReferences;
import com.mercedesbenz.sechub.owaspzapwrapper.config.data.OwaspZapFullRuleset;
import com.mercedesbenz.sechub.owaspzapwrapper.helper.BaseTargetUriFactory;
import com.mercedesbenz.sechub.owaspzapwrapper.helper.SecHubWebScanConfigurationHelper;
import com.mercedesbenz.sechub.owaspzapwrapper.util.EnvironmentVariableReader;

class OwaspZapScanConfigurationFactoryTest {

    private OwaspZapScanConfigurationFactory factoryToTest;

    private SecHubWebScanConfigurationHelper sechubWebConfigHelper;
    private EnvironmentVariableReader environmentVariableReader;
    private BaseTargetUriFactory targetUriFactory;

    private SechubWebConfigProvider webConfigProvider;

    private RuleProvider ruleProvider;

    private File fullRulesetFile;
    private File deactivationFile;

    @BeforeEach
    void beforeEach() {
        // create object to test
        factoryToTest = new OwaspZapScanConfigurationFactory();

        // create mocks
        sechubWebConfigHelper = mock(SecHubWebScanConfigurationHelper.class);
        environmentVariableReader = mock(EnvironmentVariableReader.class);
        targetUriFactory = mock(BaseTargetUriFactory.class);
        webConfigProvider = mock(SechubWebConfigProvider.class);
        ruleProvider = mock(RuleProvider.class);

        // connect mocks with test object
        factoryToTest.sechubWebConfigHelper = sechubWebConfigHelper;
        factoryToTest.environmentVariableReader = environmentVariableReader;
        factoryToTest.targetUriFactory = targetUriFactory;
        factoryToTest.webConfigProvider = webConfigProvider;
        factoryToTest.ruleProvider = ruleProvider;

        // create test data
        fullRulesetFile = new File("src/test/resources/zap-available-rules/owaspzap-full-ruleset.json");
        deactivationFile = new File("src/test/resources/wrapper-deactivated-rule-examples/owaspzap-rules-to-deactivate.json");
    }

    @Test
    void commandLineSettings_object_is_null_results_in_mustexitruntimeexception() {
        /* execute + test */
        assertThrows(MustExitRuntimeException.class, () -> factoryToTest.create(null));
    }

    @Test
    void created_configuration_has_max_scan_duration_from_sechub_webconfig() {
        /* prepare */
        CommandLineSettings settings = createSettingsMockWithNecessaryParts();
        SecHubWebScanConfiguration config = simulateProvidedSecHubConfiguration(settings);

        long maxScanDuration = 4711L;
        when(sechubWebConfigHelper.fetchMaxScanDurationInMillis(config)).thenReturn(maxScanDuration);

        /* execute */
        OwaspZapScanConfiguration result = factoryToTest.create(settings);

        /* test */
        assertEquals(result.getMaxScanDurationInMillis(), maxScanDuration);

    }

    @Test
    void configuration_returned_by_provider_is_inside_result() {
        /* prepare */
        CommandLineSettings settings = createSettingsMockWithNecessaryParts();
        SecHubWebScanConfiguration config = simulateProvidedSecHubConfiguration(settings);

        /* execute */
        OwaspZapScanConfiguration result = factoryToTest.create(settings);

        /* test */
        assertEquals(result.getSecHubWebScanConfiguration(), config);

    }

    @Test
    void context_name_is_used_from_settings_when_defined() {
        /* prepare */
        CommandLineSettings settings = createSettingsMockWithNecessaryParts();

        String jobUUID = "12345";
        when(settings.getJobUUID()).thenReturn(jobUUID);

        /* execute */
        OwaspZapScanConfiguration result = factoryToTest.create(settings);

        /* test */
        assertEquals(result.getContextName(), jobUUID);

    }

    @Test
    void context_name_is_created_as_UUID_when_not_defined() {
        /* prepare */
        CommandLineSettings settings = createSettingsMockWithNecessaryParts();
        when(settings.getJobUUID()).thenReturn(null);

        /* execute */
        OwaspZapScanConfiguration result = factoryToTest.create(settings);

        /* test */
        String contextName = result.getContextName();
        assertNotNull(contextName);
        UUID.fromString(contextName);// just check it us a uuid... (otherwise exception)
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

        /* execute */
        OwaspZapScanConfiguration result = factoryToTest.create(settings);

        /* test */
        OwaspZapServerConfiguration serverConfig = result.getServerConfig();
        assertNotNull(serverConfig);
        assertEquals(host, serverConfig.getZaproxyHost());
        assertEquals(port, serverConfig.getZaproxyPort());
        assertEquals(apiKey, serverConfig.getZaproxyApiKey());

        assertEquals(proxy, result.getProxyInformation().getHost());
        assertEquals(proxyPort, result.getProxyInformation().getPort());
    }

    @ParameterizedTest
    @CsvSource({ "https://zaproxy.example.com,8080,api-key,https://proxy.example.com,3333", "host,4711,secret,proxy,5312" })
    void result_contains_server_config_with_arguments_from_environment_when_command_line_settings_not_set(String host, int port, String apiKey, String proxy,
            int proxyPort) {
        /* prepare */
        CommandLineSettings settings = mock(CommandLineSettings.class);
        when(environmentVariableReader.readAsString(ZAP_HOST_ENV_VARIABLE_NAME)).thenReturn(host);
        when(environmentVariableReader.readAsString(ZAP_API_KEY_ENV_VARIABLE_NAME)).thenReturn(apiKey);
        when(environmentVariableReader.readAsInt(ZAP_PORT_ENV_VARIABLE_NAME)).thenReturn(port);

        when(environmentVariableReader.readAsString(PROXY_HOST_ENV_VARIABLE_NAME)).thenReturn(proxy);
        when(environmentVariableReader.readAsInt(PROXY_PORT_ENV_VARIABLE_NAME)).thenReturn(proxyPort);

        /* execute */
        OwaspZapScanConfiguration result = factoryToTest.create(settings);

        /* test */
        OwaspZapServerConfiguration serverConfig = result.getServerConfig();
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

        when(environmentVariableReader.readAsString(PROXY_HOST_ENV_VARIABLE_NAME)).thenReturn(null);
        when(environmentVariableReader.readAsInt(PROXY_PORT_ENV_VARIABLE_NAME)).thenReturn(0);

        /* execute */
        OwaspZapScanConfiguration result = factoryToTest.create(settings);

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
        if (port != null) {
            // when not defined in CSV, null is used. The mock does use int primitive,
            // so we just do not set it.
            when(settings.getZapPort()).thenReturn(port);
        }
        when(settings.getZapApiKey()).thenReturn(apiKey);

        /* execute + test */
        assertThrows(MustExitRuntimeException.class, () -> factoryToTest.create(settings));

    }

    @Test
    void authentication_type_from_config_is_in_result() {
        /* prepare */
        CommandLineSettings settings = createSettingsMockWithNecessaryParts();
        SecHubWebScanConfiguration config = simulateProvidedSecHubConfiguration(settings);

        AuthenticationType type = AuthenticationType.FORM_BASED_AUTHENTICATION;
        when(sechubWebConfigHelper.determineAuthenticationType(config)).thenReturn(type);

        /* execute */
        OwaspZapScanConfiguration result = factoryToTest.create(settings);

        /* test */
        assertEquals(result.getAuthenticationType(), type);

    }

    @Test
    void targetURI_calculated_by_factory_is_in_result() {
        /* prepare */
        CommandLineSettings settings = createSettingsMockWithNecessaryParts();
        String targetUri = "https://www.example.com";
        when(settings.getTargetURL()).thenReturn(targetUri);

        URI createdUri = URI.create("https://fromfactory.example.com");
        when(targetUriFactory.create(targetUri)).thenReturn(createdUri);

        /* execute */
        OwaspZapScanConfiguration result = factoryToTest.create(settings);

        /* test */
        assertEquals(result.getTargetUri(), createdUri);

    }

    @ParameterizedTest
    @CsvSource({ "true", "false" })
    void verbose_from_settings_is_in_result(boolean verboseEnabled) {
        /* prepare */
        CommandLineSettings settings = createSettingsMockWithNecessaryParts();
        when(settings.isVerboseEnabled()).thenReturn(verboseEnabled);

        /* execute */
        OwaspZapScanConfiguration result = factoryToTest.create(settings);

        /* test */
        assertEquals(result.isVerboseOutput(), verboseEnabled);

    }

    @ParameterizedTest
    @CsvSource({ "true", "false" })
    void ajaxspider_enabled_from_settings_is_in_result(boolean enabled) {
        /* prepare */
        CommandLineSettings settings = createSettingsMockWithNecessaryParts();
        when(settings.isAjaxSpiderEnabled()).thenReturn(enabled);

        /* execute */
        OwaspZapScanConfiguration result = factoryToTest.create(settings);

        /* test */
        assertEquals(result.isAjaxSpiderEnabled(), enabled);

    }

    @ParameterizedTest
    @CsvSource({ "true", "false" })
    void active_scan_enabled_from_settings_is_in_result(boolean enabled) {
        /* prepare */
        CommandLineSettings settings = createSettingsMockWithNecessaryParts();
        when(settings.isActiveScanEnabled()).thenReturn(enabled);

        /* execute */
        OwaspZapScanConfiguration result = factoryToTest.create(settings);

        /* test */
        assertEquals(result.isActiveScanEnabled(), enabled);

    }

    @Test
    void report_file_from_setting_is_used_in_result() {
        /* prepare */
        CommandLineSettings settings = createSettingsMockWithNecessaryParts();
        Path path = new File("not-existing").toPath();
        when(settings.getReportFile()).thenReturn(path);

        /* execute */
        OwaspZapScanConfiguration result = factoryToTest.create(settings);

        /* test */
        assertEquals(result.getReportFile(), path);

    }

    @Test
    void commandline_settings_null_throws_must_exit_runtime_exception() {
        /* execute + test */
        assertThrows(MustExitRuntimeException.class, () -> factoryToTest.create(null));
    }

    @Test
    void fullruleset_returned_by_provider_is_in_result() {
        /* prepare */
        CommandLineSettings settings = createSettingsMockWithNecessaryParts();
        when(ruleProvider.fetchFullRuleset(fullRulesetFile)).thenReturn(createOwaspZapFullRuleset());

        /* execute */
        OwaspZapScanConfiguration result = factoryToTest.create(settings);

        OwaspZapFullRuleset fullRuleset = result.getFullRuleset();

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
        OwaspZapScanConfiguration result = factoryToTest.create(settings);
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

        String[] arrayToTestExpectedLength = {};
        if (value != null) {
            arrayToTestExpectedLength = value.split(",");
        }

        /* execute */
        OwaspZapScanConfiguration result = factoryToTest.create(settings);
        DeactivatedRuleReferences deactivatedRuleReferences = result.getDeactivatedRuleReferences();

        /* test */
        assertNotNull(deactivatedRuleReferences);
        assertNotNull(deactivatedRuleReferences.getDeactivatedRuleReferences());
        assertEquals(arrayToTestExpectedLength.length, deactivatedRuleReferences.getDeactivatedRuleReferences().size());
        assertEquals(arrayToTestExpectedLength.length, deactivatedRuleReferences.getDeactivatedRuleReferences().size());
    }

    private SecHubWebScanConfiguration simulateProvidedSecHubConfiguration(CommandLineSettings settings) {
        File file = new File("not-existing-just-placeholder");
        when(settings.getSecHubConfigFile()).thenReturn(file);

        SecHubWebScanConfiguration config = new SecHubWebScanConfiguration();
        when(webConfigProvider.getSecHubWebConfiguration(file)).thenReturn(config);
        return config;
    }

    private CommandLineSettings createSettingsMockWithNecessaryParts() {
        CommandLineSettings settings = mock(CommandLineSettings.class);
        when(settings.getZapHost()).thenReturn("https://zaphot.example.com");
        when(settings.getZapPort()).thenReturn(815);
        when(settings.getZapApiKey()).thenReturn("secret-key");

        when(settings.getFullRulesetFile()).thenReturn(fullRulesetFile);
        when(settings.getRulesDeactvationFile()).thenReturn(deactivationFile);

        return settings;
    }

    private CommandLineSettings createSettingsMockWithNecessaryPartsWithoutRuleFiles() {
        CommandLineSettings settings = mock(CommandLineSettings.class);
        when(settings.getZapHost()).thenReturn("https://zaphot.example.com");
        when(settings.getZapPort()).thenReturn(815);
        when(settings.getZapApiKey()).thenReturn("secret-key");

        when(settings.getFullRulesetFile()).thenReturn(null);
        when(settings.getRulesDeactvationFile()).thenReturn(null);

        return settings;
    }

    private OwaspZapFullRuleset createOwaspZapFullRuleset() {
        RuleProvider provider = new RuleProvider();
        return provider.fetchFullRuleset(fullRulesetFile);
    }

    private DeactivatedRuleReferences createDeactivatedRuleReferences() {
        RuleProvider provider = new RuleProvider();
        return provider.fetchDeactivatedRuleReferences(deactivationFile);
    }

}
