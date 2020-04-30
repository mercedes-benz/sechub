// SPDX-License-Identifier: MIT
package com.daimler.sechub.integrationtest.scenario4;

import static com.daimler.sechub.integrationtest.api.AssertJob.*;
import static com.daimler.sechub.integrationtest.api.TestAPI.*;
import static com.daimler.sechub.integrationtest.scenario4.Scenario4.*;
import static org.junit.Assert.*;

import java.util.UUID;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import com.daimler.sechub.integrationtest.api.IntegrationTestMockMode;
import com.daimler.sechub.integrationtest.api.IntegrationTestSetup;
import com.daimler.sechub.integrationtest.api.TestProject;

/**
 * Integration tests, recording events and check happens as expected
 * 
 * @author Albert Tregnaghi
 *
 */
public class RestartJobScenario4IntTest {

    @Rule
    public IntegrationTestSetup setup = IntegrationTestSetup.forScenario(Scenario4.class);

    @Rule
    public Timeout timeOut = Timeout.seconds(600);

    TestProject project = PROJECT_1;

    @Test
    /**
     * We simulate a JVM crash where a product result was already written to
     * database.
     */
    public void restart_hard__simulate_accidently_job_restarted_where_already_done() {
        /* @formatter:off */
        /* prepare */
        UUID sechubJobUUD = as(USER_1).triggerAsyncCodeScanGreenSuperFastWithPseudoZipUpload(project);
        waitForJobDone(project, sechubJobUUD);
        
        /* execute */
        as(SUPER_ADMIN).restartCodeScanHardAndFetchJobStatus(project,sechubJobUUD);
        
        /* test */
        String report = as(USER_1).getJobReport(project.getProjectId(),sechubJobUUD);
        assertNotNull(report);
        if (!report.contains("GREEN")) {
            assertEquals("GREEN was not found, but expected...","GREEN",report);
        }
        
        /* @formatter:on */
    }

    @Test
    /**
     * We simulate a JVM crash where a product result was already written to
     * database.
     */
    public void restart_hard__simulate_jvm_crash_but_product_results_in_db() {
        /* @formatter:off */
        /* prepare */
        clearMetaDataInspection();
        
        UUID sechubJobUUD = as(USER_1).triggerAsyncCodeScanGreenSuperFastWithPseudoZipUpload(project);
        waitForJobDone(project, sechubJobUUD);
        
        simulateJobIsStillRunningAndUploadAvailable(sechubJobUUD);
        
        assertInspections().
            hasAmountOfInspections(1).
                inspectionNr(0).hasId("CHECKMARX");

        /* execute */
        as(SUPER_ADMIN).restartCodeScanHardAndFetchJobStatus(project,sechubJobUUD);
        
        /* test */
        String report = as(USER_1).getJobReport(project.getProjectId(),sechubJobUUD);
        assertNotNull(report);
        if (!report.contains("GREEN")) {
            assertEquals("GREEN was not found, but expected...","GREEN",report);
        }
        /* adapter was called, because product results for purged */
        assertInspections().
            hasAmountOfInspections(2). // why 2? because behavior of product executor is: always call the adapter!
                                       // only adapter is able to know exactly, if the product result is correct, needs a restart
                                       // etc. We try to restart and currently do ignore product result state
            inspectionNr(0).hasId("CHECKMARX").
            and().
            inspectionNr(1).hasId("CHECKMARX");
        /* @formatter:on */
    }

    @Test
    /**
     * We simulate a JVM crash where NO product result was written to database.
     * Shall trigger JOB_RESTARTED
     */
    public void restart_hard__simulate_jvm_crash_no_product_results_in_db_upload_still_available() {
        /* @formatter:off */
        /* prepare */
        UUID sechubJobUUD = as(USER_1).triggerAsyncCodeScanGreenSuperFastWithPseudoZipUpload(project);
        waitForJobDone(project, sechubJobUUD);
        
        simulateJobIsStillRunningAndUploadAvailable(sechubJobUUD);
        
        destroyProductResults(sechubJobUUD); // destroy former product result to simulate execution crashed..

        /* execute */
        as(SUPER_ADMIN).restartCodeScanHardAndFetchJobStatus(project,sechubJobUUD);
        
        /* test */
        String report = as(USER_1).getJobReport(project.getProjectId(),sechubJobUUD);
        assertNotNull(report);
        if (!report.contains("GREEN")) {
            assertEquals("GREEN was not found, but expected...","GREEN",report);
        }
        /* @formatter:on */
    }

