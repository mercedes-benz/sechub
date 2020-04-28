// SPDX-License-Identifier: MIT
package com.daimler.sechub.integrationtest.scenario3;

import static com.daimler.sechub.integrationtest.api.AssertJob.*;
import static com.daimler.sechub.integrationtest.api.TestAPI.*;
import static com.daimler.sechub.integrationtest.scenario3.Scenario3.*;
import static org.junit.Assert.*;

import java.util.UUID;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import com.daimler.sechub.integrationtest.api.AssertEventInspection;
import com.daimler.sechub.integrationtest.api.AssertExecutionResult;
import com.daimler.sechub.integrationtest.api.IntegrationTestSetup;
import com.daimler.sechub.integrationtest.api.TestAPI;
import com.daimler.sechub.integrationtest.api.TestProject;
import com.daimler.sechub.sharedkernel.messaging.MessageID;
import com.daimler.sechub.sharedkernel.usecases.UseCaseIdentifier;

public class JobUsecasesEventTraceScenario3IntTest {

    @Rule
    public IntegrationTestSetup setup = IntegrationTestSetup.forScenario(Scenario3.class);

    @Rule
    public Timeout timeOut = Timeout.seconds(600);

    TestProject project = PROJECT_1;

    @Test
    /**
     * We simulate a JVM crash where a product result was already written to
     * database. 
     */
    public void UC_ADMIN_RESTARTS_JOB_HARD__simulate_accidently_job_restarted_where_already_done() {
        /* @formatter:off */
        /* prepare */
        AssertExecutionResult result = as(USER_1).createCodeScanAndFetchScanData(project);
        assertNotNull(result);
        UUID sechubJobUUD = result.getResult().getSechubJobUUD();
        
        assertJobHasEnded(project,sechubJobUUD);

        startEventInspection();
        
        /* execute */
        as(SUPER_ADMIN).restartCodeScanHardAndFetchJobStatus(project,sechubJobUUD);
        /* test */
        String report = as(USER_1).getJobReport(project.getProjectId(),sechubJobUUD);
        assertNotNull(report);
        if (!report.contains("GREEN")) {
            assertEquals("GREEN was not found, but expected...","GREEN",report);
        }
        
        AssertEventInspection.assertEventInspection().
        expect().
           /* 0 */
           asyncEvent(MessageID.REQUEST_JOB_RESTART_HARD).
                 from("com.daimler.sechub.domain.administration.job.JobRestartRequestService").
                 to("com.daimler.sechub.domain.schedule.ScheduleMessageHandler").
           /* 1 */
           asyncEvent(MessageID.JOB_RESTART_CANCELED).
                 from("com.daimler.sechub.domain.schedule.SchedulerRestartJobService").
                 to("com.daimler.sechub.domain.notification.NotificationMessageHandler").
        /* assert + write */
        assertAsExpectedAndCreateHistoryFile(UseCaseIdentifier.UC_ADMIN_RESTARTS_JOB_HARD.name(),"accidently_restart_because_job_has_already_finished");
        /* @formatter:on */
    }

