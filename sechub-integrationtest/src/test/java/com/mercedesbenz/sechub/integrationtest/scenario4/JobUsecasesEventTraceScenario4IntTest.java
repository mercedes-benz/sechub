// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.scenario4;

import static com.mercedesbenz.sechub.integrationtest.api.AssertJob.*;
import static com.mercedesbenz.sechub.integrationtest.api.TestAPI.*;
import static com.mercedesbenz.sechub.integrationtest.scenario4.Scenario4.*;
import static org.junit.Assert.*;

import java.util.UUID;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import com.mercedesbenz.sechub.integrationtest.IntegrationTestIsNecessaryForDocumentation;
import com.mercedesbenz.sechub.integrationtest.api.AssertEventInspection;
import com.mercedesbenz.sechub.integrationtest.api.IntegrationTestSetup;
import com.mercedesbenz.sechub.integrationtest.api.TestDataConstants;
import com.mercedesbenz.sechub.integrationtest.api.TestProject;
import com.mercedesbenz.sechub.sharedkernel.messaging.MessageID;
import com.mercedesbenz.sechub.sharedkernel.usecases.UseCaseIdentifier;

public class JobUsecasesEventTraceScenario4IntTest implements IntegrationTestIsNecessaryForDocumentation {

    @Rule
    public IntegrationTestSetup setup = IntegrationTestSetup.forScenario(Scenario4.class);

    @Rule
    public Timeout timeOut = Timeout.seconds(600);

    TestProject project = PROJECT_1;

