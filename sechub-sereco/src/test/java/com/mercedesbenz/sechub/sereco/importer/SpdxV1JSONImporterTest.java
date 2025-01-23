// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sereco.importer;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.commons.model.ScanType;
import com.mercedesbenz.sechub.sereco.metadata.SerecoMetaData;
import com.mercedesbenz.sechub.sereco.test.TestSerecoFileSupport;

public class SpdxV1JSONImporterTest {
    private static String spdx_2_2_scancode;
    private static String spdx_invalid_json;

    private SpdxV1JSONImporter importerToTest;

    @BeforeAll
    public static void before() {
        spdx_2_2_scancode = loadSpdxTestFile("spdx_scancode_30.1.0.spdx.json");
        spdx_invalid_json = loadSpdxTestFile("spdx_correct_header_invalid_json.txt");
    }

    @BeforeEach
    void beforeEach() {
        importerToTest = new SpdxV1JSONImporter();
    }

    @Test
    void importResult__cannot_import_null() throws IOException {
        /* prepare */
        String spdx = null;

        /* execute + test */
        assertThrows(NullPointerException.class, () -> {
            importerToTest.importResult(spdx, ScanType.LICENSE_SCAN);
        });
    }

    @Test
    void importResult__import_empty_string() throws IOException {
        /* prepare */
        String spdx = "";

        /* execute */
        SerecoMetaData metaData = importerToTest.importResult(spdx, ScanType.LICENSE_SCAN);

        /* test */
        assertTrue(metaData.getLicenseDocuments().isEmpty());
    }

    @Test
    void importResult__import_empty_json_cannot_be_imported() throws IOException {
        /* prepare */
        String spdx = "{}";

        /* execute */
        SerecoMetaData metaData = importerToTest.importResult(spdx, ScanType.LICENSE_SCAN);

        /* test */
        assertTrue(metaData.getLicenseDocuments().isEmpty());
    }

    @Test
    void importResult__just_text_cannot_be_imported() throws IOException {
        /* prepare */
        String spdx = "I am a long text …";

        /* execute */
        SerecoMetaData metaData = importerToTest.importResult(spdx, ScanType.LICENSE_SCAN);

        /* test */
        assertTrue(metaData.getLicenseDocuments().isEmpty());
    }

    @Test
    void importResult__correct_header_but_corrupt_json_cannot_be_imported() throws IOException {
        /* prepare */
        String spdx = spdx_invalid_json;

        /* execute */
        SerecoMetaData metaData = importerToTest.importResult(spdx, ScanType.LICENSE_SCAN);

        /* test */
        assertTrue(metaData.getLicenseDocuments().isEmpty());
    }

    @Test
    void importResult__import_spdx_2_2_scancode() throws IOException {
        /* prepare */
        String spdx = spdx_2_2_scancode;

        /* execute */
        SerecoMetaData metaData = importerToTest.importResult(spdx, ScanType.LICENSE_SCAN);

        /* test */
        assertNotNull(metaData.getLicenseDocuments().get(0).getSpdx());
    }

    /* ++++++++++++++++++++++++++++++++++++++++++++++++++++ */
    /* + ................Helpers......................... + */
    /* ++++++++++++++++++++++++++++++++++++++++++++++++++++ */
    private static String loadSpdxTestFile(String spdxTestFile) {
        return TestSerecoFileSupport.INSTANCE.loadTestFile("spdx/" + spdxTestFile);
    }
}
