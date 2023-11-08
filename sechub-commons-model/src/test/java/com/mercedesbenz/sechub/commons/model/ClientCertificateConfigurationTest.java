// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;

import org.junit.jupiter.api.Test;

class ClientCertificateConfigurationTest {

    /**
     * We have defined the json attribute "use" for the interface
     * "SecHubDataConfigurationUsageByName" but not for the class - here we check
     * that the "use" attribute works as expected
     */
    @Test
    void json_attribute_use_is_handled_correctly_without_password_set_by_from_json() {
        /* prepare */
        String json = "{ \"use\" : [ \"certificate-reference1\"] }";

        /* execute */
        ClientCertificateConfiguration config = JSONConverter.get().fromJSON(ClientCertificateConfiguration.class, json);

        /* test */
        Set<String> set = config.getNamesOfUsedDataConfigurationObjects();
        assertNotNull(set);
        assertEquals(1, set.size());
        assertTrue(set.contains("certificate-reference1"));
        assertNull(config.getPassword());
    }

    /**
     * We have defined the json attribute "use" for the interface
     * "SecHubDataConfigurationUsageByName" but not for the class - here we check
     * that the "use" attribute works as expected
     */
    @Test
    void json_attribute_use_is_handled_correctly_without_password_set_by_to_json() {
        ClientCertificateConfiguration config = new ClientCertificateConfiguration();
        config.getNamesOfUsedDataConfigurationObjects().add("certificate-reference1");

        /* execute */
        String json = JSONConverter.get().toJSON(config);

        /* test */
        String expected = "{\"use\":[\"certificate-reference1\"]}";
        assertEquals(expected, json);
        assertNull(config.getPassword());
    }

    @Test
    void json_attribute_use_is_handled_correctly_with_password_set_by_from_json() {
        /* prepare */
        String expectedPassword = "secret-password";
        String json = "{ \"password\" : \"" + expectedPassword + "\", \"use\" : [ \"certificate-reference1\"] }";

        /* execute */
        ClientCertificateConfiguration config = JSONConverter.get().fromJSON(ClientCertificateConfiguration.class, json);

        /* test */
        Set<String> set = config.getNamesOfUsedDataConfigurationObjects();
        assertNotNull(set);
        assertEquals(1, set.size());
        assertTrue(set.contains("certificate-reference1"));

        String actualPassword = new String(config.getPassword());
        assertEquals(expectedPassword, actualPassword);
    }

    @Test
    void json_attribute_use_is_handled_correctly_with_password_set_by_to_json() {
        ClientCertificateConfiguration config = new ClientCertificateConfiguration();
        config.getNamesOfUsedDataConfigurationObjects().add("certificate-reference1");
        config.setPassword("secret-password".toCharArray());

        /* execute */
        String json = JSONConverter.get().toJSON(config);

        /* test */
        String expected = "{\"password\":\"secret-password\",\"use\":[\"certificate-reference1\"]}";
        assertEquals(expected, json);
    }
}
