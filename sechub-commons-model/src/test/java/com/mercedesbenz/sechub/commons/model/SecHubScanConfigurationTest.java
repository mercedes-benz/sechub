// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.model;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;

import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.test.TestFileReader;

class SecHubScanConfigurationTest {

    @Test
    void sechub_job_config_example1_JSON_can_be_desrialized_and_contains_expected_login_url() {

        /* prepare */
        String json = TestFileReader.loadTextFile(new File("./src/test/resources/sechub_config_example1.json"));

        /* execute */
        SecHubScanConfiguration scanConfig = SecHubScanConfiguration.createFromJSON(json);

        /* test */
        assertEquals("https://productfailure.demo.example.org/login", scanConfig.getWebScan().get().getLogin().get().getUrl().toExternalForm());
    }

}
