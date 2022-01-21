package com.daimler.sechub.commons.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class SecHubFindingTest {

    @Test
    void nothing_defined_has_no_scantype() {
        /* prepare*/
        SecHubFinding finding = new SecHubFinding();
        
        /* test*/
        assertFalse(finding.hasScanType(null));
        assertFalse(finding.hasScanType("codescan"));
        assertFalse(finding.hasScanType("infrascan"));
        assertFalse(finding.hasScanType("webscan"));
    }
    
    @Test
    void codescan_defined_has_scantype_codescan_and_no_others() {
        /* prepare*/
        SecHubFinding finding = new SecHubFinding();
        finding.setType(ScanType.CODE_SCAN);
        
        /* test*/
        assertFalse(finding.hasScanType(null));
        assertTrue(finding.hasScanType("codescan"));
        
        assertFalse(finding.hasScanType("infrascan"));
        assertFalse(finding.hasScanType("webscan"));
    }
    
    @Test
    void codescan_defined_has_scantype_codescan_case_independant() {
        /* prepare*/
        SecHubFinding finding = new SecHubFinding();
        finding.setType(ScanType.CODE_SCAN);
        
        /* test*/
        assertTrue(finding.hasScanType("codescan"));
        assertTrue(finding.hasScanType("Codescan"));
        assertTrue(finding.hasScanType("CODESCAN"));
        assertTrue(finding.hasScanType("codeScan"));
    }
    
    @Test
    void webscan_defined_has_scantype_webscan_and_no_others() {
        /* prepare*/
        SecHubFinding finding = new SecHubFinding();
        finding.setType(ScanType.WEB_SCAN);
        
        /* test*/
        assertTrue(finding.hasScanType("webscan"));

        assertFalse(finding.hasScanType(null));
        assertFalse(finding.hasScanType("codeScan"));
        assertFalse(finding.hasScanType("infrascan"));
    }
    

    @Test
    void infrascan_defined_has_scantype_infrascan_and_no_others() {
        /* prepare*/
        SecHubFinding finding = new SecHubFinding();
        finding.setType(ScanType.INFRA_SCAN);
        
        /* test*/
        assertTrue(finding.hasScanType("infrascan"));

        assertFalse(finding.hasScanType(null));
        assertFalse(finding.hasScanType("codeScan"));
        assertFalse(finding.hasScanType("webscan"));
    }

}
