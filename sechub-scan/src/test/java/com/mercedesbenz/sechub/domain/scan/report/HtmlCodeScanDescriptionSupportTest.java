// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.report;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.commons.model.SecHubCodeCallStack;
import com.mercedesbenz.sechub.commons.model.SecHubFinding;

public class HtmlCodeScanDescriptionSupportTest {

    private HTMLCodeScanDescriptionSupport descriptionSupport = new HTMLCodeScanDescriptionSupport();

    @Test
    void test_is_code_scan_with_code_scan_finding() {

        /* prepare */
        SecHubFinding finding = new SecHubFinding();
        finding.setCode(new SecHubCodeCallStack());

        /* test */
        assertTrue(descriptionSupport.isCodeScan(finding));
    }

    @Test
    void test_is_code_scan_with_non_code_scan_finding() {

        /* prepare */
        SecHubFinding finding = new SecHubFinding();

        /* test */
        assertFalse(descriptionSupport.isCodeScan(finding));
    }

    @Test
    void build_entries__creates_NO_html_scan_entries_when_finding_has_no_code() {

        /* prepare */
        SecHubFinding finding = new SecHubFinding();

        /* execute */
        List<HTMLScanResultCodeScanEntry> emptyResult = descriptionSupport.buildEntries(finding);

        /* test */
        assertTrue(emptyResult.isEmpty());
    }

    @Test
    void build_entries__creates_html_scan_entries_with_correct_linenumbers() {

        /* prepare */
        SecHubFinding finding = new SecHubFinding();

        SecHubCodeCallStack code1 = new SecHubCodeCallStack();
        code1.setLine(0);
        SecHubCodeCallStack code2 = new SecHubCodeCallStack();
        code2.setLine(1);
        SecHubCodeCallStack code3 = new SecHubCodeCallStack();
        code3.setLine(2);
        SecHubCodeCallStack code4 = new SecHubCodeCallStack();
        code4.setLine(3);

        finding.setCode(code1);
        code1.setCalls(code2);
        code2.setCalls(code3);
        code3.setCalls(code4);

        /* execute */
        List<HTMLScanResultCodeScanEntry> fourElementsResult = descriptionSupport.buildEntries(finding);

        /* test */
        assertEquals(4, fourElementsResult.size());
        assertEquals(code1.getLine(), fourElementsResult.get(0).getLine());
        assertEquals(code2.getLine(), fourElementsResult.get(1).getLine());
        assertEquals(code3.getLine(), fourElementsResult.get(2).getLine());
        assertEquals(code4.getLine(), fourElementsResult.get(3).getLine());

    }

    @Test
    void build_entries__creates_html_scan_entries_with_correct_callnumbers() {

        /* prepare */
        SecHubFinding finding = new SecHubFinding();

        SecHubCodeCallStack code1 = new SecHubCodeCallStack();
        SecHubCodeCallStack code2 = new SecHubCodeCallStack();
        SecHubCodeCallStack code3 = new SecHubCodeCallStack();
        SecHubCodeCallStack code4 = new SecHubCodeCallStack();

        finding.setCode(code1);
        code1.setCalls(code2);
        code2.setCalls(code3);
        code3.setCalls(code4);

        /* execute */
        List<HTMLScanResultCodeScanEntry> fourElementsResult = descriptionSupport.buildEntries(finding);

        /* test */
        assertEquals(4, fourElementsResult.size());

        assertEquals(1, fourElementsResult.get(0).getCallNumber());
        assertEquals(2, fourElementsResult.get(1).getCallNumber());
        assertEquals(3, fourElementsResult.get(2).getCallNumber());
        assertEquals(4, fourElementsResult.get(3).getCallNumber());

    }
}
