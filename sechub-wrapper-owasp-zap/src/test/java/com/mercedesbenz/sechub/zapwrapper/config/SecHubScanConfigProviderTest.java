// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.zapwrapper.config;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.File;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.commons.model.SecHubScanConfiguration;
import com.mercedesbenz.sechub.commons.model.SecHubWebScanConfiguration;
import com.mercedesbenz.sechub.commons.model.login.BasicLoginConfiguration;
import com.mercedesbenz.sechub.commons.model.login.WebLoginConfiguration;
import com.mercedesbenz.sechub.zapwrapper.cli.ZapWrapperRuntimeException;
import com.mercedesbenz.sechub.zapwrapper.util.EnvironmentVariableConstants;
import com.mercedesbenz.sechub.zapwrapper.util.EnvironmentVariableReader;

class SecHubScanConfigProviderTest {

    private SecHubScanConfigProvider providerToTest;

    private EnvironmentVariableReader environmentVariableReader;

    @BeforeEach
    void beforeEach() {
        providerToTest = new SecHubScanConfigProvider();

        environmentVariableReader = mock();
    }

    @Test
    void fetch_sechub_web_config_when_file_and_env_variable_are_not_set_results_in_empty_sechub_config() {
        /* execute */
        SecHubScanConfiguration sechubScanConfig = providerToTest.fetchSecHubScanConfiguration(null, null);

        /* test */
        assertTrue(sechubScanConfig.getWebScan().isEmpty());
        verify(environmentVariableReader, never()).readAsString(EnvironmentVariableConstants.PDS_SCAN_CONFIGURATION);
    }

    @Test
    void fetch_sechub_web_config_when_file_is_not_set_butenv_variable_is_set_results_in_config_from_env_variable() {
        String json = """
                {
                  "apiVersion" : "1.0",
                  "webScan" : {
                    "url" : "https://127.0.0.1:8080",
                    "login" : {
                      "url" : "https://127.0.0.1:8080/login",
                      "basic" : {
                        "realm" : "realm",
                        "user" : "user",
                        "password" : "password"
                      }
                    }
                  }
                }
                """;

        when(environmentVariableReader.readAsString(EnvironmentVariableConstants.PDS_SCAN_CONFIGURATION)).thenReturn(json);

        /* execute */
        SecHubWebScanConfiguration sechubWebConfig = providerToTest.fetchSecHubScanConfiguration(null, environmentVariableReader).getWebScan().get();

        /* test */
        assertEquals(sechubWebConfig.getUrl().toString(), "https://127.0.0.1:8080");
        assertTrue(sechubWebConfig.getLogin().isPresent());

        WebLoginConfiguration webLoginConfiguration = sechubWebConfig.getLogin().get();
        assertEquals(webLoginConfiguration.getUrl().toExternalForm(), "https://127.0.0.1:8080/login");
        assertTrue(webLoginConfiguration.getBasic().isPresent());

        BasicLoginConfiguration basicLoginConfiguration = webLoginConfiguration.getBasic().get();
        assertEquals(basicLoginConfiguration.getRealm().get(), "realm");

        String user = new String(basicLoginConfiguration.getUser());
        assertEquals(user, "user");

        String password = new String(basicLoginConfiguration.getPassword());
        assertEquals(password, "password");

        verify(environmentVariableReader).readAsString(EnvironmentVariableConstants.PDS_SCAN_CONFIGURATION);
    }

    @Test
    void fetch_sechub_web_config_by_sechub_file_works_when_file_can_be_read() {
        /* prepare */
        File testFile = new File("src/test/resources/sechub-config-examples/basic-auth.json");

        /* execute */
        SecHubWebScanConfiguration sechubWebConfig = providerToTest.fetchSecHubScanConfiguration(testFile, null).getWebScan().get();

        /* test */
        assertEquals(sechubWebConfig.getUrl().toString(), "https://127.0.0.1:8080");
        assertTrue(sechubWebConfig.getLogin().isPresent());

        WebLoginConfiguration webLoginConfiguration = sechubWebConfig.getLogin().get();
        assertEquals(webLoginConfiguration.getUrl().toExternalForm(), "https://127.0.0.1:8080/login");
        assertTrue(webLoginConfiguration.getBasic().isPresent());

        BasicLoginConfiguration basicLoginConfiguration = webLoginConfiguration.getBasic().get();
        assertEquals(basicLoginConfiguration.getRealm().get(), "realm");

        String user = new String(basicLoginConfiguration.getUser());
        assertEquals(user, "user");

        String password = new String(basicLoginConfiguration.getPassword());
        assertEquals(password, "password");

        verify(environmentVariableReader, never()).readAsString(EnvironmentVariableConstants.PDS_SCAN_CONFIGURATION);
    }

    @Test
    void fetch_sechub_web_config_when_config_file_and_env_are_set_results_in_config_file_being_used() {
        /* prepare */
        File testFile = new File("src/test/resources/sechub-config-examples/basic-auth.json");

        /* execute */
        SecHubWebScanConfiguration sechubWebConfig = providerToTest.fetchSecHubScanConfiguration(testFile, environmentVariableReader).getWebScan().get();

        /* test */
        assertEquals(sechubWebConfig.getUrl().toString(), "https://127.0.0.1:8080");
        assertTrue(sechubWebConfig.getLogin().isPresent());

        WebLoginConfiguration webLoginConfiguration = sechubWebConfig.getLogin().get();
        assertEquals(webLoginConfiguration.getUrl().toExternalForm(), "https://127.0.0.1:8080/login");
        assertTrue(webLoginConfiguration.getBasic().isPresent());

        BasicLoginConfiguration basicLoginConfiguration = webLoginConfiguration.getBasic().get();
        assertEquals(basicLoginConfiguration.getRealm().get(), "realm");

        String user = new String(basicLoginConfiguration.getUser());
        assertEquals(user, "user");

        String password = new String(basicLoginConfiguration.getPassword());
        assertEquals(password, "password");

        verify(environmentVariableReader, never()).readAsString(EnvironmentVariableConstants.PDS_SCAN_CONFIGURATION);
    }

    @Test
    void fetch_sechub_web_config_by_sechub_file_throws_zap_wrapper_runtime_exception() {
        /* prepare */
        File testFile = new File("not-existing-file");

        /* execute + test */
        assertThrows(ZapWrapperRuntimeException.class, () -> providerToTest.fetchSecHubScanConfiguration(testFile, environmentVariableReader));

        verify(environmentVariableReader, never()).readAsString(EnvironmentVariableConstants.PDS_SCAN_CONFIGURATION);
    }

}
