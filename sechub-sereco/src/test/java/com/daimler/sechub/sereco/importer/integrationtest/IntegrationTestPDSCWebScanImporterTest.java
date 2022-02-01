// SPDX-License-Identifier: MIT
package com.daimler.sechub.sereco.importer.integrationtest;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.daimler.sechub.sereco.metadata.SerecoMetaData;
import com.daimler.sechub.sereco.metadata.SerecoVulnerability;

class IntegrationTestPDSCWebScanImporterTest {

    @Test
    void the_integrationtest_pdswebscan_importer_can_import_target_urls_and_other_parts() throws Exception {
        /* prepare */
        String descriptionOfVulnerability1 = "PDS_SCAN_TARGET_URL=https://mytargeturl.example.com/app1,PDS_TEST_KEY_VARIANTNAME=X,PRODUCT2_LEVEL=4711";
        String pdsResultText = "info:" + descriptionOfVulnerability1;
        IntegrationTestPDSCWebScanImporter importerToTest = new IntegrationTestPDSCWebScanImporter();

        /* execute */
        SerecoMetaData result = importerToTest.importResult(pdsResultText);

        /* test */
        SerecoVulnerability vulnerability = result.getVulnerabilities().iterator().next();
        String description = vulnerability.getDescription();

        assertEquals(descriptionOfVulnerability1, description);
    }

}
