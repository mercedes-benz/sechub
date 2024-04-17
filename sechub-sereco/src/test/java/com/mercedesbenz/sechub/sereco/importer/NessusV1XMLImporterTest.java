// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sereco.importer;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.sereco.ImportParameter;
import com.mercedesbenz.sechub.sereco.test.SerecoTestFileSupport;

class NessusV1XMLImporterTest {

    private NessusV1XMLImporter importerToTest;

    @BeforeEach
    void before() {
        importerToTest = new NessusV1XMLImporter();
    }

    @Test
    void xmlReportFromNessus7canBeImported() {
        /* prepare */
        String xml = SerecoTestFileSupport.INSTANCE.loadTestFile("nessus/nessus_7.0.2.result.xml");

        ImportParameter param = ImportParameter.builder().importData(xml).importId("id1").productId("Nessus").build();

        /* execute */
        boolean ableToImport = importerToTest.isAbleToImportForProduct(param);

        /* test */
        assertTrue(ableToImport, "Was not able to import xml!");
    }

}
