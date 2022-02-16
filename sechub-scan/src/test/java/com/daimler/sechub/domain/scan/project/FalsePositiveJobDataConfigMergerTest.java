// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.project;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import com.daimler.sechub.commons.model.SecHubFinding;
import com.daimler.sechub.commons.model.Severity;
import com.daimler.sechub.domain.scan.ScanDomainTestFileSupport;
import com.daimler.sechub.domain.scan.report.ScanSecHubReport;

public class FalsePositiveJobDataConfigMergerTest {

    private static final String TEST_AUTHOR = "author1";
    private FalsePositiveJobDataConfigMerger toTest;

    private FalsePositiveProjectConfiguration config;
    private FalsePositiveMetaDataFactory metaDataFactory;

    @Before
    public void before() throws Exception {
        toTest = new FalsePositiveJobDataConfigMerger();
        metaDataFactory = mock(FalsePositiveMetaDataFactory.class);

        toTest.metaDataFactory = metaDataFactory;
        config = new FalsePositiveProjectConfiguration();

    }

    @Test
    public void sanity_check_for_JSON_example_data() {
        /* execute */
        ScanSecHubReport scanSecHubReport = loadScanReport("sechub_result/sechub-report-example1-noscantype.json");
        SecHubFinding secHubFinding = scanSecHubReport.getResult().getFindings().get(1);
        assertEquals(Severity.MEDIUM, secHubFinding.getSeverity());
        Integer cweId = secHubFinding.getCweId();
        assertEquals(Integer.valueOf(1), cweId);
    }

    @Test
    public void report_example1_add_one_jobdata_results_in_one_entry_in_config() {
        /* prepare */
        UUID jobUUID = UUID.fromString("f1d02a9d-5e1b-4f52-99e5-401854ccf936");

        ScanSecHubReport scanSecHubReport = loadScanReport("sechub_result/sechub-report-example1-noscantype.json");

        FalsePositiveJobData falsePositiveJobData = new FalsePositiveJobData();
        falsePositiveJobData.setComment("comment1");
        falsePositiveJobData.setFindingId(2);
        falsePositiveJobData.setJobUUID(jobUUID);

        FalsePositiveMetaData metaDataCreatedByFactory = new FalsePositiveMetaData();
        when(metaDataFactory.createMetaData(any())).thenReturn(metaDataCreatedByFactory);

        /* execute */
        toTest.addJobDataWithMetaDataToConfig(scanSecHubReport, config, falsePositiveJobData, TEST_AUTHOR);

        /* test */
        List<FalsePositiveEntry> falsePositives = config.getFalsePositives();
        assertNotNull(falsePositives);
        assertEquals(1, falsePositives.size());

        FalsePositiveEntry fp1 = falsePositives.iterator().next();

        // check given job data contained
        FalsePositiveJobData jobData = fp1.getJobData();
        assertEquals("comment1", jobData.getComment());
        assertEquals(2, jobData.getFindingId());
        assertEquals(jobUUID, jobData.getJobUUID());
        assertEquals(TEST_AUTHOR, fp1.getAuthor());

        // check meta data created by factory fetched and added
        FalsePositiveMetaData metaData = fp1.getMetaData();
        assertSame(metaDataCreatedByFactory, metaData);

    }

