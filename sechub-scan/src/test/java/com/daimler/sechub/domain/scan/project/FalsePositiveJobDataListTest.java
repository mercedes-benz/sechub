// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.project;

import static org.junit.Assert.*;

import java.util.Iterator;
import java.util.List;

import org.junit.Test;

import com.daimler.sechub.domain.scan.ScanDomainTestFileSupport;

public class FalsePositiveJobDataListTest {

    @Test
    public void json_content_as_described_in_example_of_documentation() {
        /* prepare */
        String json = ScanDomainTestFileSupport.getTestfileSupport().loadTestFileFromRoot("/sechub-doc/src/docs/asciidoc/documents/shared/false-positives/false-positives-REST-API-content-example1.json");
        
        /* execute */
        FalsePositiveJobDataList dataList = FalsePositiveJobDataList.fromString(json);
        
        /* test*/
        assertEquals(FalsePositiveJobDataList.ACCEPTED_TYPE,dataList.getType());
        List<FalsePositiveJobData> jobData = dataList.getJobData();
        assertEquals(3, jobData.size());
        Iterator<FalsePositiveJobData> it = jobData.iterator();
        FalsePositiveJobData jd1 = it.next();
        FalsePositiveJobData jd2 = it.next();
        FalsePositiveJobData jd3 = it.next();
        assertEquals(1,jd1.getFindingId());
        assertEquals("6cfa2ccf-da13-4dee-b529-0225ed9661bd",jd1.getJobUUID().toString());
        assertEquals("Absolute Path Traversal, can be ignored, because not in deployment",jd1.getComment());
        assertEquals(2,jd2.getFindingId());
        assertEquals("6cfa2ccf-da13-4dee-b529-0225ed9661bd",jd2.getJobUUID().toString());
        assertEquals(15,jd3.getFindingId());
        assertEquals("6cfa2ccf-da13-4dee-b529-0225ed9661bd",jd3.getJobUUID().toString());
        assertNull(jd3.getComment());
    }

}
