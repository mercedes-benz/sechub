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
import com.daimler.sechub.integrationtest.api.IntegrationTestMockMode;
import com.daimler.sechub.integrationtest.api.IntegrationTestSetup;
import com.daimler.sechub.integrationtest.api.TestProject;
import com.daimler.sechub.sharedkernel.messaging.MessageID;
import com.daimler.sechub.sharedkernel.usecases.UseCaseIdentifier;

public class JobUsecasesEventTraceScenario3IntTest {

    @Rule
    public IntegrationTestSetup setup = IntegrationTestSetup.forScenario(Scenario3.class);

    @Rule
    public Timeout timeOut = Timeout.seconds(600);
    
    TestProject project = PROJECT_1;
    

    @Test // use scenario3 because USER_1 is already assigned to project
    public void UC_ADMIN_RESTARTS_JOB__simulate_jvm_crash_but_product_results_in_db() {
        /* @formatter:off */
        /* prepare */
        AssertExecutionResult result = as(USER_1).createCodeScanAndFetchScanData(project);
        assertNotNull(result);
        UUID sechubJobUUD = result.getResult().getSechubJobUUD();
        
        assertJobHasEnded(project,sechubJobUUD);
        revertJobToStillRunning(sechubJobUUD);
        assertJobIsRunning(project,sechubJobUUD);
        startEventInspection();

        /* execute */
        as(SUPER_ADMIN).restartCodeScanAndFetchJobStatus(project,sechubJobUUD);
        
        /* test */
        assertJobHasEnded(project,sechubJobUUD);
        AssertEventInspection.assertEventInspection().
        expect().
           /* 0 */
           asyncEvent(MessageID.REQUEST_JOB_RESTART).
                 from("com.daimler.sechub.domain.administration.job.JobRestartRequestService").
                 to("com.daimler.sechub.domain.schedule.ScheduleMessageHandler").
           /* 1 */
           asyncEvent(MessageID.JOB_STARTED).
                 from("com.daimler.sechub.domain.schedule.ScheduleJobLauncherService").
                 to("com.daimler.sechub.domain.administration.job.JobAdministrationMessageHandler").
           /* 2 */
           asyncEvent(MessageID.JOB_RESTARTED).
                 from("com.daimler.sechub.domain.schedule.SchedulerRestartJobService").
                 to().
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

    @Test // use scenario3 because USER_1 is already assigned to project
    public void UC_ADMIN_RESTARTS_JOB__simulate_jvm_crash_no_product_results_in_db() {
        /* @formatter:off */
        /* prepare */
        UUID sechubJobUUD = as(USER_1).createCodeScan(project, IntegrationTestMockMode.CODE_SCAN__CHECKMARX__GREEN__FAST);
        as(USER_1).upload(project, sechubJobUUD, "zipfile_contains_only_test1.txt.zip");
        
        revertJobToStillRunning(sechubJobUUD);
        startEventInspection();

        /* execute */
        as(SUPER_ADMIN).restartCodeScanAndFetchJobStatus(project,sechubJobUUD);
        
        /* test */
        assertJobHasEnded(project,sechubJobUUD);
        AssertEventInspection.assertEventInspection().
        expect().
           /* 0 */
           asyncEvent(MessageID.JOB_STARTED).
                 from("com.daimler.sechub.domain.schedule.ScheduleJobLauncherService").
                 to("com.daimler.sechub.domain.administration.job.JobAdministrationMessageHandler").
           /* 1 */
           syncEvent(MessageID.START_SCAN).
                 from("com.daimler.sechub.domain.schedule.batch.ScanExecutionTasklet").
                 to("com.daimler.sechub.domain.scan.ScanService").
        /* assert + write */
        assertAsExpectedAndCreateHistoryFile(UseCaseIdentifier.UC_ADMIN_RESTARTS_JOB.name(),"crashed_jvm_no_product_results");
        /* @formatter:on */
    }

    

}
