package com.mercedesbenz.sechub.domain.scan.report;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.commons.model.ScanType;
import com.mercedesbenz.sechub.commons.model.SecHubFinding;
import com.mercedesbenz.sechub.commons.model.Severity;

class HTMLTrafficlightScanTypeFindingDataTest {

    @Test
    void isFirstLinkItem_null_finding() {
        /* prepare */
        ScanType scanType = ScanType.CODE_SCAN;
        HTMLTrafficlightScanTypeFindingData data = new HTMLTrafficlightScanTypeFindingData(scanType);

        /* test */
        assertFalse(data.isFirstLinkItem(null));
    }
    
    @Test
    void isFirstLinkItem_other_finding() {
        /* prepare */
        ScanType scanType = ScanType.CODE_SCAN;
        HTMLTrafficlightScanTypeFindingData data = new HTMLTrafficlightScanTypeFindingData(scanType);

        SecHubFinding finding1 = new SecHubFinding();
        finding1.setId(0);
        finding1.setName("name1");
        finding1.setType(scanType);
        finding1.setSeverity(Severity.LOW);

        /* test */
        assertFalse(data.isFirstLinkItem(finding1));
    }
    
    @Test
    void isFirstLinkItem_two_low_findings_one_high() {
        /* prepare */
        ScanType scanType = ScanType.CODE_SCAN;
        HTMLTrafficlightScanTypeFindingData data = new HTMLTrafficlightScanTypeFindingData(scanType);

        SecHubFinding finding1 = new SecHubFinding();
        finding1.setId(0);
        finding1.setName("name1");
        finding1.setType(scanType);
        finding1.setSeverity(Severity.LOW);

        SecHubFinding finding2 = new SecHubFinding();
        finding2.setId(1);
        finding2.setName("name1");
        finding2.setType(scanType);
        finding2.setSeverity(Severity.LOW);

        SecHubFinding finding3 = new SecHubFinding();
        finding3.setId(2);
        finding3.setName("name1");
        finding3.setType(scanType);
        finding3.setSeverity(Severity.HIGH);

        /* execute */
        data.addRelatedFinding(finding1); /* first low finding */
        data.addRelatedFinding(finding2);
        data.addRelatedFinding(finding3); /* first high finding */

        /* test */
        assertTrue(data.isFirstLinkItem(finding1));
        assertFalse(data.isFirstLinkItem(finding2));
        assertTrue(data.isFirstLinkItem(finding3));
    }

}
