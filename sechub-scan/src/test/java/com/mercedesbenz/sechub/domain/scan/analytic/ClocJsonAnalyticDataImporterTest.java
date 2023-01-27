package com.mercedesbenz.sechub.domain.scan.analytic;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.sharedkernel.analytic.AnalyticData;
import com.mercedesbenz.sechub.sharedkernel.analytic.CodeAnalyticData;
import com.mercedesbenz.sechub.test.TestFileReader;

class ClocJsonAnalyticDataImporterTest {

    private ClocJsonAnalyticDataImporter importerToTest;
    private String sechubClocJSON;

    @BeforeAll
    void beforeAll() {
        sechubClocJSON = TestFileReader.loadTextFile(new File("./src/test/cloc/cloc-sechub.json"));
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
        CodeAnalyticData linesOfCode = result.getCodeAnalyticData();
        assertNotNull(linesOfCode);

        assertEquals(3474L, linesOfCode.getAmountOfFiles());
        assertEquals(22, linesOfCode.getLanguages().size());

    }

}
