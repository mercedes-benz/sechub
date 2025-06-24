// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.project;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.commons.model.ScanType;
import com.mercedesbenz.sechub.commons.model.SecHubCodeCallStack;
import com.mercedesbenz.sechub.commons.model.SecHubFinding;
import com.mercedesbenz.sechub.commons.model.web.SecHubReportWeb;
import com.mercedesbenz.sechub.commons.model.web.SecHubReportWebEvidence;
import com.mercedesbenz.sechub.commons.model.web.SecHubReportWebRequest;
import com.mercedesbenz.sechub.commons.model.web.SecHubReportWebResponse;

class FalsePositiveMetaDataFactoryTest {

    private FalsePositiveMetaDataFactory factoryToTest;

    @BeforeEach
    void beforeEach() {
        factoryToTest = new FalsePositiveMetaDataFactory();

    }

    @Test
    void code_scan_only_one_entry_factory_code_start_set_but_end_is_null() {
        /* prepare */
        SecHubFinding codeScanfinding = createCodeFindingOnlyOneCallStackElementOnly();

        /* execute */
        FalsePositiveMetaData metaData = factoryToTest.createMetaData(codeScanfinding);

        /* test */
        FalsePositiveCodeMetaData code = metaData.getCode();
        assertNotNull(code.getStart());
        assertNull(code.getEnd());

    }

    @Test
    void iac_scan_only_one_entry_factory_code_start_set_but_end_is_null() {
        /* prepare */
        SecHubFinding iacScanFinding = createIacFindingOnlyOneCallStackElementOnly();

        /* execute */
        FalsePositiveMetaData metaData = factoryToTest.createMetaData(iacScanFinding);

        /* test */
        FalsePositiveCodeMetaData code = metaData.getCode();
        assertNotNull(code.getStart());
        assertNull(code.getEnd());

    }

    @Test
    void code_scan_finding_transformed_to_relevant_code_metadata() {
        SecHubFinding codeScanfinding = createCodeFinding();

        /* execute */
        FalsePositiveMetaData metaData = factoryToTest.createMetaData(codeScanfinding);

        /* test */
        assertEquals(4211, metaData.getCweId());
        assertNull(metaData.getCveId()); // we do not expect a CVE id here, even when set in report

        assertEquals(ScanType.CODE_SCAN, metaData.getScanType());
        assertNull(metaData.getWeb());

        FalsePositiveCodeMetaData code = metaData.getCode();
        assertNotNull(code);

        // relevant code snippets and call hierarchy check:
        FalsePositiveCodePartMetaData start = code.getStart();
        assertEquals("relevant-part-start", start.getRelevantPart());
        assertEquals("location-start", start.getLocation());
        assertEquals("source-start", start.getSourceCode());

        FalsePositiveCodePartMetaData end = code.getEnd();
        assertEquals("relevant-part-end", end.getRelevantPart());
        assertEquals("location-end", end.getLocation());
        assertEquals("source-end", end.getSourceCode());
    }

    @Test
    void web_scan_finding_transformed_to_relevant_web_metadata() {
        SecHubFinding webScanfinding = createWebFinding();
        /* execute */
        FalsePositiveMetaData metaData = factoryToTest.createMetaData(webScanfinding);

        /* test */
        assertEquals(ScanType.WEB_SCAN, metaData.getScanType());
        assertEquals(4211, metaData.getCweId());
        assertEquals("CVE-4211", metaData.getCveId()); // most times a CVE makes more sense in infrastructure scans, but maybe possible

        assertNull(metaData.getCode());

        FalsePositiveWebMetaData web = metaData.getWeb();
        assertNotNull(web);

        FalsePositiveWebRequestMetaData request = web.getRequest();
        assertEquals("attack-vector1", request.getAttackVector());
        assertEquals("method1", request.getMethod());
        assertEquals("protocol1", request.getProtocol());
        assertEquals("target1", request.getTarget());
        assertEquals("version1", request.getVersion());

        FalsePositiveWebResponseMetaData response = web.getResponse();
        assertEquals("evidence-snippet1", response.getEvidence());
        assertEquals(4211, response.getStatusCode());

    }

    private SecHubFinding createIacFindingOnlyOneCallStackElementOnly() {
        SecHubFinding result = createCodeFindingOnlyOneCallStackElementOnly();
        result.setType(ScanType.IAC_SCAN);
        return result;
    }

    private SecHubFinding createCodeFindingOnlyOneCallStackElementOnly() {
        SecHubFinding finding = createTestFinding();
        SecHubCodeCallStack codeStart = new SecHubCodeCallStack();
        codeStart.setRelevantPart("relevant-part-start");
        codeStart.setLocation("location-start");
        codeStart.setSource("source-start");
        codeStart.setSource("source-start");

        finding.setCode(codeStart);
        finding.setType(ScanType.CODE_SCAN);
        return finding;
    }

    private SecHubFinding createCodeFinding() {
        SecHubFinding finding = createTestFinding();
        SecHubCodeCallStack codeStart = new SecHubCodeCallStack();
        codeStart.setRelevantPart("relevant-part-start");
        codeStart.setLocation("location-start");
        codeStart.setSource("source-start");
        codeStart.setSource("source-start");

        SecHubCodeCallStack codeMiddle = new SecHubCodeCallStack();
        codeMiddle.setRelevantPart("relevant-part-middle");

        SecHubCodeCallStack codeEnd = new SecHubCodeCallStack();
        codeEnd.setRelevantPart("relevant-part-end");
        codeEnd.setLocation("location-end");
        codeEnd.setSource("source-end");

        codeStart.setCalls(codeMiddle);
        codeMiddle.setCalls(codeEnd);

        finding.setCode(codeStart);
        finding.setType(ScanType.CODE_SCAN);
        return finding;
    }

    private SecHubFinding createWebFinding() {
        SecHubFinding finding = createTestFinding();
        SecHubReportWeb web = new SecHubReportWeb();
        finding.setWeb(web);
        finding.setType(ScanType.WEB_SCAN);

        SecHubReportWebRequest request = web.getRequest();
        request.setMethod("method1");
        request.setTarget("target1");
        request.setProtocol("protocol1");
        request.setVersion("version1");

        SecHubReportWebResponse response = web.getResponse();
        response.setStatusCode(4211);

        // attack
        SecHubReportWebEvidence evidence = new SecHubReportWebEvidence();
        evidence.setSnippet("evidence-snippet1");
        web.getAttack().setEvidence(evidence);
        web.getAttack().setVector("attack-vector1");

        return finding;
    }

    private SecHubFinding createTestFinding() {

        SecHubFinding finding = new SecHubFinding();
        finding.setCweId(4211);
        finding.setCveId("CVE-4211");
        return finding;
    }

}
