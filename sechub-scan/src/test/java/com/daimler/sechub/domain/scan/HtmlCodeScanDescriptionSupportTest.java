package com.daimler.sechub.domain.scan;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.List;
import org.junit.Test;

import com.daimler.sechub.commons.model.SecHubCodeCallStack;
import com.daimler.sechub.commons.model.SecHubFinding;

public class HtmlCodeScanDescriptionSupportTest {
    
    private HtmlCodeScanDescriptionSupport descriptionSupport = new HtmlCodeScanDescriptionSupport();
    
    @Test
    public void test_is_code_scan_with_code_scan_finding() {

        SecHubFinding finding = new SecHubFinding();
        finding.setCode(new SecHubCodeCallStack());
        
        
        assertTrue(descriptionSupport.isCodeScan(finding));
    }
    
    @Test
    public void test_is_code_scan_with_non_code_scan_finding() {
        SecHubFinding finding = new SecHubFinding();      
        
        assertFalse(descriptionSupport.isCodeScan(finding));
    }
    
    @Test
    public void test_build_entries() {
        SecHubFinding finding = new SecHubFinding();
        
        SecHubCodeCallStack code1 = new SecHubCodeCallStack();
        code1.setLine(0);
        SecHubCodeCallStack code2 = new SecHubCodeCallStack();
        code2.setLine(1);
        SecHubCodeCallStack code3 = new SecHubCodeCallStack();
        code3.setLine(2);
        SecHubCodeCallStack code4 = new SecHubCodeCallStack();
        code4.setLine(3);
        
        List<HTMLScanResultCodeScanEntry> emptyResult = descriptionSupport.buildEntries(finding);
        
        assertTrue(emptyResult.isEmpty());
        
        finding.setCode(code1);
        code1.setCalls(code2);
        code2.setCalls(code3);
        code3.setCalls(code4);
        
        List<HTMLScanResultCodeScanEntry> fourElementsResult = descriptionSupport.buildEntries(finding);
        
        assertEquals(4, fourElementsResult.size());
                
        assertEquals(code1.getLine(), fourElementsResult.get(0).getLine());
        assertEquals(code2.getLine(), fourElementsResult.get(1).getLine());
        assertEquals(code3.getLine(), fourElementsResult.get(2).getLine());
        assertEquals(code4.getLine(), fourElementsResult.get(3).getLine());
    }
}
