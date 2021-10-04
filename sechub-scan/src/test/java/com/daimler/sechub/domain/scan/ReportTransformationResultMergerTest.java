// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.daimler.sechub.commons.model.SecHubFinding;

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
        assertEquals(r,mergerToTest.merge(r, null));
        assertEquals(r,mergerToTest.merge(null,r));
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

}
