// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.mercedesbenz.sechub.commons.model.SecHubFinding;
import com.mercedesbenz.sechub.commons.model.SecHubMessage;
import com.mercedesbenz.sechub.commons.model.SecHubMessageType;
import com.mercedesbenz.sechub.commons.model.SecHubStatus;

public class ReportTransformationResultMergerTest {

    private ReportTransformationResultMerger mergerToTest;

    @Before
    public void before() throws Exception {
        mergerToTest = new ReportTransformationResultMerger();
    }

    @Test
    public void nulls_merged_results_in_null() {
        assertNull(mergerToTest.merge(null, null));
    }

    @Test
    public void null_merged_with_existing_one_returns_existing() {
        ReportTransformationResult r = new ReportTransformationResult();
        assertEquals(r, mergerToTest.merge(r, null));
        assertEquals(r, mergerToTest.merge(null, r));
    }

    @Test
    public void result1_result2_merged_contains_all_findings() {
        /* prepare */
        ReportTransformationResult result1 = new ReportTransformationResult();
        SecHubFinding finding1 = new SecHubFinding();
        result1.getModel().getResult().getFindings().add(finding1);

        SecHubFinding finding2 = new SecHubFinding();
        ReportTransformationResult result2 = new ReportTransformationResult();
        result1.getModel().getResult().getFindings().add(finding2);

        /* execute */
        ReportTransformationResult merged = mergerToTest.merge(result1, result2);

        /* test */
        List<SecHubFinding> findings = merged.getModel().getResult().getFindings();
        assertEquals(2, findings.size());
        assertTrue(findings.contains(finding1));
        assertTrue(findings.contains(finding2));

    }

    @Test
    public void result1_result2_merged_contains_all_messages() {
        /* prepare */
        ReportTransformationResult result1 = new ReportTransformationResult();
        ReportTransformationResult result2 = new ReportTransformationResult();

        SecHubMessage message1 = new SecHubMessage(SecHubMessageType.INFO, "msg1");
        SecHubMessage message2 = new SecHubMessage(SecHubMessageType.INFO, "msg2");
        result1.getModel().getMessages().add(message1);
        result2.getModel().getMessages().add(message2);

        /* execute */
        ReportTransformationResult merged = mergerToTest.merge(result1, result2);

        /* test */
        Set<SecHubMessage> messages = merged.getModel().getMessages();
        assertEquals(2, messages.size());
        assertTrue(messages.contains(message1));
        assertTrue(messages.contains(message2));

    }

    @Test
    public void result1_result2_merged_result1_has_status_failed_merge_result_is_failed() {
        /* prepare */
        ReportTransformationResult result1 = new ReportTransformationResult();
        result1.getModel().setStatus(SecHubStatus.FAILED);
        ReportTransformationResult result2 = new ReportTransformationResult();
        result2.getModel().setStatus(SecHubStatus.SUCCESS);

        /* execute */
        ReportTransformationResult merged = mergerToTest.merge(result1, result2);

        /* test */
        assertTrue(merged.getModel().getStatus() == SecHubStatus.FAILED);

    }

    @Test
    public void result1_result2_merged_result2_has_status_failed_merge_result_is_failed() {
        /* prepare */
        ReportTransformationResult result1 = new ReportTransformationResult();
        result1.getModel().setStatus(SecHubStatus.SUCCESS);
        ReportTransformationResult result2 = new ReportTransformationResult();
        result2.getModel().setStatus(SecHubStatus.FAILED);

        /* execute */
        ReportTransformationResult merged = mergerToTest.merge(result1, result2);

        /* test */
        assertTrue(merged.getModel().getStatus() == SecHubStatus.FAILED);

    }

    @Test
    public void result1_result2_merged_result1_has_report_version_42_0_merged_has_same_version() {
        /* prepare */
        ReportTransformationResult result1 = new ReportTransformationResult();
        result1.getModel().setReportVersion("42.0");
        ReportTransformationResult result2 = new ReportTransformationResult();

        /* execute */
        ReportTransformationResult merged = mergerToTest.merge(result1, result2);

        /* test */
        assertEquals("42.0", merged.getModel().getReportVersion());

    }

    @Test
    public void result1_result2_merged_result1_has_report_version_42_0_result2_has_version_47_11_merged_has_47_11() {
        /* prepare */
        ReportTransformationResult result1 = new ReportTransformationResult();
        result1.getModel().setReportVersion("42.0");
        ReportTransformationResult result2 = new ReportTransformationResult();
        result2.getModel().setReportVersion("47.11");

        /* execute */
        ReportTransformationResult merged = mergerToTest.merge(result1, result2);

        /* test */
        assertEquals("47.11", merged.getModel().getReportVersion());

    }

    @Test
    public void result1_result2_merged_result1_has_report_version_47_11_result2_has_version_42_0_merged_has_42_0() {
        /* prepare */
        ReportTransformationResult result1 = new ReportTransformationResult();
        result1.getModel().setReportVersion("47.11");
        ReportTransformationResult result2 = new ReportTransformationResult();
        result2.getModel().setReportVersion("42.0");

        /* execute */
        ReportTransformationResult merged = mergerToTest.merge(result1, result2);

        /* test */
        assertEquals("42.0", merged.getModel().getReportVersion());

    }

}
