package com.mercedesbenz.sechub.wrapper.infralight.product.nmap;

import static org.assertj.core.api.Assertions.*;

import java.util.Iterator;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.commons.model.interchange.GenericInfrascanFinding;
import com.mercedesbenz.sechub.test.TestFileReader;

class NmapPortScanProductImporterTest {

    private NmapPortScanProductImporter importerToTest;

    @BeforeEach
    void beforeEach() {
        importerToTest = new NmapPortScanProductImporter();
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
    void productfolder1_test() {
        
        /* prepare */
        String data = TestFileReader.readTextFromFile("./src/test/resources/product-output-testfolder-1/nmap_portscan-output.xml");
        
        /* execute */
        List<GenericInfrascanFinding> imported = importerToTest.startImport(data);
        
        /* test */
        assertThat(imported).isNotNull().isNotEmpty().hasSize(24);
        Iterator<GenericInfrascanFinding> it = imported.iterator();
        GenericInfrascanFinding finding1 = it.next();
        
        assertThat(finding1.getName()).contains("Open port detected");
        assertThat(finding1.getDescription()).contains("IP:10.0.0.1","tcp port 135","Service: msrpc");
    }
}
