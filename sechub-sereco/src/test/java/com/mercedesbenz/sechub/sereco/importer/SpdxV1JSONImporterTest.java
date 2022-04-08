package com.mercedesbenz.sechub.sereco.importer;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.sereco.ImportParameter;
import com.mercedesbenz.sechub.sereco.metadata.SerecoMetaData;
import com.mercedesbenz.sechub.sereco.test.SerecoTestFileSupport;

public class SpdxV1JSONImporterTest {
    private static String spdx_2_2_scancode;
    
	private SpdxV1JSONImporter importerToTest;

    @BeforeAll
    public static void before() {
    	spdx_2_2_scancode = loadSpdxTestFile("spdx_scancode_30.1.0.spdx.json");
    }
    
    @BeforeEach
    void beforeEach() {
        importerToTest = new SpdxV1JSONImporter();
    }
    
    @Test
    void importResult__import_empty_string() throws IOException {
        /* prepare */
    	String spdx = "";

        /* execute */
        SerecoMetaData metaData = importerToTest.importResult(spdx);

        /* test */
        assertTrue(metaData.getLicenseDocuments().isEmpty());
    }
    
    @Test
    void importResult__import_spdx_2_2_scancode() throws IOException {
        /* prepare */
    	String spdx = spdx_2_2_scancode;

        /* execute */
        SerecoMetaData metaData = importerToTest.importResult(spdx);

        /* test */
        assertNotNull(metaData.getLicenseDocuments().get(0).getSpdx());
    }
    
    @Test
    void createImportSupport__import_spdx_scancode_codescan() {
        /* prepare */

        ImportParameter paramScancode = ImportParameter.builder().importData(spdx_2_2_scancode).importId("id1").productId("PDS_CODESCAN").build();

        /* execute */
        ProductImportAbility ableToImportScancodeSpdx = importerToTest.isAbleToImportForProduct(paramScancode);

        /* test */
        assertEquals(ProductImportAbility.ABLE_TO_IMPORT, ableToImportScancodeSpdx, "Was NOT able to import SPDX JSON!");
    }
    
    @Test
    void createImportSupport__import_spdx_scancode_binaryscan() {
        /* prepare */

        ImportParameter paramScancode = ImportParameter.builder().importData(spdx_2_2_scancode).importId("id1").productId("PDS_BINARYSCAN").build();

        /* execute */
        ProductImportAbility ableToImportScancodeSpdx = importerToTest.isAbleToImportForProduct(paramScancode);

        /* test */
        assertEquals(ProductImportAbility.ABLE_TO_IMPORT, ableToImportScancodeSpdx, "Was NOT able to import SPDX JSON!");
    }

    /* ++++++++++++++++++++++++++++++++++++++++++++++++++++ */
    /* + ................Helpers......................... + */
    /* ++++++++++++++++++++++++++++++++++++++++++++++++++++ */
    private static String loadSpdxTestFile(String spdxTestFile) {
        return SerecoTestFileSupport.INSTANCE.loadTestFile("spdx/" + spdxTestFile);
    }
}
