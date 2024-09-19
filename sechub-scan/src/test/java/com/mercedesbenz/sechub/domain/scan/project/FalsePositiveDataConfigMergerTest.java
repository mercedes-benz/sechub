// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.project;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.commons.model.SecHubFinding;
import com.mercedesbenz.sechub.commons.model.Severity;
import com.mercedesbenz.sechub.domain.scan.ScanDomainTestFileSupport;
import com.mercedesbenz.sechub.domain.scan.report.ScanSecHubReport;

public class FalsePositiveDataConfigMergerTest {

    private static final String TEST_AUTHOR = "author1";
    private FalsePositiveDataConfigMerger toTest;

    private FalsePositiveProjectConfiguration config;
    private FalsePositiveMetaDataFactory metaDataFactory;

    @BeforeEach
    void beforeEach() throws Exception {
        toTest = new FalsePositiveDataConfigMerger();
        metaDataFactory = mock(FalsePositiveMetaDataFactory.class);

        toTest.metaDataFactory = metaDataFactory;
        config = new FalsePositiveProjectConfiguration();

    }

    @Test
    void sanity_check_for_JSON_example_data() {
        /* execute */
        ScanSecHubReport scanSecHubReport = loadScanReport("sechub_result/sechub-report-example1-noscantype.json");
        SecHubFinding secHubFinding = scanSecHubReport.getResult().getFindings().get(1);
        assertEquals(Severity.MEDIUM, secHubFinding.getSeverity());
        Integer cweId = secHubFinding.getCweId();
        assertEquals(Integer.valueOf(1), cweId);
    }

