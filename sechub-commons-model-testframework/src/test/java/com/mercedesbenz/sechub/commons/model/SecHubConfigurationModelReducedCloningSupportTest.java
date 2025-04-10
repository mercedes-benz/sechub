// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * This test is not inside sechub-commons-model because we use
 * TestSecHubConfigurationBuilder and we could use this inside
 * sechub-commons-model because there would be a cyclic dependency. So we settle
 * this test inside this project
 *
 * @author Albert Tregnaghi
 *
 */
class SecHubConfigurationModelReducedCloningSupportTest {

    private SecHubConfigurationModelReducedCloningSupport toTest;

    @BeforeEach
    void beforeEach() {
        toTest = new SecHubConfigurationModelReducedCloningSupport();
    }

    @Test
    void configuration_having_code_infra_and_webs_config_parts__target_is_analytics_all_contained() throws Exception {

        /* prepare */

        /* @formatter:off */
        SecHubScanConfiguration config = TestSecHubConfigurationBuilder.
                configureSecHub().
                    webConfig().login("https://login.example.com/login").
                                    basic("testuser","pseudopwd").
                                and().
                    codeScanConfig().setFileSystemFolders("folder1","folder2").
                                and().
                    infraConfig().addURI("https://testinfra.example.com").

                build();
        /* @formatter:on */

        // check preconditions
        assertTrue(config.getWebScan().isPresent());
        assertTrue(config.getInfraScan().isPresent());
        assertTrue(config.getCodeScan().isPresent());

        /* execute */
        String json = toTest.createReducedScanConfigurationCloneJSON(config, ScanType.ANALYTICS);

        /* test */
        SecHubScanConfiguration resultClone = SecHubScanConfiguration.createFromJSON(json);
        assertTrue(resultClone.getCodeScan().isPresent());
        assertTrue(resultClone.getWebScan().isPresent());
        assertTrue(resultClone.getInfraScan().isPresent());
    }

    @Test
    void configuration_having_code_iac_and_web_config_parts__target_is_iacscan() throws Exception {

        /* prepare */

        /* @formatter:off */
        SecHubScanConfiguration config = TestSecHubConfigurationBuilder.
                configureSecHub().
                    webConfig().login("https://login.example.com/login").
                                    basic("testuser","pseudopwd").
                                and().
                    codeScanConfig().setFileSystemFolders("folder1","folder2").
                                and().
                    iacScanConfig().useDataReferences("ref1").

                build();
        /* @formatter:on */

        // check preconditions
        assertTrue(config.getCodeScan().isPresent());
        assertTrue(config.getWebScan().isPresent());
        assertTrue(config.getIacScan().isPresent());

        /* execute */
        String json = toTest.createReducedScanConfigurationCloneJSON(config, ScanType.IAC_SCAN);

        /* test */
        SecHubScanConfiguration resultClone = SecHubScanConfiguration.createFromJSON(json);
        assertFalse(resultClone.getCodeScan().isPresent());
        assertFalse(resultClone.getWebScan().isPresent());
        assertTrue(resultClone.getIacScan().isPresent());
    }

    @Test
    void configuration_having_infra_code_and_webs_config_parts__target_is_webscan() throws Exception {

        /* prepare */

        /* @formatter:off */
        SecHubScanConfiguration config = TestSecHubConfigurationBuilder.
                configureSecHub().
                    webConfig().login("https://login.example.com/login").
                                    basic("testuser","pseudopwd").
                                and().
                    codeScanConfig().setFileSystemFolders("folder1","folder2").
                                and().
                    infraConfig().addURI("https://testinfra.example.com").

                build();
        /* @formatter:on */

        // check preconditions
        assertTrue(config.getWebScan().isPresent());
        assertTrue(config.getInfraScan().isPresent());
        assertTrue(config.getCodeScan().isPresent());

        /* execute */
        String json = toTest.createReducedScanConfigurationCloneJSON(config, ScanType.WEB_SCAN);

        /* test */
        SecHubScanConfiguration resultClone = SecHubScanConfiguration.createFromJSON(json);
        assertTrue(resultClone.getWebScan().isPresent());
        assertFalse(resultClone.getCodeScan().isPresent());
        assertFalse(resultClone.getInfraScan().isPresent());
    }

