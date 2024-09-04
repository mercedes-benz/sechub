// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.project;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Iterator;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.domain.scan.ScanDomainTestFileSupport;

public class FalsePositiveDataListTest {

    @Test
    void json_content_as_described_in_example_of_documentation() {
        /* prepare */
        String json = ScanDomainTestFileSupport.getTestfileSupport()
                .loadTestFileFromRoot("/sechub-doc/src/docs/asciidoc/documents/shared/false-positives/false-positives-REST-API-content-example1.json");

        /* execute */
        FalsePositiveDataList dataList = FalsePositiveDataList.fromString(json);

        /* test */
        assertEquals(FalsePositiveDataList.ACCEPTED_TYPE, dataList.getType());
        List<FalsePositiveJobData> jobData = dataList.getJobData();
        assertEquals(2, jobData.size());
        Iterator<FalsePositiveJobData> it = jobData.iterator();
        FalsePositiveJobData jd1 = it.next();
        FalsePositiveJobData jd2 = it.next();
        assertEquals(1, jd1.getFindingId());
        assertEquals("6cfa2ccf-da13-4dee-b529-0225ed9661bd", jd1.getJobUUID().toString());
        assertEquals("Absolute Path Traversal, can be ignored because not in deployment", jd1.getComment());
        assertEquals(15, jd2.getFindingId());
        assertEquals("6cfa2ccf-da13-4dee-b529-0225ed9661bd", jd2.getJobUUID().toString());
        assertNull(jd2.getComment());
    }

    @Test
    void jobData_and_projectData_must_never_be_null() {
        /* prepare */
        String json = """
                {
                 "apiVersion": "1.0",
                 "type": "falsePositiveDataList",
                 "jobData": null,
                 "projectData": null
                }
                """;

        /* execute */
        FalsePositiveDataList dataList = FalsePositiveDataList.fromString(json);

        /* test */
        assertTrue(dataList.getJobData().isEmpty());
        assertTrue(dataList.getProjectData().isEmpty());
    }

}
