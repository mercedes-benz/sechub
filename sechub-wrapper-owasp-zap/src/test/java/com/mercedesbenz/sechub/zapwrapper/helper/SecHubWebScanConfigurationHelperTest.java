// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.zapwrapper.helper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;

import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.commons.model.SecHubScanConfiguration;
import com.mercedesbenz.sechub.commons.model.SecHubWebScanConfiguration;
import com.mercedesbenz.sechub.test.TestFileReader;
import com.mercedesbenz.sechub.zapwrapper.config.auth.AuthenticationType;

class SecHubWebScanConfigurationHelperTest {

    private SecHubWebScanConfigurationHelper helperToTest = new SecHubWebScanConfigurationHelper();

    @Test
    void determines_AuthenticationType_from_empty_sechub_config_correctly() {
        /* execute */
        AuthenticationType authTypeNewConfig = helperToTest.determineAuthenticationType(new SecHubWebScanConfiguration());

        /* test */
        assertEquals(authTypeNewConfig, AuthenticationType.UNAUTHENTICATED);
    }

    @Test
    void determines_AuthenticationType_sechub_config_is_null() {
        /* execute */
        AuthenticationType authTypeFromNull = helperToTest.determineAuthenticationType(null);

        /* test */
        assertEquals(authTypeFromNull, AuthenticationType.UNAUTHENTICATED);
    }

    @Test
    void determines_AuthenticationType_sechub_config_has_basic_auth() {
        /* prepare */
        File file = new File("src/test/resources/sechub-config-examples/basic-auth.json");
        String sechubConfigJSON = TestFileReader.loadTextFile(file);
        SecHubScanConfiguration sechubConfig = SecHubScanConfiguration.createFromJSON(sechubConfigJSON);
        SecHubWebScanConfiguration secHubWebScanConfiguration = sechubConfig.getWebScan().get();

        /* execute */
        AuthenticationType authenticationType = helperToTest.determineAuthenticationType(secHubWebScanConfiguration);

        /* test */
        assertEquals(authenticationType, AuthenticationType.HTTP_BASIC_AUTHENTICATION);
    }
}