    @Test
    public void report_example1_add_job_data_already_contained_does_not_change() {
        /* prepare */
        UUID jobUUID = UUID.fromString("f1d02a9d-5e1b-4f52-99e5-401854ccf936");

        ScanSecHubReport scanSecHubReport = loadScanReport("sechub_result/sechub-report-example1-noscantype.json");

        FalsePositiveJobData falsePositiveJobData = new FalsePositiveJobData();
        falsePositiveJobData.setComment("comment1");
        falsePositiveJobData.setFindingId(2);
        falsePositiveJobData.setJobUUID(jobUUID);

        // first call does setup configuration
        toTest.addJobDataWithMetaDataToConfig(scanSecHubReport, config, falsePositiveJobData, TEST_AUTHOR);

        // now we change the false positive job data
        FalsePositiveJobData falsePositiveJobData2 = new FalsePositiveJobData();
        falsePositiveJobData2.setComment("comment2");
        falsePositiveJobData2.setFindingId(2);
        falsePositiveJobData2.setJobUUID(jobUUID);

        /* execute */
        toTest.addJobDataWithMetaDataToConfig(scanSecHubReport, config, falsePositiveJobData2, TEST_AUTHOR);

        /* test */
        List<FalsePositiveEntry> falsePositives = config.getFalsePositives();
        assertNotNull(falsePositives);
        assertEquals(1, falsePositives.size());

        FalsePositiveEntry fp1 = falsePositives.iterator().next();

        // check given job data contained
        FalsePositiveJobData jobData = fp1.getJobData();
        assertEquals("comment1", jobData.getComment()); // we still have comment1, so no changes

    }

    @Test
    public void report_example1_REMOVE_job_data_contained_does_remove_it() {
        /* prepare */
        UUID jobUUID = UUID.fromString("f1d02a9d-5e1b-4f52-99e5-401854ccf936");

        ScanSecHubReport scanSecHubReport = loadScanReport("sechub_result/sechub-report-example1-noscantype.json");

        FalsePositiveJobData falsePositiveJobData2 = new FalsePositiveJobData();
        falsePositiveJobData2.setComment("comment2");
        falsePositiveJobData2.setFindingId(2);
        falsePositiveJobData2.setJobUUID(jobUUID);

        FalsePositiveJobData falsePositiveJobData3 = new FalsePositiveJobData();
        falsePositiveJobData3.setComment("comment3");
        falsePositiveJobData3.setFindingId(3);
        falsePositiveJobData3.setJobUUID(jobUUID);

        FalsePositiveJobData falsePositiveJobData4 = new FalsePositiveJobData();
        falsePositiveJobData4.setComment("comment4");
        falsePositiveJobData4.setFindingId(4);
        falsePositiveJobData4.setJobUUID(jobUUID);

        toTest.addJobDataWithMetaDataToConfig(scanSecHubReport, config, falsePositiveJobData2, TEST_AUTHOR);
        toTest.addJobDataWithMetaDataToConfig(scanSecHubReport, config, falsePositiveJobData3, TEST_AUTHOR);
        toTest.addJobDataWithMetaDataToConfig(scanSecHubReport, config, falsePositiveJobData4, TEST_AUTHOR);

        /* test */
        List<FalsePositiveEntry> falsePositives = config.getFalsePositives();
        assertNotNull(falsePositives);
        assertEquals(3, falsePositives.size());

        /* execute */
        // now we remove the false positive job data
        FalsePositiveJobData falsePositiveDataToRemove = new FalsePositiveJobData();
        falsePositiveDataToRemove.setFindingId(3);
        falsePositiveDataToRemove.setJobUUID(jobUUID);

        toTest.removeJobDataWithMetaDataFromConfig(config, falsePositiveDataToRemove);

        /* test */
        falsePositives = config.getFalsePositives();
        assertNotNull(falsePositives);
        assertEquals(2, falsePositives.size());

        Iterator<FalsePositiveEntry> iterator = falsePositives.iterator();
        FalsePositiveEntry fp2 = iterator.next();
        FalsePositiveEntry fp4 = iterator.next();

        FalsePositiveJobData jd2 = fp2.getJobData();
        FalsePositiveJobData jd4 = fp4.getJobData();
        assertEquals(2, jd2.getFindingId());
        assertEquals(4, jd4.getFindingId());

    }

    private ScanSecHubReport loadScanReport(String path) {
        String reportJSON = ScanDomainTestFileSupport.getTestfileSupport().loadTestFile(path);
        return ScanSecHubReport.fromJSONString(reportJSON);
    }

}