    /* ------------------------------------------------------------------------ */
    /* --------------------------- HARD RESTART ------------------------------- */
    /* ------------------------------------------------------------------------ */
    @Test
    /**
     * We simulate a JVM crash where a product result was already written to
     * database.
     */
    public void UC_ADMIN_RESTARTS_JOB_HARD__simulate_accidently_job_restarted_where_already_done() {
        /* @formatter:off */
        /* prepare */
        UUID sechubJobUUD = as(USER_1).triggerAsyncCodeScanGreenSuperFastWithPseudoZipUpload(project);

        waitForJobDoneAndFailWhenJobIsFailing(project, sechubJobUUD);

        startEventInspection();

        /* execute */
        as(SUPER_ADMIN).restartJobHardAndFetchJobStatus(project,sechubJobUUD);

        /* test */
        assertJobHasEnded(project,sechubJobUUD);

        AssertEventInspection.assertEventInspection().
        expect().
           /* 0 */
           asyncEvent(MessageID.REQUEST_JOB_RESTART_HARD).
                 from("com.mercedesbenz.sechub.domain.administration.job.JobRestartRequestService").
                 to("com.mercedesbenz.sechub.domain.schedule.ScheduleMessageHandler").
           /* 1 */
           asyncEvent(MessageID.JOB_RESTART_CANCELED).
                 from("com.mercedesbenz.sechub.domain.schedule.SchedulerRestartJobService").
                 to("com.mercedesbenz.sechub.domain.notification.NotificationMessageHandler").
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
        UUID sechubJobUUD = as(USER_1).triggerAsyncCodeScanGreenSuperFastWithPseudoZipUpload(project);
        waitForJobDoneAndFailWhenJobIsFailing(project, sechubJobUUD);
        simulateJobIsStillRunningAndUploadAvailable(sechubJobUUD);

        startEventInspection();

        /* execute */
        as(SUPER_ADMIN).restartJobHardAndFetchJobStatus(project,sechubJobUUD);

        /* test */
        assertJobHasEnded(project,sechubJobUUD);

        AssertEventInspection.assertEventInspection().
        expect().
           /* 0 */
           asyncEvent(MessageID.REQUEST_JOB_RESTART_HARD).
                 from("com.mercedesbenz.sechub.domain.administration.job.JobRestartRequestService").
                 to("com.mercedesbenz.sechub.domain.schedule.ScheduleMessageHandler").
           /* 1 */
           syncEvent(MessageID.REQUEST_PURGE_JOB_RESULTS).
                 from("com.mercedesbenz.sechub.domain.schedule.SchedulerRestartJobService").
                 to("com.mercedesbenz.sechub.domain.scan.ScanMessageHandler").
           /* 2 */
           asyncEvent(MessageID.JOB_RESULTS_PURGED).
                 from("com.mercedesbenz.sechub.domain.scan.product.ProductResultService").
                 to("com.mercedesbenz.sechub.domain.notification.NotificationMessageHandler").
           /* 3 */
           asyncEvent(MessageID.JOB_RESTART_TRIGGERED).
                 from("com.mercedesbenz.sechub.domain.schedule.SchedulerRestartJobService").
                 to("com.mercedesbenz.sechub.domain.notification.NotificationMessageHandler").
           /* 4 */
           asyncEvent(MessageID.JOB_STARTED).
                 from("com.mercedesbenz.sechub.domain.schedule.ScheduleJobLauncherService").
                 to("com.mercedesbenz.sechub.domain.administration.job.JobAdministrationMessageHandler").
           /* 5 */
           asyncEvent(MessageID.JOB_EXECUTION_STARTING).
                 from("com.mercedesbenz.sechub.domain.schedule.batch.SynchronSecHubJobExecutor").
                 to("com.mercedesbenz.sechub.domain.statistic.StatisticMessageHandler").
           /* 6 */
           syncEvent(MessageID.START_SCAN).
                 from("com.mercedesbenz.sechub.domain.schedule.batch.SynchronSecHubJobExecutor").
                 to("com.mercedesbenz.sechub.domain.scan.ScanService").
           /* 7 */
           syncEvent(MessageID.REQUEST_SCHEDULER_JOB_STATUS).
                 from("com.mercedesbenz.sechub.domain.scan.ScanProgressStateFetcher").
                 to("com.mercedesbenz.sechub.domain.schedule.job.SchedulerJobStatusRequestHandler").
           /* 8 */
           asyncEvent(MessageID.JOB_DONE).
                 from("com.mercedesbenz.sechub.domain.schedule.batch.SynchronSecHubJobExecutor").
                 to("com.mercedesbenz.sechub.domain.administration.job.JobAdministrationMessageHandler",
                    "com.mercedesbenz.sechub.domain.statistic.StatisticMessageHandler").
        /* assert + write */
        assertAsExpectedAndCreateHistoryFile(UseCaseIdentifier.UC_ADMIN_RESTARTS_JOB_HARD.name());
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
        UUID sechubJobUUD = as(USER_1).triggerAsyncCodeScanGreenSuperFastWithPseudoZipUpload(project);

        waitForJobDoneAndFailWhenJobIsFailing(project, sechubJobUUD);
        simulateJobIsStillRunningAndUploadAvailable(sechubJobUUD);

        destroyProductResults(sechubJobUUD); // destroy former product result to simulate execution crashed..

        startEventInspection();

        /* execute */
        as(SUPER_ADMIN).restartJobHardAndFetchJobStatus(project,sechubJobUUD);

        /* test */
        assertJobHasEnded(project,sechubJobUUD);
        AssertEventInspection.assertEventInspection().
        expect().
           /* 0 */
           asyncEvent(MessageID.REQUEST_JOB_RESTART_HARD).
                 from("com.mercedesbenz.sechub.domain.administration.job.JobRestartRequestService").
                 to("com.mercedesbenz.sechub.domain.schedule.ScheduleMessageHandler").
           /* 1 */
           syncEvent(MessageID.REQUEST_PURGE_JOB_RESULTS).
                 from("com.mercedesbenz.sechub.domain.schedule.SchedulerRestartJobService").
                 to("com.mercedesbenz.sechub.domain.scan.ScanMessageHandler").
           /* 2 */
           asyncEvent(MessageID.JOB_RESTART_TRIGGERED).
                 from("com.mercedesbenz.sechub.domain.schedule.SchedulerRestartJobService").
                 to("com.mercedesbenz.sechub.domain.notification.NotificationMessageHandler").
           /* 3 */
           asyncEvent(MessageID.JOB_STARTED).
                 from("com.mercedesbenz.sechub.domain.schedule.ScheduleJobLauncherService").
                 to("com.mercedesbenz.sechub.domain.administration.job.JobAdministrationMessageHandler").
           /* 4 */
           asyncEvent(MessageID.JOB_EXECUTION_STARTING).
                 from("com.mercedesbenz.sechub.domain.schedule.batch.SynchronSecHubJobExecutor").
                 to("com.mercedesbenz.sechub.domain.statistic.StatisticMessageHandler").
           /* 5 */
           syncEvent(MessageID.START_SCAN).
                 from("com.mercedesbenz.sechub.domain.schedule.batch.SynchronSecHubJobExecutor").
                 to("com.mercedesbenz.sechub.domain.scan.ScanService").
           /* 6 */
           syncEvent(MessageID.REQUEST_SCHEDULER_JOB_STATUS).
                 from("com.mercedesbenz.sechub.domain.scan.ScanProgressStateFetcher").
                 to("com.mercedesbenz.sechub.domain.schedule.job.SchedulerJobStatusRequestHandler").
           /* 7 */
           asyncEvent(MessageID.JOB_DONE).
                 from("com.mercedesbenz.sechub.domain.schedule.batch.SynchronSecHubJobExecutor").
                 to("com.mercedesbenz.sechub.domain.administration.job.JobAdministrationMessageHandler",
                    "com.mercedesbenz.sechub.domain.statistic.StatisticMessageHandler").
        /* assert + write */
           assertAsExpectedAndCreateHistoryFile(UseCaseIdentifier.UC_ADMIN_RESTARTS_JOB_HARD.name(),"crashed_jvm_with_product_result");
        /* @formatter:on */
    }

