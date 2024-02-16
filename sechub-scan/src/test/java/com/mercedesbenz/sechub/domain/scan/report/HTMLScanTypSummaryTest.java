package com.mercedesbenz.sechub.domain.scan.report;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import com.mercedesbenz.sechub.commons.model.ScanType;
import com.mercedesbenz.sechub.commons.model.SecHubFinding;
import com.mercedesbenz.sechub.commons.model.Severity;
import com.mercedesbenz.sechub.domain.scan.report.HTMLScanTypSummary.HTMLScanTypeSeveritySummary;

class HTMLScanTypSummaryTest {

    @ParameterizedTest
    @EnumSource(ScanType.class)
    void scantype_from_constructor_is_used(ScanType scanType) {
        /* execute */
        HTMLScanTypSummary summary = new HTMLScanTypSummary(scanType);

        /* test */
        assertEquals(scanType, summary.getScanType());
        assertEquals(scanType.getText(), summary.getScanTypeName());

    }

    @ParameterizedTest
    @EnumSource(Severity.class)
    void getSeveritySummary_returns_never_null_for_severity(Severity severity) {
        /* prepare */
        HTMLScanTypSummary summary = new HTMLScanTypSummary(ScanType.CODE_SCAN);

        /* execute */
        HTMLScanTypeSeveritySummary severitySummary = summary.ensureSeveritySummary(severity);

        /* test */
        assertNotNull(severitySummary);

    }

    @Test
    void summary_critical_with_3_finding_summaries_has_severity_count_3() {
        /* prepare */
        HTMLScanTypSummary summary = new HTMLScanTypSummary(ScanType.CODE_SCAN);

        SecHubFinding finding1 = mock(SecHubFinding.class);
        when(finding1.getName()).thenReturn("name1");
        when(finding1.getCweId()).thenReturn(Integer.valueOf(4711));
        when(finding1.getSeverity()).thenReturn(Severity.CRITICAL);

        SecHubFinding finding2 = mock(SecHubFinding.class);
        when(finding2.getName()).thenReturn("name1");
        when(finding2.getCweId()).thenReturn(Integer.valueOf(4711));
        when(finding2.getSeverity()).thenReturn(Severity.CRITICAL);

        SecHubFinding finding3 = mock(SecHubFinding.class);
        when(finding3.getName()).thenReturn("name-other");
        when(finding3.getCweId()).thenReturn(Integer.valueOf(815));
        when(finding3.getSeverity()).thenReturn(Severity.CRITICAL);

        /* execute */
        summary.add(finding1);
        summary.add(finding2);
        summary.add(finding3);

        /* test */
        assertEquals(3, summary.getCriticalSeverityCount());
        assertEquals(0, summary.getHighSeverityCount());
        assertEquals(0, summary.getMediumSeverityCount());
        assertEquals(0, summary.getLowSeverityCount());
        assertEquals(0, summary.getUnclassifiedSeverityCount());
        assertEquals(0, summary.getInfoSeverityCount());
        assertEquals(3, summary.getTotalCount());
    }

    @Test
    void summary_low_with_4_finding_summaries_has_severity_count_3() {
        /* prepare */
        HTMLScanTypSummary summary = new HTMLScanTypSummary(ScanType.WEB_SCAN);

        SecHubFinding finding1 = mock(SecHubFinding.class);
        when(finding1.getName()).thenReturn("name1");
        when(finding1.getCweId()).thenReturn(Integer.valueOf(4711));
        when(finding1.getSeverity()).thenReturn(Severity.LOW);

        SecHubFinding finding2 = mock(SecHubFinding.class);
        when(finding2.getName()).thenReturn("name1");
        when(finding2.getCweId()).thenReturn(Integer.valueOf(4711));
        when(finding2.getSeverity()).thenReturn(Severity.LOW);

        SecHubFinding finding3 = mock(SecHubFinding.class);
        when(finding3.getName()).thenReturn("name-other");
        when(finding3.getCweId()).thenReturn(Integer.valueOf(815));
        when(finding3.getSeverity()).thenReturn(Severity.LOW);

        SecHubFinding finding4 = mock(SecHubFinding.class);
        when(finding4.getName()).thenReturn("name-other");
        when(finding4.getCweId()).thenReturn(Integer.valueOf(815));
        when(finding4.getSeverity()).thenReturn(Severity.HIGH);

        /* execute */
        summary.add(finding1);
        summary.add(finding2);
        summary.add(finding3);
        summary.add(finding4);

        /* test */
        assertEquals(0, summary.getCriticalSeverityCount());
        assertEquals(1, summary.getHighSeverityCount());
        assertEquals(0, summary.getMediumSeverityCount());
        assertEquals(3, summary.getLowSeverityCount());
        assertEquals(0, summary.getUnclassifiedSeverityCount());
        assertEquals(0, summary.getInfoSeverityCount());
        assertEquals(4, summary.getTotalCount());

    }

    @Test
    void summary_low_with_3_but_same_findingnames_summaries_has_severity_count_2() {
        /* prepare */
        HTMLScanTypSummary summary = new HTMLScanTypSummary(ScanType.WEB_SCAN);

        SecHubFinding finding1 = mock(SecHubFinding.class);
        when(finding1.getName()).thenReturn("name1");
        when(finding1.getCweId()).thenReturn(Integer.valueOf(4711));
        when(finding1.getSeverity()).thenReturn(Severity.LOW);

        SecHubFinding finding2 = mock(SecHubFinding.class);
        when(finding2.getName()).thenReturn("name1");
        when(finding2.getCweId()).thenReturn(Integer.valueOf(4711));
        when(finding2.getSeverity()).thenReturn(Severity.LOW);

        SecHubFinding finding3 = mock(SecHubFinding.class);
        when(finding3.getName()).thenReturn("name1");
        when(finding3.getCweId()).thenReturn(Integer.valueOf(4711));
        when(finding3.getSeverity()).thenReturn(Severity.LOW);

        /* execute */
        summary.add(finding1);
        summary.add(finding2);
        summary.add(finding3);

        /* test */
        assertEquals(0, summary.getCriticalSeverityCount());
        assertEquals(0, summary.getHighSeverityCount());
        assertEquals(0, summary.getMediumSeverityCount());
        assertEquals(3, summary.getLowSeverityCount());
        assertEquals(0, summary.getUnclassifiedSeverityCount());
        assertEquals(0, summary.getInfoSeverityCount());
        assertEquals(3, summary.getTotalCount());

    }

}
