package com.mercedesbenz.sechub.commons.model;

import static org.junit.Assert.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SecHubReportMetaDataSummaryTest {

    SecHubReportMetaDataSummary secHubReportMetaDataSummary;
    SecHubFinding highFinding, criticalFinding;
    SecHubFinding mediumFinding;
    SecHubFinding infoFinding, unclassifiedFinding, lowFinding;

    @BeforeEach
    void beforeEach() {
        secHubReportMetaDataSummary = new SecHubReportMetaDataSummary();

        criticalFinding = new SecHubFinding();
        criticalFinding.setSeverity(Severity.CRITICAL);
        criticalFinding.setName("Critical name");

        highFinding = new SecHubFinding();
        highFinding.setSeverity(Severity.HIGH);
        highFinding.setName("Cross Site Scripting (Reflected)");

        mediumFinding = new SecHubFinding();
        mediumFinding.setSeverity(Severity.MEDIUM);
        mediumFinding.setName("CSP: Wildcard Directive");

        lowFinding = new SecHubFinding();
        lowFinding.setSeverity(Severity.LOW);
        lowFinding.setName("Cookie Without Secure Flag");

        infoFinding = new SecHubFinding();
        infoFinding.setSeverity(Severity.INFO);
        infoFinding.setName("Info name");

        unclassifiedFinding = new SecHubFinding();
        unclassifiedFinding.setSeverity(Severity.UNCLASSIFIED);
        unclassifiedFinding.setName("Unclassified name");
    }

    @Test
    void when_add_new_critical_finding_then_only_red_and_total_counters_must_be_increased() {
        /* execute */
        secHubReportMetaDataSummary.reportScanHelper(criticalFinding);

        /* test */
        assertEquals(1, secHubReportMetaDataSummary.getTotal());
        assertEquals(1, secHubReportMetaDataSummary.getRed());
        assertEquals(0, secHubReportMetaDataSummary.getYellow());
        assertEquals(0, secHubReportMetaDataSummary.getGreen());
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
    void when_add_new_low_finding_then_only_green_and_total_counters_must_be_increased() {
        /* execute */
        secHubReportMetaDataSummary.reportScanHelper(lowFinding);

        /* test */
        assertEquals(1, secHubReportMetaDataSummary.getTotal());
        assertEquals(0, secHubReportMetaDataSummary.getRed());
        assertEquals(0, secHubReportMetaDataSummary.getYellow());
        assertEquals(1, secHubReportMetaDataSummary.getGreen());
    }

    @Test
    void when_add_new_info_finding_then_only_green_and_total_counters_must_be_increased() {
        /* execute */
        secHubReportMetaDataSummary.reportScanHelper(infoFinding);

        /* test */
        assertEquals(1, secHubReportMetaDataSummary.getTotal());
        assertEquals(0, secHubReportMetaDataSummary.getRed());
        assertEquals(0, secHubReportMetaDataSummary.getYellow());
        assertEquals(1, secHubReportMetaDataSummary.getGreen());
    }

    @Test
    void when_add_new_unclassified_finding_then_only_green_and_total_counters_must_be_increased() {
        /* execute */
        secHubReportMetaDataSummary.reportScanHelper(unclassifiedFinding);

        /* test */
        assertEquals(1, secHubReportMetaDataSummary.getTotal());
        assertEquals(0, secHubReportMetaDataSummary.getRed());
        assertEquals(0, secHubReportMetaDataSummary.getYellow());
        assertEquals(1, secHubReportMetaDataSummary.getGreen());
    }

    @Test
    void when_add_multiple_critical_findings_then_red_and_total_counters_must_be_increased() {
        /* execute */
        for (int i = 0; i < 9; i++) {
            secHubReportMetaDataSummary.reportScanHelper(criticalFinding);
        }

        /* test */
        assertEquals(9, secHubReportMetaDataSummary.getTotal());
        assertEquals(9, secHubReportMetaDataSummary.getRed());
    }

    @Test
    void when_add_multiple_high_findings_then_red_and_total_counters_must_be_increased() {
        /* execute */
        for (int i = 0; i < 11; i++) {
            secHubReportMetaDataSummary.reportScanHelper(highFinding);
        }

        /* test */
        assertEquals(11, secHubReportMetaDataSummary.getTotal());
        assertEquals(11, secHubReportMetaDataSummary.getRed());
    }

    @Test
    void when_add_multiple_medium_findings_then_yellow_and_total_counters_must_be_increased() {
        /* execute */
        for (int i = 0; i < 6; i++) {
            secHubReportMetaDataSummary.reportScanHelper(mediumFinding);
        }

        /* test */
        assertEquals(6, secHubReportMetaDataSummary.getTotal());
        assertEquals(6, secHubReportMetaDataSummary.getYellow());
    }

    @Test
    void when_add_multiple_low_findings_then_green_and_total_counters_must_be_increased() {
        /* execute */
        for (int i = 0; i < 4; i++) {
            secHubReportMetaDataSummary.reportScanHelper(lowFinding);
        }

        /* test */
        assertEquals(4, secHubReportMetaDataSummary.getTotal());
        assertEquals(4, secHubReportMetaDataSummary.getGreen());
    }

    @Test
    void when_add_multiple_info_findings_then_green_and_total_counters_must_be_increased() {
        /* execute */
        for (int i = 0; i < 3; i++) {
            secHubReportMetaDataSummary.reportScanHelper(infoFinding);
        }

        /* test */
        assertEquals(3, secHubReportMetaDataSummary.getTotal());
        assertEquals(3, secHubReportMetaDataSummary.getGreen());
    }

    @Test
    void when_add_multiple_unclassified_findings_then_green_and_total_counters_must_be_increased() {
        /* execute */
        for (int i = 0; i < 5; i++) {
            secHubReportMetaDataSummary.reportScanHelper(unclassifiedFinding);
        }

        /* test */
        assertEquals(5, secHubReportMetaDataSummary.getTotal());
        assertEquals(5, secHubReportMetaDataSummary.getGreen());
    }

    @Test
    void when_add_multiple_critical_high_medium_low_info_unclassified_findings_then_red_yellow_green_and_total_counters_must_be_increased() {
        /* execute */
        for (int i = 0; i < 4; i++) {
            secHubReportMetaDataSummary.reportScanHelper(criticalFinding);
            secHubReportMetaDataSummary.reportScanHelper(highFinding);
            secHubReportMetaDataSummary.reportScanHelper(mediumFinding);
            secHubReportMetaDataSummary.reportScanHelper(lowFinding);
            secHubReportMetaDataSummary.reportScanHelper(infoFinding);
            secHubReportMetaDataSummary.reportScanHelper(unclassifiedFinding);
        }

        /* test */
        assertEquals(24, secHubReportMetaDataSummary.getTotal());
        assertEquals(8, secHubReportMetaDataSummary.getRed());
        assertEquals(4, secHubReportMetaDataSummary.getYellow());
        assertEquals(12, secHubReportMetaDataSummary.getGreen());
    }
}