    /* ------------------------------------------------------------------------ */
    /* --------------------------- SOFT RESTART ------------------------------- */
    /* ------------------------------------------------------------------------ */

    @Test
    /**
     * We simulate a JVM crash where a product result was already written to
     * database. Shall trigger JOB_RESTARTED
     */
    public void UC_ADMIN_RESTARTS_JOB__simulate_accidently_job_restarted_where_already_done() {
        /* @formatter:off */
        /* prepare */
        UUID sechubJobUUD = as(USER_1).triggerAsyncCodeScanGreenSuperFastWithPseudoZipUpload(project);

        waitForJobDoneAndFailWhenJobIsFailing(project, sechubJobUUD);
        startEventInspection();

        /* execute */
        as(SUPER_ADMIN).restartJobAndFetchJobStatus(project,sechubJobUUD);


        /* test */
        assertJobHasEnded(project,sechubJobUUD);

        AssertEventInspection.assertEventInspection().
        expect().
           /* 0 */
           asyncEvent(MessageID.REQUEST_JOB_RESTART).
                 from("com.mercedesbenz.sechub.domain.administration.job.JobRestartRequestService").
                 to("com.mercedesbenz.sechub.domain.schedule.ScheduleMessageHandler").
           /* 1 */
           asyncEvent(MessageID.JOB_RESTART_CANCELED).
                 from("com.mercedesbenz.sechub.domain.schedule.SchedulerRestartJobService").
                 to("com.mercedesbenz.sechub.domain.notification.NotificationMessageHandler").
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
        UUID sechubJobUUD = as(USER_1).triggerAsyncCodeScanGreenSuperFastWithPseudoZipUpload(project);

        waitForJobDoneAndFailWhenJobIsFailing(project, sechubJobUUD);

        simulateJobIsStillRunningAndUploadAvailable(sechubJobUUD);

        startEventInspection();

        /* execute */
        as(SUPER_ADMIN).restartJobAndFetchJobStatus(project,sechubJobUUD);

        /* test */
        assertJobHasEnded(project,sechubJobUUD);
        AssertEventInspection.assertEventInspection().
        expect().
           /* 0 */
           asyncEvent(MessageID.REQUEST_JOB_RESTART).
                 from("com.mercedesbenz.sechub.domain.administration.job.JobRestartRequestService").
                 to("com.mercedesbenz.sechub.domain.schedule.ScheduleMessageHandler").
           /* 1 */
           asyncEvent(MessageID.JOB_RESTART_TRIGGERED).
                 from("com.mercedesbenz.sechub.domain.schedule.SchedulerRestartJobService").
                 to("com.mercedesbenz.sechub.domain.notification.NotificationMessageHandler").
           /* 2 */
           asyncEvent(MessageID.JOB_STARTED).
                 from("com.mercedesbenz.sechub.domain.schedule.ScheduleJobLauncherService").
                 to("com.mercedesbenz.sechub.domain.administration.job.JobAdministrationMessageHandler").
           /* 3 */
           asyncEvent(MessageID.JOB_EXECUTION_STARTING).
                 from("com.mercedesbenz.sechub.domain.schedule.batch.SynchronSecHubJobExecutor").
                 to("com.mercedesbenz.sechub.domain.statistic.StatisticMessageHandler").
           /* 4 */
           syncEvent(MessageID.START_SCAN).
                 from("com.mercedesbenz.sechub.domain.schedule.batch.SynchronSecHubJobExecutor").
                 to("com.mercedesbenz.sechub.domain.scan.ScanService").
           /* 5 */
           syncEvent(MessageID.REQUEST_SCHEDULER_JOB_STATUS).
                 from("com.mercedesbenz.sechub.domain.scan.ScanProgressStateFetcher").
                 to("com.mercedesbenz.sechub.domain.schedule.job.SchedulerJobStatusRequestHandler").
           /* 6 */
           asyncEvent(MessageID.JOB_DONE).
                 from("com.mercedesbenz.sechub.domain.schedule.batch.SynchronSecHubJobExecutor").
                 to("com.mercedesbenz.sechub.domain.administration.job.JobAdministrationMessageHandler",
                    "com.mercedesbenz.sechub.domain.statistic.StatisticMessageHandler").
        /* assert + write */
           assertAsExpectedAndCreateHistoryFile(UseCaseIdentifier.UC_ADMIN_RESTARTS_JOB.name(),"crashed_jvm_with_product_result");
        /* @formatter:on */

    }

