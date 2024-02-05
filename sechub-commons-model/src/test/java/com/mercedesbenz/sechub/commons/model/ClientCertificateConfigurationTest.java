// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.model;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
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

    @Test
    void from_json_handles_sechub_config_with_client_certificate_correctly() {
        /* prepare */
        String json = """
                {
                  "apiVersion" : "1.0",
                  "data" : {
                    "sources" : [ {
                      "name" : "client-certificate-file-reference",
                      "fileSystem" : {
                        "files" : [ "path/to/backend-cert.p12" ]
                      }
                    } ]
                  },
                  "webScan" : {
                    "url" : "https://my-app.com",
                    "clientCertificate" : {
                      "password" : "example-password",
                      "use" : [ "client-certificate-file-reference" ]
                    }
                  }
                }
                """;

        /* execute */
        SecHubScanConfiguration sechubConfig = JSONConverter.get().fromJSON(SecHubScanConfiguration.class, json);

        /* test */
        // check for expected data section
        assertTrue(sechubConfig.getData().isPresent());

        SecHubDataConfiguration secHubDataConfiguration = sechubConfig.getData().get();
        assertEquals(1, secHubDataConfiguration.getSources().size());
        assertEquals(0, secHubDataConfiguration.getBinaries().size());

        SecHubSourceDataConfiguration sources = secHubDataConfiguration.getSources().get(0);
        assertTrue(sources.getFileSystem().isPresent());
        assertEquals("client-certificate-file-reference", sources.getUniqueName());

        SecHubFileSystemConfiguration secHubFileSystemConfiguration = sources.getFileSystem().get();
        assertEquals(0, secHubFileSystemConfiguration.getFolders().size());
        assertEquals(1, secHubFileSystemConfiguration.getFiles().size());
        assertTrue(secHubFileSystemConfiguration.getFiles().contains("path/to/backend-cert.p12"));

        // check for expected web scan config parts
        assertTrue(sechubConfig.getWebScan().isPresent());

        SecHubWebScanConfiguration secHubWebScanConfiguration = sechubConfig.getWebScan().get();
        assertTrue(secHubWebScanConfiguration.getClientCertificate().isPresent());

        ClientCertificateConfiguration clientCertificate = secHubWebScanConfiguration.getClientCertificate().get();

        Set<String> set = clientCertificate.getNamesOfUsedDataConfigurationObjects();
        assertNotNull(set);
        assertEquals(1, set.size());
        assertTrue(set.contains("client-certificate-file-reference"));

        String actualPassword = new String(clientCertificate.getPassword());
        assertEquals("example-password", actualPassword);
    }

    @Test
    void to_json_handles_sechub_config_with_client_certificate_correctly() {
        /* prepare */
        SecHubScanConfiguration sechubConfig = createSecHubConfigWithClientCertificateConfig();
        String expectedJson = "{\"webScan\":{\"clientCertificate\":{\"password\":\"example-password\",\"use\":[\"client-certificate-file-reference\"]}},"
                + "\"data\":{\"sources\":[{\"fileSystem\":{\"files\":[\"path/to/backend-cert.p12\"],\"folders\":[]},\"name\":\"client-certificate-file-reference\"}],\"binaries\":[]}}";

        /* execute */
        String json = JSONConverter.get().toJSON(sechubConfig);

        /* test */
        assertEquals(expectedJson, json);
    }

    private SecHubScanConfiguration createSecHubConfigWithClientCertificateConfig() {
        // create data section
        SecHubFileSystemConfiguration fileSystem = new SecHubFileSystemConfiguration();
        fileSystem.getFiles().add("path/to/backend-cert.p12");

        SecHubSourceDataConfiguration sourceDataConfig = new SecHubSourceDataConfiguration();
        sourceDataConfig.setFileSystem(fileSystem);
        sourceDataConfig.setUniqueName("client-certificate-file-reference");

        SecHubDataConfiguration dataConfig = new SecHubDataConfiguration();
        dataConfig.getSources().add(sourceDataConfig);

        // create client certificate
        ClientCertificateConfiguration clientCertificateConfiguration = new ClientCertificateConfiguration();
        clientCertificateConfiguration.setPassword("example-password".toCharArray());
        clientCertificateConfiguration.getNamesOfUsedDataConfigurationObjects().add("client-certificate-file-reference");

        // create SecHub configuration
        SecHubWebScanConfiguration webscanConfig = new SecHubWebScanConfiguration();
        webscanConfig.setClientCertificate(Optional.ofNullable(clientCertificateConfiguration));

        SecHubScanConfiguration sechubConfig = new SecHubScanConfiguration();
        sechubConfig.setData(dataConfig);
        sechubConfig.setWebScan(webscanConfig);
        return sechubConfig;
    }
}
