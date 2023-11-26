// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.model;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SecHubReportMetaDataSummaryDetailsTest {

    static final int CRITICAL_FINDING_CWEID = 1;
    static final int HIGH_FINDING_CWEID = 2;
    static final int MEDIUM_FINDING_CWEID = 3;
    static final int LOW_FINDING_CWEID = 4;
    static final int INFO_FINDING_CWEID = 5;
    static final int UNCLASSIFIED_FINDING_CWEID = 6;

    static final String CRITICAL_FINDING_NAME = "Critical name";
    static final String HIGH_FINDING_NAME = "Cross Site Scripting (Reflected)";
    static final String MEDIUM_FINDING_NAME = "CSP: Wildcard Directive";
    static final String LOW_FINDING_NAME = "Cookie Without Secure Flag";
    static final String INFO_FINDING_NAME = "Info name";
    static final String UNCLASSIFIED_FINDING_NAME = "Unclassified name";

    SecHubReportMetaDataSummaryDetails secHubReportMetaDataSummaryDetails;
    SecHubFinding criticalFinding;
    SecHubFinding highFinding;
    SecHubFinding mediumFinding;
    SecHubFinding lowFinding;
    SecHubFinding infoFinding;
    SecHubFinding unclassifiedFinding;

    @BeforeEach
    void beforeEach() {
        secHubReportMetaDataSummaryDetails = new SecHubReportMetaDataSummaryDetails();

        criticalFinding = new SecHubFinding();
        criticalFinding.setCweId(CRITICAL_FINDING_CWEID);
        criticalFinding.setSeverity(Severity.CRITICAL);
        criticalFinding.setName(CRITICAL_FINDING_NAME);

        highFinding = new SecHubFinding();
        highFinding.setCweId(HIGH_FINDING_CWEID);
        highFinding.setSeverity(Severity.HIGH);
        highFinding.setName(HIGH_FINDING_NAME);

        mediumFinding = new SecHubFinding();
        mediumFinding.setCweId(MEDIUM_FINDING_CWEID);
        mediumFinding.setSeverity(Severity.MEDIUM);
        mediumFinding.setName(MEDIUM_FINDING_NAME);

        lowFinding = new SecHubFinding();
        lowFinding.setCweId(LOW_FINDING_CWEID);
        lowFinding.setSeverity(Severity.LOW);
        lowFinding.setName(LOW_FINDING_NAME);

        infoFinding = new SecHubFinding();
        infoFinding.setCweId(INFO_FINDING_CWEID);
        infoFinding.setSeverity(Severity.INFO);
        infoFinding.setName(INFO_FINDING_NAME);

        unclassifiedFinding = new SecHubFinding();
        unclassifiedFinding.setCweId(UNCLASSIFIED_FINDING_CWEID);
        unclassifiedFinding.setSeverity(Severity.UNCLASSIFIED);
        unclassifiedFinding.setName(UNCLASSIFIED_FINDING_NAME);
    }

    @Test
    void new_element_for_critical_finding_in_high_map_must_be_created() {
        /* execute */
        secHubReportMetaDataSummaryDetails.detailsHelper(criticalFinding);

        /* test */
        assertNotNull(secHubReportMetaDataSummaryDetails.high.get(CRITICAL_FINDING_NAME));
    }

    @Test
    void new_element_for_high_finding_in_high_map_must_be_created() {
        /* execute */
        secHubReportMetaDataSummaryDetails.detailsHelper(highFinding);

        /* test */
        assertNotNull(secHubReportMetaDataSummaryDetails.high.get(HIGH_FINDING_NAME));
    }

    @Test
    void new_element_for_medium_finding_in_medium_map_must_be_created() {
        /* execute */
        secHubReportMetaDataSummaryDetails.detailsHelper(mediumFinding);

        /* test */
        assertNotNull(secHubReportMetaDataSummaryDetails.medium.get(MEDIUM_FINDING_NAME));
    }

    @Test
    void new_element_for_low_finding_in_low_map_must_be_created() {
        /* execute */
        secHubReportMetaDataSummaryDetails.detailsHelper(lowFinding);

        /* test */
        assertNotNull(secHubReportMetaDataSummaryDetails.low.get(LOW_FINDING_NAME));
    }

    @Test
    void new_element_for_info_finding_in_low_map_must_be_created() {
        /* execute */
        secHubReportMetaDataSummaryDetails.detailsHelper(infoFinding);

        /* test */
        assertNotNull(secHubReportMetaDataSummaryDetails.low.get(INFO_FINDING_NAME));
    }

    @Test
    void new_element_for_unclassified_finding_in_low_map_must_be_created() {
        /* execute */
        secHubReportMetaDataSummaryDetails.detailsHelper(unclassifiedFinding);

        /* test */
        assertNotNull(secHubReportMetaDataSummaryDetails.low.get(UNCLASSIFIED_FINDING_NAME));
    }

    @Test
    void instance_variables_of_new_element_in_high_map_initialized_correctly() {
        /* execute */
        secHubReportMetaDataSummaryDetails.detailsHelper(highFinding);
        SecHubReportMetaDataSummaryDetails.SeverityDetails severityDetails = secHubReportMetaDataSummaryDetails.high.get(HIGH_FINDING_NAME);

        /* test */
        assertEquals(HIGH_FINDING_CWEID, severityDetails.getCweId());
        assertEquals(HIGH_FINDING_NAME, severityDetails.getName());
        assertEquals(1, severityDetails.getCount());
    }

    @Test
    void instance_variables_of_new_element_in_medium_map_initialized_correctly() {
        /* execute */
        secHubReportMetaDataSummaryDetails.detailsHelper(mediumFinding);
        SecHubReportMetaDataSummaryDetails.SeverityDetails severityDetails = secHubReportMetaDataSummaryDetails.medium.get(MEDIUM_FINDING_NAME);

        /* test */
        assertEquals(MEDIUM_FINDING_CWEID, severityDetails.getCweId());
        assertEquals(MEDIUM_FINDING_NAME, severityDetails.getName());
        assertEquals(1, severityDetails.getCount());
    }

    @Test
    void instance_variables_of_new_element_in_low_map_initialized_correctly() {
        /* execute */
        secHubReportMetaDataSummaryDetails.detailsHelper(lowFinding);
        SecHubReportMetaDataSummaryDetails.SeverityDetails severityDetails = secHubReportMetaDataSummaryDetails.low.get(LOW_FINDING_NAME);

        /* test */
        assertEquals(LOW_FINDING_CWEID, severityDetails.getCweId());
        assertEquals(LOW_FINDING_NAME, severityDetails.getName());
        assertEquals(1, severityDetails.getCount());
    }

    @Test
    void if_adding_multiple_similar_findings_into_high_map_counter_contains_correct_value() {
        /* execute */
        for (int i = 0; i < 8; i++) {
            secHubReportMetaDataSummaryDetails.detailsHelper(highFinding);
        }
        SecHubReportMetaDataSummaryDetails.SeverityDetails severityDetails = secHubReportMetaDataSummaryDetails.high.get(HIGH_FINDING_NAME);

        /* test */
        assertEquals(8, severityDetails.getCount());
    }

    @Test
    void if_adding_multiple_similar_findings_into_medium_map_counter_contains_correct_value() {
        /* execute */
        for (int i = 0; i < 4; i++) {
            secHubReportMetaDataSummaryDetails.detailsHelper(mediumFinding);
        }
        SecHubReportMetaDataSummaryDetails.SeverityDetails severityDetails = secHubReportMetaDataSummaryDetails.medium.get(MEDIUM_FINDING_NAME);

        /* test */
        assertEquals(4, severityDetails.getCount());
    }

    @Test
    void if_adding_multiple_similar_findings_into_low_map_counter_contains_correct_value() {
        /* execute */
        for (int i = 0; i < 11; i++) {
            secHubReportMetaDataSummaryDetails.detailsHelper(lowFinding);
        }
        SecHubReportMetaDataSummaryDetails.SeverityDetails severityDetails = secHubReportMetaDataSummaryDetails.low.get(LOW_FINDING_NAME);

        /* test */
        assertEquals(11, severityDetails.getCount());
    }

    @Test
    void must_get_correct_list_must_from_high_map() {
        /* prepare */
        secHubReportMetaDataSummaryDetails.detailsHelper(highFinding);

        /* execute */
        List<SecHubReportMetaDataSummaryDetails.SeverityDetails> list = secHubReportMetaDataSummaryDetails.getHigh();

        /* test */
        assertTrue(!list.isEmpty());
        assertEquals(1, list.size());
        assertEquals(highFinding.getCweId(), list.get(0).getCweId());
        assertEquals(highFinding.getName(), list.get(0).getName());
        assertEquals(1, list.get(0).getCount());
    }

    @Test
    void must_get_correct_list_from_medium_map() {
        /* prepare */
        secHubReportMetaDataSummaryDetails.detailsHelper(mediumFinding);

        /* execute */
        List<SecHubReportMetaDataSummaryDetails.SeverityDetails> list = secHubReportMetaDataSummaryDetails.getMedium();

        /* test */
        assertTrue(!list.isEmpty());
        assertEquals(1, list.size());
        assertEquals(mediumFinding.getCweId(), list.get(0).getCweId());
        assertEquals(mediumFinding.getName(), list.get(0).getName());
        assertEquals(1, list.get(0).getCount());
    }

    @Test
    void must_get_correct_list_from_low_map() {
        /* prepare */
        secHubReportMetaDataSummaryDetails.detailsHelper(lowFinding);

        /* execute */
        List<SecHubReportMetaDataSummaryDetails.SeverityDetails> list = secHubReportMetaDataSummaryDetails.getLow();

        /* test */
        assertTrue(!list.isEmpty());
        assertEquals(1, list.size());
        assertEquals(lowFinding.getCweId(), list.get(0).getCweId());
        assertEquals(lowFinding.getName(), list.get(0).getName());
        assertEquals(1, list.get(0).getCount());
    }
}
