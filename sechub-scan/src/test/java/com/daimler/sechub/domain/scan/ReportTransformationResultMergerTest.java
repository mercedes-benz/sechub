// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.daimler.sechub.commons.model.SecHubFinding;
import com.daimler.sechub.commons.model.SecHubMessage;
import com.daimler.sechub.commons.model.SecHubMessageType;
import com.daimler.sechub.commons.model.SecHubStatus;

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
    public void r1_r2_merged_contains_all_findings() {
        /* prepare */
        ReportTransformationResult r1 = new ReportTransformationResult();
        SecHubFinding finding1 = new SecHubFinding();
        r1.getResult().getFindings().add(finding1);

        SecHubFinding finding2 = new SecHubFinding();
        ReportTransformationResult r2 = new ReportTransformationResult();
        r1.getResult().getFindings().add(finding2);

        /* execute */
        ReportTransformationResult merged = mergerToTest.merge(r1, r2);

        /* test */
        List<SecHubFinding> findings = merged.getResult().getFindings();
        assertEquals(2, findings.size());
        assertTrue(findings.contains(finding1));
        assertTrue(findings.contains(finding2));

    }

    @Test
    public void r1_r2_merged_contains_all_messages() {
        /* prepare */
        ReportTransformationResult r1 = new ReportTransformationResult();
        ReportTransformationResult r2 = new ReportTransformationResult();

        SecHubMessage message1 = new SecHubMessage(SecHubMessageType.INFO, "msg1");
        SecHubMessage message2 = new SecHubMessage(SecHubMessageType.INFO, "msg2");
        r1.getMessages().add(message1);
        r2.getMessages().add(message2);

        /* execute */
        ReportTransformationResult merged = mergerToTest.merge(r1, r2);

        /* test */
        Set<SecHubMessage> messages = merged.getMessages();
        assertEquals(2, messages.size());
        assertTrue(messages.contains(message1));
        assertTrue(messages.contains(message2));

    }

    @Test
    public void r1_r2_merged_r1_has_status_failed_merge_result_is_failed() {
        /* prepare */
        ReportTransformationResult r1 = new ReportTransformationResult();
        r1.setStatus(SecHubStatus.FAILED);
        ReportTransformationResult r2 = new ReportTransformationResult();
        r2.setStatus(SecHubStatus.SUCCESS);

        /* execute */
        ReportTransformationResult merged = mergerToTest.merge(r1, r2);

        /* test */
        assertTrue(merged.getStatus() == SecHubStatus.FAILED);

    }

    @Test
    public void r1_r2_merged_r2_has_status_failed_merge_result_is_failed() {
        /* prepare */
        ReportTransformationResult r1 = new ReportTransformationResult();
        r1.setStatus(SecHubStatus.SUCCESS);
        ReportTransformationResult r2 = new ReportTransformationResult();
        r2.setStatus(SecHubStatus.FAILED);

        /* execute */
        ReportTransformationResult merged = mergerToTest.merge(r1, r2);

        /* test */
        assertTrue(merged.getStatus() == SecHubStatus.FAILED);

    }
    
    @Test
    public void r1_r2_merged_r1_has_report_version_42_0_merged_has_same_version() {
        /* prepare */
        ReportTransformationResult r1 = new ReportTransformationResult();
        r1.setReportVersion("42.0");
        ReportTransformationResult r2 = new ReportTransformationResult();

        /* execute */
        ReportTransformationResult merged = mergerToTest.merge(r1, r2);

        /* test */
        assertEquals("42.0", merged.getReportVersion());

    }
    
    @Test
    public void r1_r2_merged_r1_has_report_version_42_0_r2_has_version_47_11_merged_has_47_11() {
        /* prepare */
        ReportTransformationResult r1 = new ReportTransformationResult();
        r1.setReportVersion("42.0");
        ReportTransformationResult r2 = new ReportTransformationResult();
        r2.setReportVersion("47.11");

        /* execute */
        ReportTransformationResult merged = mergerToTest.merge(r1, r2);

        /* test */
        assertEquals("47.11", merged.getReportVersion());

    }
    
    @Test
    public void r1_r2_merged_r1_has_report_version_47_11_r2_has_version_42_0_merged_has_42_0() {
        /* prepare */
        ReportTransformationResult r1 = new ReportTransformationResult();
        r1.setReportVersion("47.11");
        ReportTransformationResult r2 = new ReportTransformationResult();
        r2.setReportVersion("42.0");

        /* execute */
        ReportTransformationResult merged = mergerToTest.merge(r1, r2);

        /* test */
        assertEquals("42.0", merged.getReportVersion());

    }


}
