package com.daimler.sechub.domain.scan.project;

import static org.junit.Assert.*;

import java.util.List;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import com.daimler.sechub.domain.scan.ScanDomainTestFileSupport;
import com.daimler.sechub.domain.scan.report.ScanReport;
import com.daimler.sechub.domain.scan.report.ScanReportResult;

public class FalsePositiveJobDataConfigMergerTest {

    private static final String TEST_AUTHOR = "author1";
    private FalsePositiveJobDataConfigMerger toTest;

    private FalsePositiveProjectConfiguration config;

    @Before
    public void before() throws Exception {
        toTest = new FalsePositiveJobDataConfigMerger();

        config = new FalsePositiveProjectConfiguration();

    }

    @Test
    public void report_example1_can_add_me_merged_with_finding_() {
        /* prepare */
        UUID jobUUID = UUID.fromString("f1d02a9d-5e1b-4f52-99e5-401854ccf936");
        
        ScanReportResult scanReportResult = loadScanReport("sechub_result/sechub-report-example1-noscantype.json");

        FalsePositiveJobData falsePositiveJobData = new FalsePositiveJobData();
        falsePositiveJobData.setComment("comment1");
        falsePositiveJobData.setFindingId(2);
        falsePositiveJobData.setJobUUID(jobUUID);
        
        /* execute */
        toTest.addJobDataWithMetaDataToConfig(scanReportResult, config, falsePositiveJobData, TEST_AUTHOR);
        
        /* test */
        List<FalsePositiveEntry> falsePositives = config.getFalsePositives();
        assertNotNull(falsePositives);
        assertEquals(1,falsePositives.size());
        
        FalsePositiveEntry fp1 = falsePositives.iterator().next();
        
        assertEquals("comment1", fp1.getJobData().getComment());
        assertEquals(2, fp1.getJobData().getFindingId());
        assertEquals(jobUUID, fp1.getJobData().getJobUUID());
        assertEquals(TEST_AUTHOR,  fp1.getAuthor());
        
    }

    private ScanReportResult loadScanReport(String path) {
        String reportJSON = ScanDomainTestFileSupport.getTestfileSupport().loadTestFile(path);
        return ScanReportResult.fromJSONString(reportJSON);
    }

}
