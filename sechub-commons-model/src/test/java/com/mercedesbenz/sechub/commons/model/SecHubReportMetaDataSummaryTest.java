package com.mercedesbenz.sechub.commons.model;

import static org.junit.Assert.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SecHubReportMetaDataSummaryTest {

    SecHubReportScanTypeSummary secHubReportScanTypeSummary;
    SecHubFinding highFinding, criticalFinding;
    SecHubFinding mediumFinding;
    SecHubFinding infoFinding, unclassifiedFinding, lowFinding;

    @BeforeEach
    void beforeEach() {
        secHubReportScanTypeSummary = new SecHubReportScanTypeSummary();

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
        secHubReportScanTypeSummary.addToCalculation(criticalFinding);

        /* test */
        assertEquals(1, secHubReportScanTypeSummary.getTotal());
        assertEquals(1, secHubReportScanTypeSummary.getRed());
        assertEquals(0, secHubReportScanTypeSummary.getYellow());
        assertEquals(0, secHubReportScanTypeSummary.getGreen());
    }

    @Test
    void when_add_new_high_finding_then_only_red_and_total_counters_must_be_increased() {
        /* execute */
        secHubReportScanTypeSummary.addToCalculation(highFinding);

        /* test */
        assertEquals(1, secHubReportScanTypeSummary.getTotal());
        assertEquals(1, secHubReportScanTypeSummary.getRed());
        assertEquals(0, secHubReportScanTypeSummary.getYellow());
        assertEquals(0, secHubReportScanTypeSummary.getGreen());
    }

    @Test
    void when_add_new_medium_finding_then_only_yellow_and_total_counters_must_be_increased() {
        /* execute */
        secHubReportScanTypeSummary.addToCalculation(mediumFinding);

        /* test */
        assertEquals(1, secHubReportScanTypeSummary.getTotal());
        assertEquals(0, secHubReportScanTypeSummary.getRed());
        assertEquals(1, secHubReportScanTypeSummary.getYellow());
        assertEquals(0, secHubReportScanTypeSummary.getGreen());
    }

    @Test
    void when_add_new_low_finding_then_only_green_and_total_counters_must_be_increased() {
        /* execute */
        secHubReportScanTypeSummary.addToCalculation(lowFinding);

        /* test */
        assertEquals(1, secHubReportScanTypeSummary.getTotal());
        assertEquals(0, secHubReportScanTypeSummary.getRed());
        assertEquals(0, secHubReportScanTypeSummary.getYellow());
        assertEquals(1, secHubReportScanTypeSummary.getGreen());
    }

    @Test
    void when_add_new_info_finding_then_only_green_and_total_counters_must_be_increased() {
        /* execute */
        secHubReportScanTypeSummary.addToCalculation(infoFinding);

        /* test */
        assertEquals(1, secHubReportScanTypeSummary.getTotal());
        assertEquals(0, secHubReportScanTypeSummary.getRed());
        assertEquals(0, secHubReportScanTypeSummary.getYellow());
        assertEquals(1, secHubReportScanTypeSummary.getGreen());
    }

    @Test
    void when_add_new_unclassified_finding_then_only_green_and_total_counters_must_be_increased() {
        /* execute */
        secHubReportScanTypeSummary.addToCalculation(unclassifiedFinding);

        /* test */
        assertEquals(1, secHubReportScanTypeSummary.getTotal());
        assertEquals(0, secHubReportScanTypeSummary.getRed());
        assertEquals(0, secHubReportScanTypeSummary.getYellow());
        assertEquals(1, secHubReportScanTypeSummary.getGreen());
    }

    @Test
    void when_add_multiple_critical_findings_then_red_and_total_counters_must_be_increased() {
        /* execute */
        for (int i = 0; i < 9; i++) {
            secHubReportScanTypeSummary.addToCalculation(criticalFinding);
        }

        /* test */
        assertEquals(9, secHubReportScanTypeSummary.getTotal());
        assertEquals(9, secHubReportScanTypeSummary.getRed());
    }

    @Test
    void when_add_multiple_high_findings_then_red_and_total_counters_must_be_increased() {
        /* execute */
        for (int i = 0; i < 11; i++) {
            secHubReportScanTypeSummary.addToCalculation(highFinding);
        }

        /* test */
        assertEquals(11, secHubReportScanTypeSummary.getTotal());
        assertEquals(11, secHubReportScanTypeSummary.getRed());
    }

    @Test
    void when_add_multiple_medium_findings_then_yellow_and_total_counters_must_be_increased() {
        /* execute */
        for (int i = 0; i < 6; i++) {
            secHubReportScanTypeSummary.addToCalculation(mediumFinding);
        }

        /* test */
        assertEquals(6, secHubReportScanTypeSummary.getTotal());
        assertEquals(6, secHubReportScanTypeSummary.getYellow());
    }

    @Test
    void when_add_multiple_low_findings_then_green_and_total_counters_must_be_increased() {
        /* execute */
        for (int i = 0; i < 4; i++) {
            secHubReportScanTypeSummary.addToCalculation(lowFinding);
        }

        /* test */
        assertEquals(4, secHubReportScanTypeSummary.getTotal());
        assertEquals(4, secHubReportScanTypeSummary.getGreen());
    }

    @Test
    void when_add_multiple_info_findings_then_green_and_total_counters_must_be_increased() {
        /* execute */
        for (int i = 0; i < 3; i++) {
            secHubReportScanTypeSummary.addToCalculation(infoFinding);
        }

        /* test */
        assertEquals(3, secHubReportScanTypeSummary.getTotal());
        assertEquals(3, secHubReportScanTypeSummary.getGreen());
    }

    @Test
    void when_add_multiple_unclassified_findings_then_green_and_total_counters_must_be_increased() {
        /* execute */
        for (int i = 0; i < 5; i++) {
            secHubReportScanTypeSummary.addToCalculation(unclassifiedFinding);
        }

        /* test */
        assertEquals(5, secHubReportScanTypeSummary.getTotal());
        assertEquals(5, secHubReportScanTypeSummary.getGreen());
    }

    @Test
    void when_add_multiple_critical_high_medium_low_info_unclassified_findings_then_red_yellow_green_and_total_counters_must_be_increased() {
        /* execute */
        for (int i = 0; i < 4; i++) {
            secHubReportScanTypeSummary.addToCalculation(criticalFinding);
            secHubReportScanTypeSummary.addToCalculation(highFinding);
            secHubReportScanTypeSummary.addToCalculation(mediumFinding);
            secHubReportScanTypeSummary.addToCalculation(lowFinding);
            secHubReportScanTypeSummary.addToCalculation(infoFinding);
            secHubReportScanTypeSummary.addToCalculation(unclassifiedFinding);
        }

        /* test */
        assertEquals(24, secHubReportScanTypeSummary.getTotal());
        assertEquals(8, secHubReportScanTypeSummary.getRed());
        assertEquals(4, secHubReportScanTypeSummary.getYellow());
        assertEquals(12, secHubReportScanTypeSummary.getGreen());
    }
}
