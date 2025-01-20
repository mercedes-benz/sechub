// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.project;

import static org.junit.Assert.*;

import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import com.mercedesbenz.sechub.commons.model.ScanType;
import com.mercedesbenz.sechub.commons.model.Severity;
import com.mercedesbenz.sechub.domain.scan.TestScanDomainFileSupport;

public class FalsePositiveProjectConfigurationTest {

    private FalsePositiveProjectConfiguration configToTest;

    @Before
    public void before() throws Exception {
        configToTest = new FalsePositiveProjectConfiguration();
    }

    @Test
    public void example1_unmarshalled_contains_expected_data() throws Exception {
        /* prepare */
        String json = TestScanDomainFileSupport.getTestfileSupport().loadTestFile("false_positives/scan_false_positive_config_example1.json");

        /* execute */
        configToTest = FalsePositiveProjectConfiguration.fromJSONString(json);

        /* test */
        List<FalsePositiveEntry> falsePositives = configToTest.getFalsePositives();
        assertEquals(3, falsePositives.size());
        Iterator<FalsePositiveEntry> it = falsePositives.iterator();
        FalsePositiveEntry entry1 = it.next();
        FalsePositiveEntry entry2 = it.next();
        FalsePositiveEntry entry3 = it.next();

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        format.setTimeZone(TimeZone.getTimeZone("UTC"));

        // 1
        assertEquals("2019-01-02 21:22:23", format.format(entry1.getCreated()));
        assertEquals("testuser1", entry1.getAuthor());

        FalsePositiveJobData jobData1 = entry1.getJobData();
        assertEquals("comment1", jobData1.getComment());
        assertEquals(42, jobData1.getFindingId());
        assertEquals("eb227545-8f37-47d1-ab60-c17dd1576e19", jobData1.getJobUUID().toString());

        FalsePositiveMetaData metaData1 = entry1.getMetaData();
        assertEquals(ScanType.CODE_SCAN, metaData1.getScanType());
        assertEquals(Severity.MEDIUM, metaData1.getSeverity());

        // 2
        assertEquals("2019-02-02 21:22:23", format.format(entry2.getCreated()));
        assertEquals("testuser2", entry2.getAuthor());

        FalsePositiveJobData jobData2 = entry2.getJobData();
        assertEquals("comment2", jobData2.getComment());
        assertEquals(815, jobData2.getFindingId());
        assertEquals("eb227545-8f37-47d1-ab60-c17dd1576e19", jobData2.getJobUUID().toString());

        FalsePositiveMetaData metaData2 = entry2.getMetaData();
        assertEquals(ScanType.WEB_SCAN, metaData2.getScanType());
        assertEquals(Severity.LOW, metaData2.getSeverity());

        // 3
        assertEquals("2019-03-02 21:22:23", format.format(entry3.getCreated()));
        assertEquals("testuser3", entry3.getAuthor());

        FalsePositiveJobData jobData3 = entry3.getJobData();
        assertEquals(null, jobData3.getComment());
        assertEquals(815, jobData3.getFindingId());
        assertEquals("ec227545-8f37-47d1-ab60-c17dd1576e19", jobData3.getJobUUID().toString());

        FalsePositiveMetaData metaData3 = entry3.getMetaData();
        assertEquals(ScanType.INFRA_SCAN, metaData3.getScanType());
        assertEquals(Severity.LOW, metaData3.getSeverity());

    }

    @Test
    public void marshal_and_unmarshal_contains_same_content() {
        /* prepare */
        UUID jobUUID = UUID.randomUUID();

        FalsePositiveEntry entry = new FalsePositiveEntry();
        FalsePositiveJobData jobData = new FalsePositiveJobData();
        jobData.setComment("comment1");
        jobData.setFindingId(42);
        jobData.setJobUUID(jobUUID);
        List<FalsePositiveEntry> falsePositives = configToTest.getFalsePositives();
        falsePositives.add(entry);

        FalsePositiveCodeMetaData code = new FalsePositiveCodeMetaData();

        FalsePositiveCodePartMetaData start = new FalsePositiveCodePartMetaData();
        start.setLocation("location1");
        start.setRelevantPart("relevant1");
        start.setSourceCode("source1");
        code.setStart(start);

        FalsePositiveCodePartMetaData end = new FalsePositiveCodePartMetaData();
        end.setLocation("location2");
        end.setRelevantPart("relevant2");
        end.setSourceCode("source2");
        code.setEnd(end);

        FalsePositiveMetaData metaData = new FalsePositiveMetaData();
        metaData.setName("name1");
        metaData.setScanType(ScanType.CODE_SCAN);
        metaData.setSeverity(Severity.MEDIUM);
        metaData.setCode(code);

        entry.setAuthor("testuser1");
        entry.setJobData(jobData);
        entry.setMetaData(metaData);

        /* execute */
        String json = configToTest.toJSON();

        /* test */
        FalsePositiveProjectConfiguration loaded = FalsePositiveProjectConfiguration.fromJSONString(json);
        List<FalsePositiveEntry> loadedFalsePositives = loaded.getFalsePositives();
        assertEquals(1, loadedFalsePositives.size());
        FalsePositiveEntry loadedEntry = loadedFalsePositives.iterator().next();
        assertEquals(entry, loadedEntry);
        assertEquals("testuser1", loadedEntry.getAuthor());

    }

}
