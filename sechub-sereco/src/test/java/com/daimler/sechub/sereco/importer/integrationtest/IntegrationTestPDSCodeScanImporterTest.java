// SPDX-License-Identifier: MIT
package com.daimler.sechub.sereco.importer.integrationtest;

import static org.junit.Assert.*;

import java.util.Iterator;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.daimler.sechub.sereco.metadata.SerecoMetaData;
import com.daimler.sechub.sereco.metadata.SerecoSeverity;
import com.daimler.sechub.sereco.metadata.SerecoVulnerability;

public class IntegrationTestPDSCodeScanImporterTest {

    private IntegrationTestPDSCodeScanImporter importerToTest;

    @Before
    public void before() {
        importerToTest = new IntegrationTestPDSCodeScanImporter();
    }

    @Test
    public void when_data_contains_critical_medium_low_info__exact_this_ones_will_be_imported() throws Exception {

        /* prepare */

        /* @formatter:off */
        String data = "#PDS_INTTEST_PRODUCT_CODESCAN\n"+
                      "\n" + 
                      "\n" + 
                      "CRITICAL:i am a critical error\n" + 
                      "MEDIUM:i am a medium error\n" + 
                      "LOW:i am just a low error\n" + 
                      "INFO:i am just an information";
       /* @formatter:on */

        /* execute */
        SerecoMetaData result = importerToTest.importResult(data);

        /* test */
        List<SerecoVulnerability> v = result.getVulnerabilities();
        assertEquals(4, v.size());
        
        Iterator<SerecoVulnerability> it = v.iterator();
        
        check(SerecoSeverity.CRITICAL, 4, "i am a critical error",it.next());
        check(SerecoSeverity.MEDIUM, 5, "i am a medium error",it.next());
        check(SerecoSeverity.LOW, 6, "i am just a low error",it.next());
        check(SerecoSeverity.INFO, 7, "i am just an information",it.next());

    }
    
    private void check(SerecoSeverity expectedSeverity, int expectedLine, String description ,SerecoVulnerability critical) {
        assertEquals(expectedSeverity, critical.getSeverity());
        assertEquals(description,critical.getDescription());
        assertEquals(Integer.valueOf(expectedLine),critical.getCode().getLine());
        assertEquals(Integer.valueOf(123),critical.getCode().getColumn());
    }

}
