// SPDX-License-Identifier: MIT
package com.daimler.sechub.client.java;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;

import org.junit.jupiter.api.Test;

class SecHubJobConfigurationTest {

    @Test
    void sechub_job_config_example1_JSON_can_be_desrialized_and_contains_expected_login_url() {
        /* prepare */
        TextFileReader reader = new TextFileReader();
        String json = reader.loadTextFile(new File("./src/test/resources/sechub_config_example1.json"));

        /* execute */
        SecHubJobConfiguration jobConfig = SecHubJobConfiguration.createFromJSON(json);

        /* test */
        assertEquals("https://productfailure.demo.example.org/login", jobConfig.getWebScan().get().getLogin().get().getUrl().toExternalForm());
    }

}
