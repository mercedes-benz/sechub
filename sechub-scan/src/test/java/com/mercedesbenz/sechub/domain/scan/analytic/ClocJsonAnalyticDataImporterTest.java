package com.mercedesbenz.sechub.domain.scan.analytic;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.sharedkernel.analytic.AnalyticData;
import com.mercedesbenz.sechub.sharedkernel.analytic.CodeAnalyticData;
import com.mercedesbenz.sechub.test.TestFileReader;

class ClocJsonAnalyticDataImporterTest {

    private ClocJsonAnalyticDataImporter importerToTest;
    private static String sechubClocJSON;

    @BeforeAll
    static void beforeAll() {
        sechubClocJSON = TestFileReader.loadTextFile(new File("./src/test/resources/cloc/cloc-sechub.json"));
    }

    @BeforeEach
    void beforeEach() {
        importerToTest = new ClocJsonAnalyticDataImporter();
    }

    @Test
    void sechub_cloc_json_can_be_imported_and_contains_expected_data() throws IOException {

        /* execute */
        AnalyticData result = importerToTest.importData(sechubClocJSON);

        /* test */
        CodeAnalyticData codeAnalyticData = result.getCodeAnalyticData();
        assertNotNull(codeAnalyticData);

        Set<String> languages = codeAnalyticData.getLanguages();
        assertEquals(35, languages.size());

        assertTrue(languages.contains("java"));
        assertTrue(languages.contains("go"));
        assertTrue(languages.contains("html"));

        assertEquals("CLOC", codeAnalyticData.getProductData().getName());
        assertEquals("1.96", codeAnalyticData.getProductData().getVersion());

        assertEquals(2337, codeAnalyticData.getFilesForLanguage("java"));
        assertEquals(126190, codeAnalyticData.getLinesOfCodeForLanguage("java"));

        assertEquals(51, codeAnalyticData.getFilesForLanguage("go"));
        assertEquals(4151, codeAnalyticData.getLinesOfCodeForLanguage("go"));

        assertEquals(3474, codeAnalyticData.calculateFilesForAllLanguages());
        assertEquals(584644, codeAnalyticData.calculateLinesOfCodeForAllLanguages());

    }

}
