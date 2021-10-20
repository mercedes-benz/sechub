package com.daimler.sechub.commons.model;

import static org.junit.jupiter.api.Assertions.*;

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
    public void configuration_having_infra_code_and_webs_config_parts__target_is_codescan() throws Exception {

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
        SecHubScanConfiguration resultClone = toTest.createReducedScanConfigurationClone(config, ScanType.CODE_SCAN);

        /* test */
        assertTrue(resultClone.getCodeScan().isPresent());
        assertFalse(resultClone.getWebScan().isPresent());
        assertFalse(resultClone.getInfraScan().isPresent());
    }
    
    @Test
    public void configuration_having_infra_code_and_webs_config_parts__target_is_webscan() throws Exception {

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
        SecHubScanConfiguration resultClone = toTest.createReducedScanConfigurationClone(config, ScanType.WEB_SCAN);

        /* test */
        assertTrue(resultClone.getWebScan().isPresent());
        assertFalse(resultClone.getCodeScan().isPresent());
        assertFalse(resultClone.getInfraScan().isPresent());
    }
    
    @Test
    public void configuration_having_infra_code_and_webs_config_parts__target_is_infrascan() throws Exception {

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
        SecHubScanConfiguration resultClone = toTest.createReducedScanConfigurationClone(config, ScanType.INFRA_SCAN);

        /* test */
        assertTrue(resultClone.getInfraScan().isPresent());
        assertFalse(resultClone.getWebScan().isPresent());
        assertFalse(resultClone.getCodeScan().isPresent());
    }

}
