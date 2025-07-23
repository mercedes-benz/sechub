package com.mercedesbenz.sechub.wrapper.infralight.product.sslscan;

import static org.assertj.core.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.commons.model.interchange.GenericInfrascanFinding;
import com.mercedesbenz.sechub.test.TestFileReader;

class SSLScanProductImporterTest {

    private SSLScanProductImporter importerToTest;

    @BeforeEach
    void beforeEach() {
        importerToTest = new SSLScanProductImporter();
    }

    @Test
    void name_is_defined() {
        assertThat(importerToTest.getProductName()).isNotNull().isNotBlank();
    }

    @Test
    void import_filename_is_defined() {
        assertThat(importerToTest.getImportFileName()).isNotNull().isNotBlank();
    }
    
    @Test
    void productfoler1_test() {
        
        /* prepare */
        String data = TestFileReader.readTextFromFile("./src/test/resources/product-output-testfolder-1/sslscan-output.xml");
        
        /* execute */
        List<GenericInfrascanFinding> imported = importerToTest.startImport(data);
        
        /* test */
        
    }

}