    @Test
    /**
     * We simulate a JVM crash where a product result was already written to
     * database.
     */
    public void UC_ADMIN_RESTARTS_JOB_HARD__simulate_jvm_crash_but_product_results_in_db() {
        /* @formatter:off */
        /* prepare */
        clearMetaDataInspection();
        
        AssertExecutionResult result = as(USER_1).createCodeScanAndFetchScanData(project);
        assertNotNull(result);
        UUID sechubJobUUD = result.getResult().getSechubJobUUD();
        
        assertJobHasEnded(project,sechubJobUUD);
        /* in our test the zipfile has been destroyed before, because job has
         * finished - so we must upload again...
         */
        revertJobToStillNotApproved(sechubJobUUD); // make upload possible again...
        as(USER_1).upload(project, sechubJobUUD, "zipfile_contains_only_test1.txt.zip");
        revertJobToStillRunning(sechubJobUUD);  // fake it's running
        assertJobIsRunning(project,sechubJobUUD);
        
        startEventInspection();
        assertInspections().hasAmountOfInspections(1).inspectionNr(0).hasId("CHECKMARX");

        /* execute */
        as(SUPER_ADMIN).restartCodeScanHardAndFetchJobStatus(project,sechubJobUUD);
        
        /* test */
        String report = as(USER_1).getJobReport(project.getProjectId(),sechubJobUUD);
        assertNotNull(report);
        if (!report.contains("GREEN")) {
            assertEquals("GREEN was not found, but expected...","GREEN",report);
        }
        assertJobHasEnded(project,sechubJobUUD);
        
        AssertEventInspection.assertEventInspection().
        expect().
           /* 0 */
           asyncEvent(MessageID.REQUEST_JOB_RESTART_HARD).
                 from("com.daimler.sechub.domain.administration.job.JobRestartRequestService").
                 to("com.daimler.sechub.domain.schedule.ScheduleMessageHandler").
           /* 1 */
           syncEvent(MessageID.REQUEST_PURGE_JOB_RESULTS).
                 from("com.daimler.sechub.domain.schedule.SchedulerRestartJobService").
                 to("com.daimler.sechub.domain.scan.ScanMessageHandler").
           /* 2 */
           asyncEvent(MessageID.JOB_RESULTS_PURGED).
                 from("com.daimler.sechub.domain.scan.product.ProductResultService").
                 to("com.daimler.sechub.domain.notification.NotificationMessageHandler").
           /* 3 */
           asyncEvent(MessageID.TRIGGER_JOB_RESTART).
                 from("com.daimler.sechub.domain.schedule.SchedulerRestartJobService").
                 to("com.daimler.sechub.domain.notification.NotificationMessageHandler").
           /* 4 */
           asyncEvent(MessageID.JOB_STARTED).
                 from("com.daimler.sechub.domain.schedule.ScheduleJobLauncherService").
                 to("com.daimler.sechub.domain.administration.job.JobAdministrationMessageHandler").
           /* 5 */
           syncEvent(MessageID.START_SCAN).
                 from("com.daimler.sechub.domain.schedule.batch.ScanExecutionTasklet").
                 to("com.daimler.sechub.domain.scan.ScanService").
           /* 6 */
           asyncEvent(MessageID.JOB_DONE).
                 from("com.daimler.sechub.domain.schedule.batch.ScanExecutionTasklet").
                 to("com.daimler.sechub.domain.administration.job.JobAdministrationMessageHandler").
        /* assert + write */
        assertAsExpectedAndCreateHistoryFile(UseCaseIdentifier.UC_ADMIN_RESTARTS_JOB_HARD.name(),"crashed_jvm_with_product_result");
        /* adapter was called, because product results for purged */
        assertInspections().
            hasAmountOfInspections(2). // why 2? because behavior of product executor is: always call the adapter!
                                       // only adapter is able to know exactly, if the product result is correct, needs a restart
                                       // etc. We try to restart and currently do ignore product result state
            inspectionNr(1).hasId("CHECKMARX");
        /* @formatter:on */
    }

    @Test
    /**
     * We simulate a JVM crash where NO product result was written to database.
     * Shall trigger JOB_RESTARTED
     */
    public void UC_ADMIN_RESTARTS_JOB_HARD__simulate_jvm_crash_no_product_results_in_db_upload_still_available() {
        /* @formatter:off */
        /* prepare */
        AssertExecutionResult result = as(USER_1).createCodeScanAndFetchScanData(project);
        assertNotNull(result);
        UUID sechubJobUUD = result.getResult().getSechubJobUUD();
        
        assertJobHasEnded(project,sechubJobUUD);
        /* in our test the zipfile has been destroyed before, because job has
         * finished - so we must upload again...
         */
        revertJobToStillNotApproved(sechubJobUUD); // make upload possible again...
        as(USER_1).upload(project, sechubJobUUD, "zipfile_contains_only_test1.txt.zip");
        revertJobToStillRunning(sechubJobUUD);  // fake it's running
        assertJobIsRunning(project,sechubJobUUD);
        
        destroyProductResults(sechubJobUUD); // destroy former product result to simulate execution crashed..
        
        startEventInspection();

        /* execute */
        as(SUPER_ADMIN).restartCodeScanHardAndFetchJobStatus(project,sechubJobUUD);
        
        /* test */
        String report = as(USER_1).getJobReport(project.getProjectId(),sechubJobUUD);
        assertNotNull(report);
        if (!report.contains("GREEN")) {
            assertEquals("GREEN was not found, but expected...","GREEN",report);
        }
        assertJobHasEnded(project,sechubJobUUD);
        AssertEventInspection.assertEventInspection().
        expect().
           /* 0 */
           asyncEvent(MessageID.REQUEST_JOB_RESTART_HARD).
                 from("com.daimler.sechub.domain.administration.job.JobRestartRequestService").
                 to("com.daimler.sechub.domain.schedule.ScheduleMessageHandler").
           /* 1 */
           syncEvent(MessageID.REQUEST_PURGE_JOB_RESULTS).
                 from("com.daimler.sechub.domain.schedule.SchedulerRestartJobService").
                 to("com.daimler.sechub.domain.scan.ScanMessageHandler").
           /* 2 */
           asyncEvent(MessageID.TRIGGER_JOB_RESTART).
                 from("com.daimler.sechub.domain.schedule.SchedulerRestartJobService").
                 to("com.daimler.sechub.domain.notification.NotificationMessageHandler").
           /* 3 */
           asyncEvent(MessageID.JOB_STARTED).
                 from("com.daimler.sechub.domain.schedule.ScheduleJobLauncherService").
                 to("com.daimler.sechub.domain.administration.job.JobAdministrationMessageHandler").
           /* 4 */
           syncEvent(MessageID.START_SCAN).
                 from("com.daimler.sechub.domain.schedule.batch.ScanExecutionTasklet").
                 to("com.daimler.sechub.domain.scan.ScanService").
           /* 5 */
           asyncEvent(MessageID.JOB_DONE).
                 from("com.daimler.sechub.domain.schedule.batch.ScanExecutionTasklet").
                 to("com.daimler.sechub.domain.administration.job.JobAdministrationMessageHandler").
        /* assert + write */
        assertAsExpectedAndCreateHistoryFile(UseCaseIdentifier.UC_ADMIN_RESTARTS_JOB_HARD.name(),"crashed_jvm_with_product_result");
        /* @formatter:on */
    }

