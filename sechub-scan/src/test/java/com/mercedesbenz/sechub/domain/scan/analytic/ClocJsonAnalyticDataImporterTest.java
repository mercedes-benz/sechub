// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.analytic;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import com.mercedesbenz.sechub.sharedkernel.analytic.CodeAnalyticData;
import com.mercedesbenz.sechub.test.TestFileReader;

class ClocJsonAnalyticDataImporterTest {

    private ClocJsonAnalyticDataImporter importerToTest;
    private static String sechubClocJSON;
    private static String gosecClocJSON;

    @BeforeAll
    static void beforeAll() {
        sechubClocJSON = TestFileReader.readTextFromFile(new File("./src/test/resources/cloc/cloc-sechub.json"));
        gosecClocJSON = TestFileReader.readTextFromFile(new File("./src/test/resources/cloc/cloc-gosec.json"));
    }

    @BeforeEach
    void beforeEach() {
        importerToTest = new ClocJsonAnalyticDataImporter();
    }

    @Test
    void is_able_to_import_returns_true_for_sechub_cloc_json_example() throws IOException {
        assertTrue(importerToTest.isAbleToImport(sechubClocJSON));
    }

    @Test
    void is_able_to_import_returns_true_for_gosec_cloc_json_example() throws IOException {
        assertTrue(importerToTest.isAbleToImport(gosecClocJSON));
    }

    @NullSource
    @ValueSource(strings = { "", "{}", "{cloc}" })
    @ParameterizedTest
    void is_able_to_import_returns_false_for_unsupported_content(String unsupportedContent) throws IOException {
        assertFalse(importerToTest.isAbleToImport(unsupportedContent));
    }

    @Test
    void sechub_cloc_json_can_be_imported_and_contains_expected_data() throws IOException {

        /* execute */
        CodeAnalyticData codeAnalyticData = importerToTest.importData(sechubClocJSON);

        /* test */
        assertNotNull(codeAnalyticData);

        Set<String> languages = codeAnalyticData.getLanguages();
        assertEquals(35, languages.size());

        assertTrue(languages.contains("java"));
        assertTrue(languages.contains("go"));
        assertTrue(languages.contains("html"));

        assertEquals("CLOC", codeAnalyticData.getProductInfo().getName());
        assertEquals("1.96", codeAnalyticData.getProductInfo().getVersion());

        assertEquals(2337, codeAnalyticData.getFilesForLanguage("java"));
        assertEquals(126190, codeAnalyticData.getLinesOfCodeForLanguage("java"));

        assertEquals(51, codeAnalyticData.getFilesForLanguage("go"));
        assertEquals(4151, codeAnalyticData.getLinesOfCodeForLanguage("go"));

        assertEquals(3474, codeAnalyticData.calculateFilesForAllLanguages());
        assertEquals(584644, codeAnalyticData.calculateLinesOfCodeForAllLanguages());

    }

}
