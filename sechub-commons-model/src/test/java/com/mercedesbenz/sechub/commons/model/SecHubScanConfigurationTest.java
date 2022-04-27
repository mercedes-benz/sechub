// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.model;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.test.TestFileReader;

class SecHubScanConfigurationTest {

    @Test
    void sechub_job_config_example1_JSON_can_be_deserialized_and_contains_expected_login_url() {

        /* prepare */
        String json = TestFileReader.loadTextFile(new File("./src/test/resources/sechub_config_example1.json"));

        /* execute */
        SecHubScanConfiguration scanConfig = SecHubScanConfiguration.createFromJSON(json);

        /* test */
        assertEquals("https://productfailure.demo.example.org/login", scanConfig.getWebScan().get().getLogin().get().getUrl().toExternalForm());
    }

    @Test
    void sechub_job_config_license_scan_JSON_can_be_deserialized_and_contains_expected_source_data_reference() {

        /* prepare */
        String json = TestFileReader.loadTextFile(new File("./src/test/resources/sechub_license_scan_config_source_example.json"));

        /* execute */
        SecHubScanConfiguration scanConfig = SecHubScanConfiguration.createFromJSON(json);

        /* test */
        Set<String> usedDataConfigurations = scanConfig.getLicenseScan().get().getNamesOfUsedDataConfigurationObjects();
        assertEquals(1, usedDataConfigurations.size());
        assertEquals("code", usedDataConfigurations.iterator().next());
    }

    @Test
    void sechub_job_config_license_scan_JSON_cannot_be_deserialized_used_singular_source() {

        /* prepare */
        String json = TestFileReader.loadTextFile(new File("./src/test/resources/sechub_license_scan_wrong_source_config_example.json"));

        /* execute + test */
        Assertions.assertThrows(JSONConverterException.class, () -> {
            SecHubScanConfiguration.createFromJSON(json);
        });
    }

    @Test
    void sechub_job_config_license_scan_JSON_non_existent_scan_type() {

        /* prepare */
        String json = TestFileReader.loadTextFile(new File("./src/test/resources/sechub_unknown_scan_type.json"));

        /* execute */
        SecHubScanConfiguration scanConfig = SecHubScanConfiguration.createFromJSON(json);

        /* test */
        assertFalse(scanConfig.getWebScan().isPresent());
        assertFalse(scanConfig.getLicenseScan().isPresent());
        assertFalse(scanConfig.getCodeScan().isPresent());
        assertFalse(scanConfig.getInfraScan().isPresent());

        assertEquals("1.0", scanConfig.getApiVersion());
        assertEquals(1, scanConfig.getData().get().getSources().size());

        SecHubSourceDataConfiguration sourceConfig = scanConfig.getData().get().getSources().iterator().next();
        assertEquals("code", sourceConfig.getUniqueName());
        assertTrue(sourceConfig.getFileSystem().isPresent());
    }

    @Test
    void sechub_job_config_license_scan_JSON_can_be_deserialized_and_contains_expected_binary_data_reference() {

        /* prepare */
        String json = TestFileReader.loadTextFile(new File("./src/test/resources/sechub_license_scan_config_binary_example.json"));

        /* execute */
        SecHubScanConfiguration scanConfig = SecHubScanConfiguration.createFromJSON(json);

        /* test */
        Set<String> usedDataConfigurations = scanConfig.getLicenseScan().get().getNamesOfUsedDataConfigurationObjects();
        assertEquals(1, usedDataConfigurations.size());
        assertEquals("build-artifacts", usedDataConfigurations.iterator().next());
    }
}