    @Test
    /**
     * We simulate a JVM crash where NO product result was written to database.
     * Shall trigger JOB_RESTARTED
     */
    public void UC_ADMIN_RESTARTS_JOB__simulate_jvm_crash_no_product_results_in_db_upload_still_available() {
        /* @formatter:off */
        /* prepare */
        UUID sechubJobUUD = as(USER_1).triggerAsyncCodeScanGreenSuperFastWithPseudoZipUpload(project);

        waitForJobDoneAndFailWhenJobIsFailing(project, sechubJobUUD);
        simulateJobIsStillRunningAndUploadAvailable(sechubJobUUD);

        destroyProductResults(sechubJobUUD); // destroy former product result to simulate execution crashed..

        startEventInspection();

        /* execute */
        as(SUPER_ADMIN).restartJobAndFetchJobStatus(project,sechubJobUUD);

        /* test */
        assertJobHasEnded(project,sechubJobUUD);
        AssertEventInspection.assertEventInspection().
        expect().
           /* 0 */
           asyncEvent(MessageID.REQUEST_JOB_RESTART).
                 from("com.mercedesbenz.sechub.domain.administration.job.JobRestartRequestService").
                 to("com.mercedesbenz.sechub.domain.schedule.ScheduleMessageHandler").
           /* 1 */
           asyncEvent(MessageID.JOB_RESTART_TRIGGERED).
                 from("com.mercedesbenz.sechub.domain.schedule.SchedulerRestartJobService").
                 to("com.mercedesbenz.sechub.domain.notification.NotificationMessageHandler").
           /* 2 */
           asyncEvent(MessageID.JOB_STARTED).
                 from("com.mercedesbenz.sechub.domain.schedule.ScheduleJobLauncherService").
                 to("com.mercedesbenz.sechub.domain.administration.job.JobAdministrationMessageHandler").
           /* 3 */
           asyncEvent(MessageID.JOB_EXECUTION_STARTING).
                 from("com.mercedesbenz.sechub.domain.schedule.batch.SynchronSecHubJobExecutor").
                 to("com.mercedesbenz.sechub.domain.statistic.StatisticMessageHandler").
           /* 4 */
           syncEvent(MessageID.START_SCAN).
                 from("com.mercedesbenz.sechub.domain.schedule.batch.SynchronSecHubJobExecutor").
                 to("com.mercedesbenz.sechub.domain.scan.ScanService").
           /* 5 */
           syncEvent(MessageID.REQUEST_SCHEDULER_JOB_STATUS).
                 from("com.mercedesbenz.sechub.domain.scan.ScanProgressStateFetcher").
                 to("com.mercedesbenz.sechub.domain.schedule.job.SchedulerJobStatusRequestHandler").
           /* 6 */
           asyncEvent(MessageID.JOB_DONE).
                 from("com.mercedesbenz.sechub.domain.schedule.batch.SynchronSecHubJobExecutor").
                 to("com.mercedesbenz.sechub.domain.administration.job.JobAdministrationMessageHandler",
                    "com.mercedesbenz.sechub.domain.statistic.StatisticMessageHandler").
        /* assert + write */
           assertAsExpectedAndCreateHistoryFile(UseCaseIdentifier.UC_ADMIN_RESTARTS_JOB.name(),"crashed_jvm_with_product_result");
        /* @formatter:on */
    }

    private void simulateJobIsStillRunningAndUploadAvailable(UUID sechubJobUUD) {
        assertNotNull(sechubJobUUD);
        assertJobHasEnded(project, sechubJobUUD);
        /*
         * in our test the zipfile has been destroyed before, because job has finished -
         * so we must upload again...
         */
        revertJobToStillNotApproved(sechubJobUUD); // make upload possible again...
        as(USER_1).uploadSourcecode(project, sechubJobUUD, TestDataConstants.RESOURCE_PATH_ZIPFILE_ONLY_TEST1_TXT);
        revertJobToStillRunning(sechubJobUUD); // fake it's running
        assertJobIsRunning(project, sechubJobUUD);
    }

}