    /* ------------------------------------------------------------------------ */
    /* ------------------------------------------------------------------------ */

    @Test
    /**
     * We simulate a JVM crash where a product result was already written to
     * database. Shall trigger JOB_RESTARTED
     */
    public void UC_ADMIN_RESTARTS_JOB__simulate_accidently_job_restarted_where_already_done() {
        /* @formatter:off */
        /* prepare */
        AssertExecutionResult result = as(USER_1).createCodeScanAndFetchScanData(project);
        assertNotNull(result);
        UUID sechubJobUUD = result.getResult().getSechubJobUUD();
        
        assertJobHasEnded(project,sechubJobUUD);
        startEventInspection();
        
        /* execute */
        as(SUPER_ADMIN).restartCodeScanAndFetchJobStatus(project,sechubJobUUD);
        
        
        /* test */
        String report = as(USER_1).getJobReport(project.getProjectId(),sechubJobUUD);
        assertNotNull(report);
        if (!report.contains("GREEN")) {
            assertEquals("GREEN was not found, but expected...","GREEN",report);
        }
        
        AssertEventInspection.assertEventInspection().
        expect().
           /* 0 */
           asyncEvent(MessageID.REQUEST_JOB_RESTART).
                 from("com.daimler.sechub.domain.administration.job.JobRestartRequestService").
                 to("com.daimler.sechub.domain.schedule.ScheduleMessageHandler").
           /* 1 */
           asyncEvent(MessageID.JOB_RESTART_CANCELED).
                 from("com.daimler.sechub.domain.schedule.SchedulerRestartJobService").
                 to("com.daimler.sechub.domain.notification.NotificationMessageHandler").
        /* assert + write */
        assertAsExpectedAndCreateHistoryFile(UseCaseIdentifier.UC_ADMIN_RESTARTS_JOB.name(),"accidently_restart_because_job_has_already_finished");
        
        /* @formatter:on */
    }

    @Test
    /**
     * We simulate a JVM crash where a product result was already written to
     * database.
     */
    public void UC_ADMIN_RESTARTS_JOB__simulate_jvm_crash_but_product_results_in_db() {
        /* @formatter:off */
        /* prepare */
        AssertExecutionResult result = as(USER_1).createCodeScanAndFetchScanData(project);
        assertNotNull(result);
        UUID sechubJobUUD = result.getResult().getSechubJobUUD();
        
        assertJobHasEnded(project,sechubJobUUD);
        
        String report = as(USER_1).getJobReport(project.getProjectId(),sechubJobUUD);
        assertNotNull(report);
        if (!report.contains("GREEN")) {
            assertEquals("GREEN was not found, but expected...","GREEN",report);
        }
        assertEquals(2,TestAPI.countJobResults(sechubJobUUD)); // checkmarx + sereco
        
        /* in our test the zipfile has been destroyed before, because job has
         * finished - so we must upload again...
         */
        revertJobToStillNotApproved(sechubJobUUD); // make upload possible again...
        as(USER_1).upload(project, sechubJobUUD, "zipfile_contains_only_test1.txt.zip");
        
        revertJobToStillRunning(sechubJobUUD);
        assertJobIsRunning(project,sechubJobUUD);
        clearMetaDataInspection();
        
        startEventInspection();

        /* precondition check - there was no interaction at this point */
        assertInspections().hasAmountOfInspections(0);
        
        /* execute */
        as(SUPER_ADMIN).restartCodeScanAndFetchJobStatus(project,sechubJobUUD);
        
        /* test */
        report = as(USER_1).getJobReport(project.getProjectId(),sechubJobUUD);
        assertNotNull(report);
        if (!report.contains("GREEN")) {
            assertEquals("GREEN was not found, but expected...","GREEN",report);
        }
        
        assertJobHasEnded(project,sechubJobUUD);
        AssertEventInspection.assertEventInspection().
        expect().
           /* 0 */
           asyncEvent(MessageID.REQUEST_JOB_RESTART).
                 from("com.daimler.sechub.domain.administration.job.JobRestartRequestService").
                 to("com.daimler.sechub.domain.schedule.ScheduleMessageHandler").
           /* 1 */
           asyncEvent(MessageID.TRIGGER_JOB_RESTART).
                 from("com.daimler.sechub.domain.schedule.SchedulerRestartJobService").
                 to("com.daimler.sechub.domain.notification.NotificationMessageHandler").
           /* 2 */
           asyncEvent(MessageID.JOB_STARTED).
                 from("com.daimler.sechub.domain.schedule.ScheduleJobLauncherService").
                 to("com.daimler.sechub.domain.administration.job.JobAdministrationMessageHandler").
           /* 3 */
           syncEvent(MessageID.START_SCAN).
                 from("com.daimler.sechub.domain.schedule.batch.ScanExecutionTasklet").
                 to("com.daimler.sechub.domain.scan.ScanService").
           /* 4 */
           asyncEvent(MessageID.JOB_DONE).
                 from("com.daimler.sechub.domain.schedule.batch.ScanExecutionTasklet").
                 to("com.daimler.sechub.domain.administration.job.JobAdministrationMessageHandler").
        /* assert + write */
        assertAsExpectedAndCreateHistoryFile(UseCaseIdentifier.UC_ADMIN_RESTARTS_JOB.name(),"crashed_jvm_with_product_result");
        /* @formatter:on */
        
        assertInspections().hasAmountOfInspections(1); // adapter is called, even when product available - resilient reuse of former results is provided by adapters only
        assertEquals(2,TestAPI.countJobResults(sechubJobUUD)); // checkmarx + sereco - still only 2 results (old ones must be overriden)
    }

