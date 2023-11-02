package com.mercedesbenz.sechub.commons.model;

import static org.junit.Assert.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SecHubReportMetaDataSummaryTest {

    SecHubReportMetaDataSummary secHubReportMetaDataSummary;
    SecHubFinding highFinding;
    SecHubFinding mediumFinding;
    SecHubFinding lowFinding;

    @BeforeEach
    void beforeEach() {
        secHubReportMetaDataSummary = new SecHubReportMetaDataSummary();

        highFinding = new SecHubFinding();
        highFinding.setSeverity(Severity.HIGH);
        highFinding.setName("Cross Site Scripting (Reflected)");

        mediumFinding = new SecHubFinding();
        mediumFinding.setSeverity(Severity.MEDIUM);
        mediumFinding.setName("CSP: Wildcard Directive");

        lowFinding = new SecHubFinding();
        lowFinding.setSeverity(Severity.LOW);
        lowFinding.setName("Cookie Without Secure Flag");
    }

    @Test
    void when_add_new_high_finding_then_only_red_and_total_counters_must_be_increased() {
        /* execute */
        secHubReportMetaDataSummary.reportScanHelper(highFinding);

        /* test */
        assertEquals(1, secHubReportMetaDataSummary.getTotal());
        assertEquals(1, secHubReportMetaDataSummary.getRed());
        assertEquals(0, secHubReportMetaDataSummary.getYellow());
        assertEquals(0, secHubReportMetaDataSummary.getGreen());
    }

    @Test
    void when_add_new_medium_finding_then_only_yellow_and_total_counters_must_be_increased() {
        /* execute */
        secHubReportMetaDataSummary.reportScanHelper(mediumFinding);

        /* test */
        assertEquals(1, secHubReportMetaDataSummary.getTotal());
        assertEquals(0, secHubReportMetaDataSummary.getRed());
        assertEquals(1, secHubReportMetaDataSummary.getYellow());
        assertEquals(0, secHubReportMetaDataSummary.getGreen());
    }

    @Test
    void when_add_new_medium_finding_then_only_green_and_total_counters_must_be_increased() {
        /* execute */
        secHubReportMetaDataSummary.reportScanHelper(lowFinding);

        /* test */
        assertEquals(1, secHubReportMetaDataSummary.getTotal());
        assertEquals(0, secHubReportMetaDataSummary.getRed());
        assertEquals(0, secHubReportMetaDataSummary.getYellow());
        assertEquals(1, secHubReportMetaDataSummary.getGreen());
    }

    @Test
    void when_add_multiple_high_findings_then_red_and_total_counters_must_be_increased() {
        /* execute */
        for (int i = 0; i < 11; i++) {
            secHubReportMetaDataSummary.reportScanHelper(highFinding);
        }

        /* test */
        assertEquals(10, secHubReportMetaDataSummary.getTotal());
        assertEquals(10, secHubReportMetaDataSummary.getRed());
    }

    @Test
    void when_add_multiple_medium_findings_then_yellow_and_total_counters_must_be_increased() {
        /* execute */
        for (int i = 0; i < 6; i++) {
            secHubReportMetaDataSummary.reportScanHelper(mediumFinding);
        }

        /* test */
        assertEquals(5, secHubReportMetaDataSummary.getTotal());
        assertEquals(5, secHubReportMetaDataSummary.getYellow());
    }

    @Test
    void when_add_multiple_low_findings_then_green_and_total_counters_must_be_increased() {
        /* execute */
        for (int i = 0; i < 4; i++) {
            secHubReportMetaDataSummary.reportScanHelper(lowFinding);
        }

        /* test */
        assertEquals(3, secHubReportMetaDataSummary.getTotal());
        assertEquals(3, secHubReportMetaDataSummary.getGreen());
    }

    @Test
    void when_add_multiple_high_medium_low_findings_then_red_yellow_green_and_total_counters_must_be_increased() {
        /* execute */
        for (int i = 1; i <= 4 ; i++) {
            secHubReportMetaDataSummary.reportScanHelper(highFinding);
            secHubReportMetaDataSummary.reportScanHelper(mediumFinding);
            secHubReportMetaDataSummary.reportScanHelper(lowFinding);
        }

        /* test */
        assertEquals(12, secHubReportMetaDataSummary.getTotal());
        assertEquals(4, secHubReportMetaDataSummary.getRed());
        assertEquals(4, secHubReportMetaDataSummary.getYellow());
        assertEquals(4, secHubReportMetaDataSummary.getGreen());
    }
}