    /* ------------------------------------------------------------------------ */
    /* ------------------------------------------------------------------------ */

    @Test
    /**
     * We simulate a JVM crash where a product result was already written to
     * database. Shall trigger JOB_RESTARTED
     */
    public void restart__simulate_accidently_job_restarted_where_already_done() {
        /* @formatter:off */
        /* prepare */
        UUID sechubJobUUD = as(USER_1).triggerAsyncCodeScanGreenSuperFastWithPseudoZipUpload(project);
        
        waitForJobDone(project, sechubJobUUD);
        
        
        /* execute */
        as(SUPER_ADMIN).restartCodeScanAndFetchJobStatus(project,sechubJobUUD);
        
        
        /* test */
        String report = as(USER_1).getJobReport(project.getProjectId(),sechubJobUUD);
        assertNotNull(report);
        if (!report.contains("GREEN")) {
            assertEquals("GREEN was not found, but expected...","GREEN",report);
        }
        
        /* @formatter:on */
    }

    @Test
    /**
     * We simulate a JVM crash where a product result was already written to
     * database. 
     */
    public void restart__simulate_jvm_crash_long_running_job() {
        /* @formatter:off */
        /* prepare */
        clearMetaDataInspection();

        UUID sechubJobUUD = as(USER_1).triggerAsyncCodeScanWithPseudoZipUpload(project,IntegrationTestMockMode.CODE_SCAN__CHECKMARX__GREEN__LONG_RUNNING);
        waitForJobRunning(project, sechubJobUUD);

        /* execute */
        as(SUPER_ADMIN).restartCodeScanAndFetchJobStatus(project,sechubJobUUD);
        
        /* test */
        String report = as(USER_1).getJobReport(project.getProjectId(),sechubJobUUD);
        assertNotNull(report);
        if (!report.contains("GREEN")) {
            assertEquals("GREEN was not found, but expected...","GREEN",report);
        }
        
        /* @formatter:on */

        assertInspections().hasAmountOfInspections(2);
        
        as(SUPER_ADMIN).
            downloadFullScanDataFor(sechubJobUUD).
            dumpDownloadFilePath().
            containsFile("CHECKMARX.xml").
            containsFile("metadata_CHECKMARX.json").
            containsFile("SERECO.json").
            containsFile("metadata_SERECO.json").
            containsFiles(6); // 4 + 2 log files
    }

    @Test
    /**
     * We simulate a JVM crash where NO product result was written to database.
     * Shall trigger JOB_RESTARTED
     */
    public void restart__simulate_jvm_crash_no_product_results_in_db_upload_still_available() {
        /* @formatter:off */
        /* prepare */
        UUID sechubJobUUD = as(USER_1).triggerAsyncCodeScanGreenSuperFastWithPseudoZipUpload(project);
        waitForJobDone(project, sechubJobUUD);
        simulateJobIsStillRunningAndUploadAvailable(sechubJobUUD);
        destroyProductResults(sechubJobUUD); // destroy former product result to simulate execution crashed..

        /* execute */
        as(SUPER_ADMIN).restartCodeScanAndFetchJobStatus(project,sechubJobUUD);
        
        /* test */
        String report = as(USER_1).getJobReport(project.getProjectId(),sechubJobUUD);
        assertNotNull(report);
        if (!report.contains("GREEN")) {
            assertEquals("GREEN was not found, but expected...","GREEN",report);
        }
        /* @formatter:on */
    }

    
    private void simulateJobIsStillRunningAndUploadAvailable(UUID sechubJobUUD) {
        assertNotNull(sechubJobUUD);
        /*
         * in our test the zipfile has been destroyed before, because job has finished -
         * so we must upload again...
         */
        revertJobToStillNotApproved(sechubJobUUD); // make upload possible again...
        as(USER_1).upload(project, sechubJobUUD, "zipfile_contains_only_test1.txt.zip");
        revertJobToStillRunning(sechubJobUUD); // fake it's running
        assertJobIsRunning(project, sechubJobUUD);
    }
}