    @Test
    void configuration_having_infra_code_and_webs_config_parts__target_is_infrascan_and_data_present() throws Exception {

        /* prepare */

        /* @formatter:off */
        SecHubScanConfiguration config = TestSecHubConfigurationBuilder.
                configureSecHub().
                    webConfig().login("https://login.example.com/login").
                                    basic("testuser","pseudopwd").
                                and().
                    codeScanConfig().setFileSystemFolders("folder1","folder2").
                                and().
                    infraConfig().addURI("https://testinfra.example.com").

                build();
        /* @formatter:on */

        // check preconditions
        assertTrue(config.getWebScan().isPresent());
        assertTrue(config.getInfraScan().isPresent());
        assertTrue(config.getCodeScan().isPresent());

        /* execute */
        String json = toTest.createReducedScanConfigurationCloneJSON(config, ScanType.INFRA_SCAN);

        /* test */
        SecHubScanConfiguration resultClone = SecHubScanConfiguration.createFromJSON(json);
        assertTrue(resultClone.getInfraScan().isPresent());
        assertFalse(resultClone.getWebScan().isPresent());
        assertFalse(resultClone.getCodeScan().isPresent());
    }

    @Test
    void configuration_having_no_infra_scan_but_wanted_after_clone_fallback_infra_scan_present() throws Exception {

        /* prepare */

        /* @formatter:off */
        SecHubScanConfiguration config = TestSecHubConfigurationBuilder.
                configureSecHub().
                build();
        /* @formatter:on */

        // check preconditions
        assertFalse(config.getWebScan().isPresent());
        assertFalse(config.getInfraScan().isPresent());
        assertFalse(config.getCodeScan().isPresent());

        /* execute */
        String json = toTest.createReducedScanConfigurationCloneJSON(config, ScanType.INFRA_SCAN);

        /* test */
        SecHubScanConfiguration resultClone = SecHubScanConfiguration.createFromJSON(json);
        assertTrue(resultClone.getInfraScan().isPresent());
        assertFalse(resultClone.getWebScan().isPresent());
        assertFalse(resultClone.getCodeScan().isPresent());
    }

    @Test
    void configuration_having_data_only_after_clone_data_present_as_before() throws Exception {

        /* prepare */

        /* @formatter:off */
        SecHubScanConfiguration config = TestSecHubConfigurationBuilder.
                configureSecHub().
                    data().
                        withBinary().
                           uniqueName("unique1").
                           fileSystemFolders("folder1").
                        end().
                    and().
                build();
        /* @formatter:on */

        // check preconditions
        assertTrue(config.getData().isPresent());

        /* execute */
        String json = toTest.createReducedScanConfigurationCloneJSON(config, ScanType.INFRA_SCAN);

        /* test */
        SecHubScanConfiguration resultClone = SecHubScanConfiguration.createFromJSON(json);
        assertTrue(resultClone.getData().isPresent());
        SecHubDataConfiguration data = resultClone.getData().get();
        List<SecHubBinaryDataConfiguration> binaries = data.getBinaries();
        assertEquals(1, binaries.size());
        SecHubBinaryDataConfiguration binary = binaries.get(0);
        assertEquals("unique1", binary.getUniqueName());
        assertTrue(binary.getFileSystem().isPresent());
        SecHubFileSystemConfiguration fileSystem = binary.getFileSystem().get();
        assertEquals(1, fileSystem.getFolders().size());
        String folder = fileSystem.getFolders().iterator().next();
        assertEquals("folder1", folder);

    }

}
