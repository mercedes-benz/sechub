// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class SecHubSecretScanConfigurationTest {
    @Test
    void secret_scan_no_data_reference() {
        /* execute */
        SecHubSecretScanConfiguration secretScan = new SecHubSecretScanConfiguration();

        /* test */
        assertTrue(secretScan.getNamesOfUsedDataConfigurationObjects().isEmpty());
    }

    @Test
    void secret_scan_with_data_reference() {
        /* prepare */
        String name = "build-artifacts";
        SecHubSecretScanConfiguration secretScan = new SecHubSecretScanConfiguration();

        /* execute */
        secretScan.getNamesOfUsedDataConfigurationObjects().add(name);

        /* test */
        assertFalse(secretScan.getNamesOfUsedDataConfigurationObjects().isEmpty());
        assertEquals(1, secretScan.getNamesOfUsedDataConfigurationObjects().size());
        assertEquals(name, secretScan.getNamesOfUsedDataConfigurationObjects().iterator().next());
    }

    @Test
    void json_attribute_use_is_handled_correctly_by_from_json() {
        /* prepare */
        String json = "{ \"use\" : \"my-unique-name1\" }";

        /* execute */
        SecHubSecretScanConfiguration secretScan = JSONConverter.get().fromJSON(SecHubSecretScanConfiguration.class, json);

        /* test */
        assertEquals("my-unique-name1", secretScan.getNamesOfUsedDataConfigurationObjects().iterator().next());
    }
}