    @Test
    /**
     * We simulate a JVM crash where NO product result was written to database.
     * Shall trigger JOB_RESTARTED
     */
    public void UC_ADMIN_RESTARTS_JOB__simulate_jvm_crash_no_product_results_in_db_upload_still_available() {
        /* @formatter:off */
        /* prepare */
        AssertExecutionResult result = as(USER_1).createCodeScanAndFetchScanData(project);
        assertNotNull(result);
        UUID sechubJobUUD = result.getResult().getSechubJobUUD();
        
        assertJobHasEnded(project,sechubJobUUD);
        /* in our test the zipfile has been destroyed before, because job has
         * finished - so we must upload again...
         */
        revertJobToStillNotApproved(sechubJobUUD); // make upload possible again...
        as(USER_1).upload(project, sechubJobUUD, "zipfile_contains_only_test1.txt.zip");
        revertJobToStillRunning(sechubJobUUD);  // fake it's running
        assertJobIsRunning(project,sechubJobUUD);
        
        destroyProductResults(sechubJobUUD); // destroy former product result to simulate execution crashed..
        
        startEventInspection();

        /* execute */
        as(SUPER_ADMIN).restartCodeScanAndFetchJobStatus(project,sechubJobUUD);
        
        /* test */
        String report = as(USER_1).getJobReport(project.getProjectId(),sechubJobUUD);
        assertNotNull(report);
        if (!report.contains("GREEN")) {
            assertEquals("GREEN was not found, but expected...","GREEN",report);
        }
        assertJobHasEnded(project,sechubJobUUD);
        AssertEventInspection.assertEventInspection().
        expect().
           /* 0 */
           asyncEvent(MessageID.REQUEST_JOB_RESTART).
                 from("com.daimler.sechub.domain.administration.job.JobRestartRequestService").
                 to("com.daimler.sechub.domain.schedule.ScheduleMessageHandler").
           /* 1 */
           asyncEvent(MessageID.TRIGGER_JOB_RESTART).
                 from("com.daimler.sechub.domain.schedule.SchedulerRestartJobService").
                 to("com.daimler.sechub.domain.notification.NotificationMessageHandler").
           /* 2 */
           asyncEvent(MessageID.JOB_STARTED).
                 from("com.daimler.sechub.domain.schedule.ScheduleJobLauncherService").
                 to("com.daimler.sechub.domain.administration.job.JobAdministrationMessageHandler").
           /* 3 */
           syncEvent(MessageID.START_SCAN).
                 from("com.daimler.sechub.domain.schedule.batch.ScanExecutionTasklet").
                 to("com.daimler.sechub.domain.scan.ScanService").
           /* 4 */
           asyncEvent(MessageID.JOB_DONE).
                 from("com.daimler.sechub.domain.schedule.batch.ScanExecutionTasklet").
                 to("com.daimler.sechub.domain.administration.job.JobAdministrationMessageHandler").
        /* assert + write */
        assertAsExpectedAndCreateHistoryFile(UseCaseIdentifier.UC_ADMIN_RESTARTS_JOB.name(),"crashed_jvm_with_product_result");
        /* @formatter:on */
    }

}
