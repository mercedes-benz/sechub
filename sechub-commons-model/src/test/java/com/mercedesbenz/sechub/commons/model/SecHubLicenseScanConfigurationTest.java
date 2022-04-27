package com.mercedesbenz.sechub.commons.model;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class SecHubLicenseScanConfigurationTest {
    @Test
    void license_scan_no_data_reference() {
        /* execute */
    	SecHubLicenseScanConfiguration licenseScan = new SecHubLicenseScanConfiguration();

        /* test */
        assertTrue(licenseScan.getNamesOfUsedDataConfigurationObjects().isEmpty());
    }
    
    @Test
    void license_scan_with_data_reference() {
    	/* prepare */
    	String name = "build-artifacts";
    	SecHubLicenseScanConfiguration licenseScan = new SecHubLicenseScanConfiguration();
    	
        /* execute */
    	licenseScan.getNamesOfUsedDataConfigurationObjects().add(name);

        /* test */
    	assertFalse(licenseScan.getNamesOfUsedDataConfigurationObjects().isEmpty());
        assertEquals(1, licenseScan.getNamesOfUsedDataConfigurationObjects().size());
        assertEquals(name, licenseScan.getNamesOfUsedDataConfigurationObjects().iterator().next());
    }
    
    @Test
    void json_attribute_use_is_handled_correctly_by_from_json() {
        /* prepare */
        String json = "{ \"use\" : \"my-unique-name1\" }";

        /* execute */
        SecHubLicenseScanConfiguration licenseScan = JSONConverter.get().fromJSON(SecHubLicenseScanConfiguration.class, json);

        /* test */
        assertEquals("my-unique-name1", licenseScan.getNamesOfUsedDataConfigurationObjects().iterator().next());
    }
}
