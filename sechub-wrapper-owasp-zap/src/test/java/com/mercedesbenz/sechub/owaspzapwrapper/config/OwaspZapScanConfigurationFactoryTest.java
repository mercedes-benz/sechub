// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.owaspzapwrapper.config;

import static com.mercedesbenz.sechub.owaspzapwrapper.util.EnvironmentVariableConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.net.URI;
import java.nio.file.Path;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.mercedesbenz.sechub.commons.model.SecHubWebScanConfiguration;
import com.mercedesbenz.sechub.owaspzapwrapper.cli.CommandLineSettings;
import com.mercedesbenz.sechub.owaspzapwrapper.cli.MustExitRuntimeException;
import com.mercedesbenz.sechub.owaspzapwrapper.config.auth.AuthenticationType;
import com.mercedesbenz.sechub.owaspzapwrapper.helper.BaseTargetUriFactory;
import com.mercedesbenz.sechub.owaspzapwrapper.helper.SecHubWebScanConfigurationHelper;
import com.mercedesbenz.sechub.owaspzapwrapper.util.EnvironmentVariableReader;

class OwaspZapScanConfigurationFactoryTest {

    private OwaspZapScanConfigurationFactory factoryToTest;

    private SecHubWebScanConfigurationHelper sechubWebConfigHelper;
    private EnvironmentVariableReader environmentVariableReader;
    private BaseTargetUriFactory targetUriFactory;

    private SechubWebConfigProvider webConfigProvider;

    @BeforeEach
    void beforeEach() {
        // create object to test
        factoryToTest = new OwaspZapScanConfigurationFactory();

        // create mocks
        sechubWebConfigHelper = mock(SecHubWebScanConfigurationHelper.class);
        environmentVariableReader = mock(EnvironmentVariableReader.class);
        targetUriFactory = mock(BaseTargetUriFactory.class);
        webConfigProvider = mock(SechubWebConfigProvider.class);

        // connect mocks with test object
        factoryToTest.sechubWebConfigHelper = sechubWebConfigHelper;
        factoryToTest.environmentVariableReader = environmentVariableReader;
        factoryToTest.targetUriFactory = targetUriFactory;
        factoryToTest.webConfigProvider = webConfigProvider;
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

        long maxScanDueration = 4711L;
        when(sechubWebConfigHelper.fetchMaxScanDurationInMillis(config)).thenReturn(maxScanDueration);

        /* execute */
        OwaspZapScanConfiguration result = factoryToTest.create(settings);

        /* test */
        assertEquals(result.getMaxScanDurationInMillis(), maxScanDueration);

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

        verify(environmentVariableReader, never()).readAsInt(any());
        verify(environmentVariableReader, never()).readAsString(any());
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
    void sechub_webconfig_helper_retrieveMaxScanDurationInMillis_is_called_with_correct_webconfig() {
        /* execute + test */
        assertThrows(MustExitRuntimeException.class, () -> factoryToTest.create(null));
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
        return settings;
    }

}