    @Test
    void report_example1_add_one_jobdata_results_in_one_entry_in_config() {
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
    void report_example1_add_job_data_already_contained_does_not_change() {
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
    void report_example1_REMOVE_job_data_contained_does_remove_it() {
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

    @Test
    void add_one_project_data_entry_results_in_one_entry_in_config() {
        /* prepare */
        String id = "unique-id";
        WebscanFalsePositiveProjectData webScan = new WebscanFalsePositiveProjectData();
        webScan.setUrlPattern("https://myapp-*.example.com:80*/rest/*/search?*");

        FalsePositiveProjectData projectData = new FalsePositiveProjectData();
        projectData.setId(id);
        projectData.setComment("comment1");
        projectData.setWebScan(webScan);

        /* execute */
        toTest.addFalsePositiveProjectDataEntryOrUpdateExisting(config, projectData, TEST_AUTHOR);

        /* test */
        List<FalsePositiveEntry> falsePositives = config.getFalsePositives();
        assertEquals(1, falsePositives.size());

        FalsePositiveEntry falsePositiveEntry = falsePositives.get(0);
        assertNull(falsePositiveEntry.getJobData());
        assertNull(falsePositiveEntry.getMetaData());

        assertEquals(TEST_AUTHOR, falsePositiveEntry.getAuthor());
        assertEquals(projectData, falsePositiveEntry.getProjectData());
    }

    @Test
    void add_one_project_data_entry_with_id_which_already_exists_results_in_entry_being_updated() {
        /* prepare */
        String id = "unique-id";
        WebscanFalsePositiveProjectData webScan1 = new WebscanFalsePositiveProjectData();
        webScan1.setUrlPattern("https://myapp-*.example.com:80*/rest/*/search?*");

        FalsePositiveProjectData projectData1 = new FalsePositiveProjectData();
        projectData1.setId(id);
        projectData1.setComment("comment1");
        projectData1.setWebScan(webScan1);

        FalsePositiveEntry falsePositiveEntry = new FalsePositiveEntry();
        falsePositiveEntry.setProjectData(projectData1);
        falsePositiveEntry.setAuthor(TEST_AUTHOR);

        // add projectData with id="unique-id" to config
        config.getFalsePositives().add(falsePositiveEntry);

        WebscanFalsePositiveProjectData webScan2 = new WebscanFalsePositiveProjectData();
        webScan2.setUrlPattern("https://another-*.example.com:80*/rest/*/search?*");

        FalsePositiveProjectData projectData2 = new FalsePositiveProjectData();
        projectData2.setId(id);
        projectData2.setComment("comment2");
        projectData2.setWebScan(webScan2);

        /* execute */
        toTest.addFalsePositiveProjectDataEntryOrUpdateExisting(config, projectData2, TEST_AUTHOR);

        /* test */
        List<FalsePositiveEntry> falsePositives = config.getFalsePositives();
        assertEquals(1, falsePositives.size());

        FalsePositiveEntry existingEntry = falsePositives.get(0);
        assertNull(existingEntry.getJobData());
        assertNull(existingEntry.getMetaData());

        assertEquals(TEST_AUTHOR, existingEntry.getAuthor());
        assertEquals(projectData2, existingEntry.getProjectData());
    }

    @Test
    void add_a_second_project_data_entry_which_has_an_unique_id_results_in_two_entries_in_config() {
        /* prepare */
        String id1 = "unique-id";
        WebscanFalsePositiveProjectData webScan1 = new WebscanFalsePositiveProjectData();
        webScan1.setUrlPattern("https://myapp-*.example.com:80*/rest/*/search?*");

        FalsePositiveProjectData projectData1 = new FalsePositiveProjectData();
        projectData1.setId(id1);
        projectData1.setComment("comment1");
        projectData1.setWebScan(webScan1);

        FalsePositiveEntry falsePositiveEntry = new FalsePositiveEntry();
        falsePositiveEntry.setProjectData(projectData1);
        falsePositiveEntry.setAuthor(TEST_AUTHOR);

        // add projectData with id="unique-id" to config
        config.getFalsePositives().add(falsePositiveEntry);

        String id2 = "other-unique-id";
        WebscanFalsePositiveProjectData webScan2 = new WebscanFalsePositiveProjectData();
        webScan2.setUrlPattern("https://another-*.example.com:80*/rest/*/search?*");

        FalsePositiveProjectData projectData2 = new FalsePositiveProjectData();
        projectData2.setId(id2);
        projectData2.setComment("comment2");
        projectData2.setWebScan(webScan2);

        /* execute */
        toTest.addFalsePositiveProjectDataEntryOrUpdateExisting(config, projectData2, TEST_AUTHOR);

        /* test */
        List<FalsePositiveEntry> falsePositives = config.getFalsePositives();
        assertEquals(2, falsePositives.size());

        FalsePositiveEntry firstEntry = falsePositives.get(0);
        assertNull(firstEntry.getJobData());
        assertNull(firstEntry.getMetaData());

        assertEquals(TEST_AUTHOR, firstEntry.getAuthor());
        assertEquals(projectData1, firstEntry.getProjectData());

        FalsePositiveEntry secondEntry = falsePositives.get(1);
        assertNull(secondEntry.getJobData());
        assertNull(secondEntry.getMetaData());

        assertEquals(TEST_AUTHOR, secondEntry.getAuthor());
        assertEquals(projectData2, secondEntry.getProjectData());
    }

    @Test
    void remove_one_project_data_entry_which_already_exists_results_in_empty_config() {
        /* prepare */
        String id = "unique-id";
        WebscanFalsePositiveProjectData webScan = new WebscanFalsePositiveProjectData();
        webScan.setUrlPattern("https://myapp-*.example.com:80*/rest/*/search?*");

        FalsePositiveProjectData projectData = new FalsePositiveProjectData();
        projectData.setId(id);
        projectData.setComment("comment1");
        projectData.setWebScan(webScan);

        FalsePositiveEntry falsePositiveEntry = new FalsePositiveEntry();
        falsePositiveEntry.setProjectData(projectData);
        falsePositiveEntry.setAuthor(TEST_AUTHOR);

        // add projectData with id="unique-id" to config
        config.getFalsePositives().add(falsePositiveEntry);

        FalsePositiveProjectData projectDataToRemove = new FalsePositiveProjectData();
        projectDataToRemove.setId(id);

        /* execute */
        toTest.removeProjectDataFromConfig(config, projectDataToRemove);

        /* test */
        List<FalsePositiveEntry> falsePositives = config.getFalsePositives();
        assertEquals(0, falsePositives.size());
    }

    @Test
    void remove_one_project_data_entry_which_does_not_exist_results_in_unchanged_config() {
        /* prepare */
        String id = "unique-id";
        WebscanFalsePositiveProjectData webScan = new WebscanFalsePositiveProjectData();
        webScan.setUrlPattern("https://myapp-*.example.com:80*/rest/*/search?*");

        FalsePositiveProjectData projectData = new FalsePositiveProjectData();
        projectData.setId(id);
        projectData.setComment("comment1");
        projectData.setWebScan(webScan);

        FalsePositiveEntry falsePositiveEntry = new FalsePositiveEntry();
        falsePositiveEntry.setProjectData(projectData);
        falsePositiveEntry.setAuthor(TEST_AUTHOR);

        // add projectData with id="unique-id" to config
        config.getFalsePositives().add(falsePositiveEntry);

        FalsePositiveProjectData projectDataToRemove = new FalsePositiveProjectData();
        projectDataToRemove.setId("other-unique-id");

        /* execute */
        toTest.removeProjectDataFromConfig(config, projectDataToRemove);

        /* test */
        List<FalsePositiveEntry> falsePositives = config.getFalsePositives();
        assertEquals(1, falsePositives.size());

        FalsePositiveEntry unchangedFalsePositiveEntry = falsePositives.get(0);
        assertNull(unchangedFalsePositiveEntry.getJobData());
        assertNull(unchangedFalsePositiveEntry.getMetaData());

        assertEquals(TEST_AUTHOR, unchangedFalsePositiveEntry.getAuthor());
        assertEquals(projectData, unchangedFalsePositiveEntry.getProjectData());
    }

    @Test
    void remove_one_project_data_entry_when_only_job_data_available_results_in_unchanged_config() {
        /* prepare */
        UUID jobUUID = UUID.fromString("f1d02a9d-5e1b-4f52-99e5-401854ccf936");

        ScanSecHubReport scanSecHubReport = loadScanReport("sechub_result/sechub-report-example1-noscantype.json");

        FalsePositiveJobData falsePositiveJobData = new FalsePositiveJobData();
        falsePositiveJobData.setComment("comment1");
        falsePositiveJobData.setFindingId(2);
        falsePositiveJobData.setJobUUID(jobUUID);

        FalsePositiveMetaData metaDataCreatedByFactory = new FalsePositiveMetaData();
        when(metaDataFactory.createMetaData(any())).thenReturn(metaDataCreatedByFactory);

        // first call does setup configuration
        toTest.addJobDataWithMetaDataToConfig(scanSecHubReport, config, falsePositiveJobData, TEST_AUTHOR);

        FalsePositiveProjectData projectDataToRemove = new FalsePositiveProjectData();
        projectDataToRemove.setId("other-unique-id");

        /* execute */
        toTest.removeProjectDataFromConfig(config, projectDataToRemove);

        /* test */
        List<FalsePositiveEntry> falsePositives = config.getFalsePositives();
        assertEquals(1, falsePositives.size());

        FalsePositiveEntry unchangedFalsePositiveEntry = falsePositives.get(0);
        assertEquals(falsePositiveJobData, unchangedFalsePositiveEntry.getJobData());
        assertNotNull(unchangedFalsePositiveEntry.getMetaData());

        assertEquals(TEST_AUTHOR, unchangedFalsePositiveEntry.getAuthor());
        assertEquals(null, unchangedFalsePositiveEntry.getProjectData());
    }

    @Test
    void remove_one_job_data_entry_when_only_project_data_available_results_in_unchanged_config() {
        /* prepare */
        String id = "unique-id";
        WebscanFalsePositiveProjectData webScan = new WebscanFalsePositiveProjectData();
        webScan.setUrlPattern("https://myapp-*.example.com:80*/rest/*/search?*");

        FalsePositiveProjectData projectData = new FalsePositiveProjectData();
        projectData.setId(id);
        projectData.setComment("comment1");
        projectData.setWebScan(webScan);

        FalsePositiveEntry falsePositiveEntry = new FalsePositiveEntry();
        falsePositiveEntry.setAuthor(TEST_AUTHOR);
        falsePositiveEntry.setProjectData(projectData);

        // add projectData with id="unique-id" to config
        config.getFalsePositives().add(falsePositiveEntry);

        UUID jobUUID = UUID.fromString("f1d02a9d-5e1b-4f52-99e5-401854ccf936");
        int findingId = 2;

        FalsePositiveJobData falsePositiveJobData = new FalsePositiveJobData();
        falsePositiveJobData.setFindingId(findingId);
        falsePositiveJobData.setJobUUID(jobUUID);

        /* execute */
        toTest.removeJobDataWithMetaDataFromConfig(config, falsePositiveJobData);

        /* test */
        List<FalsePositiveEntry> falsePositives = config.getFalsePositives();
        assertEquals(1, falsePositives.size());

        FalsePositiveEntry falsePositiveEntryFromConfig = falsePositives.get(0);
        assertNull(falsePositiveEntryFromConfig.getJobData());
        assertNull(falsePositiveEntryFromConfig.getMetaData());

        assertEquals(TEST_AUTHOR, falsePositiveEntryFromConfig.getAuthor());
        assertEquals(projectData, falsePositiveEntryFromConfig.getProjectData());
    }

    private ScanSecHubReport loadScanReport(String path) {
        String reportJSON = ScanDomainTestFileSupport.getTestfileSupport().loadTestFile(path);
        return ScanSecHubReport.fromJSONString(reportJSON);
    }

}
