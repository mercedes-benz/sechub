// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.model;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SecHubReportMetaDataSummaryDetailsTest {

    static final int HIGH_FINDING_CWEID = 123;
    static final int MEDIUM_FINDING_CWEID = 456;
    static final int LOW_FINDING_CWEID = 789;

    SecHubReportMetaDataSummaryDetails secHubReportMetaDataSummaryDetails;
    SecHubFinding highFinding;
    SecHubFinding mediumFinding;
    SecHubFinding lowFinding;

    @BeforeEach
    void beforeEach() {
        secHubReportMetaDataSummaryDetails = new SecHubReportMetaDataSummaryDetails();

        highFinding = new SecHubFinding();
        highFinding.setCweId(HIGH_FINDING_CWEID);
        highFinding.setSeverity(Severity.HIGH);
        highFinding.setName("Cross Site Scripting (Reflected)");

        mediumFinding = new SecHubFinding();
        mediumFinding.setCweId(MEDIUM_FINDING_CWEID);
        mediumFinding.setSeverity(Severity.MEDIUM);
        mediumFinding.setName("CSP: Wildcard Directive");

        lowFinding = new SecHubFinding();
        lowFinding.setCweId(LOW_FINDING_CWEID);
        lowFinding.setSeverity(Severity.LOW);
        lowFinding.setName("Cookie Without Secure Flag");
    }

    @Test
    void new_element_in_high_map_must_be_created() {
        /* execute */
        secHubReportMetaDataSummaryDetails.detailsHelper(highFinding);

        /* test */
        assertNotNull(secHubReportMetaDataSummaryDetails.high.get(HIGH_FINDING_CWEID));
    }

    @Test
    void new_element_in_medium_map_must_be_created() {
        /* execute */
        secHubReportMetaDataSummaryDetails.detailsHelper(mediumFinding);

        /* test */
        assertNotNull(secHubReportMetaDataSummaryDetails.medium.get(MEDIUM_FINDING_CWEID));
    }

    @Test
    void new_element_in_low_map_must_be_created() {
        /* execute */
        secHubReportMetaDataSummaryDetails.detailsHelper(lowFinding);

        /* test */
        assertNotNull(secHubReportMetaDataSummaryDetails.low.get(LOW_FINDING_CWEID));
    }

    @Test
    void instance_variables_of_new_element_in_high_map_initialized_correctly() {
        /* execute */
        secHubReportMetaDataSummaryDetails.detailsHelper(highFinding);
        SecHubReportMetaDataSummaryDetails.SeverityDetails severityDetails = secHubReportMetaDataSummaryDetails.high.get(HIGH_FINDING_CWEID);

        /* test */
        assertEquals(HIGH_FINDING_CWEID, severityDetails.getCweId());
        assertEquals("Cross Site Scripting (Reflected)", severityDetails.getName());
        assertEquals(1, severityDetails.getCount());
    }

    @Test
    void instance_variables_of_new_element_in_medium_map_initialized_correctly() {
        /* execute */
        secHubReportMetaDataSummaryDetails.detailsHelper(mediumFinding);
        SecHubReportMetaDataSummaryDetails.SeverityDetails severityDetails = secHubReportMetaDataSummaryDetails.medium.get(MEDIUM_FINDING_CWEID);

        /* test */
        assertEquals(MEDIUM_FINDING_CWEID, severityDetails.getCweId());
        assertEquals("CSP: Wildcard Directive", severityDetails.getName());
        assertEquals(1, severityDetails.getCount());
    }

    @Test
    void instance_variables_of_new_element_in_low_map_initialized_correctly() {
        /* execute */
        secHubReportMetaDataSummaryDetails.detailsHelper(lowFinding);
        SecHubReportMetaDataSummaryDetails.SeverityDetails severityDetails = secHubReportMetaDataSummaryDetails.low.get(LOW_FINDING_CWEID);

        /* test */
        assertEquals(LOW_FINDING_CWEID, severityDetails.getCweId());
        assertEquals("Cookie Without Secure Flag", severityDetails.getName());
        assertEquals(1, severityDetails.getCount());
    }

    @Test
    void if_adding_multiple_similar_findings_into_high_map_counter_contains_correct_value() {
        /* execute */
        for (int i = 0; i < 101; i++) {
            secHubReportMetaDataSummaryDetails.detailsHelper(highFinding);
        }
        SecHubReportMetaDataSummaryDetails.SeverityDetails severityDetails = secHubReportMetaDataSummaryDetails.high.get(HIGH_FINDING_CWEID);

        /* test */
        assertEquals(101, severityDetails.getCount());
    }

    @Test
    void if_adding_multiple_similar_findings_into_medium_map_counter_contains_correct_value() {
        /* execute */
        for (int i = 0; i < 101; i++) {
            secHubReportMetaDataSummaryDetails.detailsHelper(mediumFinding);
        }
        SecHubReportMetaDataSummaryDetails.SeverityDetails severityDetails = secHubReportMetaDataSummaryDetails.medium.get(MEDIUM_FINDING_CWEID);

        /* test */
        assertEquals(101, severityDetails.getCount());
    }

    @Test
    void if_adding_multiple_similar_findings_into_low_map_counter_contains_correct_value() {
        /* execute */
        for (int i = 0; i < 101; i++) {
            secHubReportMetaDataSummaryDetails.detailsHelper(lowFinding);
        }
        SecHubReportMetaDataSummaryDetails.SeverityDetails severityDetails = secHubReportMetaDataSummaryDetails.low.get(LOW_FINDING_CWEID);

        /* test */
        assertEquals(101, severityDetails.getCount());
    }

    @Test
    void correct_list_must_be_get_from_high_map() {
        /* prepare */
        secHubReportMetaDataSummaryDetails.detailsHelper(highFinding);

        /* execute */
        List<SecHubReportMetaDataSummaryDetails.SeverityDetails> list = secHubReportMetaDataSummaryDetails.getHigh();

        /* test */
        assertTrue(!list.isEmpty());
        assertTrue(1 == list.size());
        assertEquals(highFinding.getCweId(), list.get(0).getCweId());
        assertEquals(highFinding.getName(), list.get(0).getName());
        assertEquals(1, list.get(0).getCount());
    }

    @Test
    void correct_list_must_be_get_from_medium_map() {
        /* prepare */
        secHubReportMetaDataSummaryDetails.detailsHelper(mediumFinding);

        /* execute */
        List<SecHubReportMetaDataSummaryDetails.SeverityDetails> list = secHubReportMetaDataSummaryDetails.getMedium();

        /* test */
        assertTrue(!list.isEmpty());
        assertTrue(1 == list.size());
        assertEquals(mediumFinding.getCweId(), list.get(0).getCweId());
        assertEquals(mediumFinding.getName(), list.get(0).getName());
        assertEquals(1, list.get(0).getCount());
    }

    @Test
    void correct_list_must_be_get_from_low_map() {
        /* prepare */
        secHubReportMetaDataSummaryDetails.detailsHelper(lowFinding);

        /* execute */
        List<SecHubReportMetaDataSummaryDetails.SeverityDetails> list = secHubReportMetaDataSummaryDetails.getLow();

        /* test */
        assertTrue(!list.isEmpty());
        assertTrue(1 == list.size());
        assertEquals(lowFinding.getCweId(), list.get(0).getCweId());
        assertEquals(lowFinding.getName(), list.get(0).getName());
        assertEquals(1, list.get(0).getCount());
    }
}
