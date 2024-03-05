// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.model;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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
    void sechub_job_config_license_scan_JSON_can_be_deserialized_even_with_unknown_key() {

        /* prepare */
        String json = TestFileReader.loadTextFile(new File("./src/test/resources/sechub_license_scan_non_existing_key.json"));

        /* execute + test */
        SecHubScanConfiguration config = SecHubScanConfiguration.createFromJSON(json);
        assertNotNull(config);

        // A configuration is returned - now test some content, so it is clear it is not
        // a fall back dummy...
        Optional<SecHubDataConfiguration> data = config.getData();
        assertTrue(data.isPresent());

        List<SecHubSourceDataConfiguration> sources = data.get().getSources();
        assertEquals(1, sources.size());

        SecHubSourceDataConfiguration dataConfiguration = sources.iterator().next();
        Optional<SecHubFileSystemConfiguration> fileSystem = dataConfiguration.getFileSystem();
        assertTrue(fileSystem.isPresent());

        List<String> folders = fileSystem.get().getFolders();
        assertEquals(1, folders.size());
        assertTrue(folders.contains("myProject/source"));

    }

    @Test
    void sechub_job_config_fantasy_scan_type_only_no_official_scan_types_inside_but_can_be_read() {

        /* prepare */
        String json = TestFileReader.loadTextFile(new File("./src/test/resources/sechub_unknown_scan_type.json"));

        /* execute */
        SecHubScanConfiguration scanConfig = SecHubScanConfiguration.createFromJSON(json);

        /* test */
        assertFalse(scanConfig.getWebScan().isPresent());
        assertFalse(scanConfig.getLicenseScan().isPresent());
        assertFalse(scanConfig.getCodeScan().isPresent());
        assertFalse(scanConfig.getInfraScan().isPresent());
    }

    @Test
    void sechub_job_config_contains_data_section_when_only_fantasy_scan_type_defined() {

        /* prepare */
        String json = TestFileReader.loadTextFile(new File("./src/test/resources/sechub_unknown_scan_type.json"));

        /* execute */
        SecHubScanConfiguration scanConfig = SecHubScanConfiguration.createFromJSON(json);

        /* test */
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

    @Test
    void sechub_job_config_secret_scan_JSON_can_be_deserialized_and_contains_expected_source_data_reference() {

        /* prepare */
        String json = TestFileReader.loadTextFile(new File("./src/test/resources/sechub_secret_scan_config_source_example.json"));

        /* execute */
        SecHubScanConfiguration scanConfig = SecHubScanConfiguration.createFromJSON(json);

        /* test */
        Set<String> usedDataConfigurations = scanConfig.getSecretScan().get().getNamesOfUsedDataConfigurationObjects();
        assertEquals(1, usedDataConfigurations.size());
        assertEquals("code", usedDataConfigurations.iterator().next());
    }

    @Test
    void sechub_job_config_secret_scan_JSON_can_be_deserialized_and_contains_expected_binary_data_reference() {

        /* prepare */
        String json = TestFileReader.loadTextFile(new File("./src/test/resources/sechub_secret_scan_config_binary_example.json"));

        /* execute */
        SecHubScanConfiguration scanConfig = SecHubScanConfiguration.createFromJSON(json);

        /* test */
        Set<String> usedDataConfigurations = scanConfig.getSecretScan().get().getNamesOfUsedDataConfigurationObjects();
        assertEquals(1, usedDataConfigurations.size());
        assertEquals("build-artifacts", usedDataConfigurations.iterator().next());
    }

    @Test
    void sechub_remote_source_code_scan_configuration_with_user_credentials() {

        /* prepare */
        String json = TestFileReader.loadTextFile(new File("./src/test/resources/sechub_remote_data_config_source_code_scan_example.json"));

        /* execute + test */
        SecHubScanConfiguration config = SecHubScanConfiguration.createFromJSON(json);
        assertNotNull(config);

        // testing the remote configuration for defined codeScan
        Set<String> usedDataConfigurations = config.getCodeScan().get().getNamesOfUsedDataConfigurationObjects();
        assertEquals(1, usedDataConfigurations.size());
        assertEquals("remote_example_name", usedDataConfigurations.iterator().next());

        // testing the remote configuration for defined values
        Optional<SecHubDataConfiguration> data = config.getData();
        assertTrue(data.isPresent());

        List<SecHubSourceDataConfiguration> sources = data.get().getSources();
        assertEquals(1, sources.size());

        SecHubSourceDataConfiguration dataConfiguration = sources.iterator().next();
        Optional<SecHubRemoteDataConfiguration> remote = dataConfiguration.getRemote();
        assertTrue(remote.isPresent());

        String location = remote.get().getLocation();
        assertEquals("remote_example_location", location);
        String type = remote.get().getType();
        assertEquals("git", type);
    }

    @Test
    void sechub_remote_binary_code_scan_configuration() {

        /* prepare */
        String json = TestFileReader.loadTextFile(new File("./src/test/resources/sechub_remote_data_config_binary_code_scan_example.json"));

        /* execute + test */
        SecHubScanConfiguration config = SecHubScanConfiguration.createFromJSON(json);
        assertNotNull(config);

        // testing the remote configuration for defined codeScan
        Set<String> usedDataConfigurations = config.getCodeScan().get().getNamesOfUsedDataConfigurationObjects();
        assertEquals(1, usedDataConfigurations.size());
        assertEquals("remote_example_name", usedDataConfigurations.iterator().next());

        // testing the remote configuration for defined values
        Optional<SecHubDataConfiguration> data = config.getData();
        assertTrue(data.isPresent());

        List<SecHubBinaryDataConfiguration> binaries = data.get().getBinaries();
        assertEquals(1, binaries.size());

        SecHubBinaryDataConfiguration dataConfiguration = binaries.iterator().next();
        Optional<SecHubRemoteDataConfiguration> remote = dataConfiguration.getRemote();
        assertTrue(remote.isPresent());

        String location = remote.get().getLocation();
        assertEquals("remote_example_location", location);
        String type = remote.get().getType();
        assertEquals("docker", type);
    }
}
