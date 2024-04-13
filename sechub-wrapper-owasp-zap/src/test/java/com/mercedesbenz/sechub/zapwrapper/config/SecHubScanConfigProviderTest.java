// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.zapwrapper.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.commons.model.SecHubWebScanConfiguration;
import com.mercedesbenz.sechub.commons.model.login.BasicLoginConfiguration;
import com.mercedesbenz.sechub.commons.model.login.WebLoginConfiguration;
import com.mercedesbenz.sechub.zapwrapper.cli.ZapWrapperRuntimeException;

class SecHubScanConfigProviderTest {

    private SecHubScanConfigProvider providerToTest;

    @BeforeEach
    void beforeEach() {
        providerToTest = new SecHubScanConfigProvider();
    }

    @Test
    void get_sechub_web_config_by_sechub_file_works_when_file_can_be_read() {
        /* prepare */
        File testFile = new File("src/test/resources/sechub-config-examples/basic-auth.json");

        /* execute */
        SecHubWebScanConfiguration sechubWebConfig = providerToTest.getSecHubWebConfiguration(testFile).getWebScan().get();

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
    }

    @Test
    void get_sechub_web_config_by_sechub_file_throws_zap_wrapper_runtime_exception() {
        /* prepare */
        File testFile = new File("not-existing-file");

        /* execute + test */
        assertThrows(ZapWrapperRuntimeException.class, () -> providerToTest.getSecHubWebConfiguration(testFile));

    }

}
