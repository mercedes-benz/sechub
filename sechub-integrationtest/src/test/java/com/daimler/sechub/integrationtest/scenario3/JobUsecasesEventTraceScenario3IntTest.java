// SPDX-License-Identifier: MIT
package com.daimler.sechub.integrationtest.scenario3;

import static com.daimler.sechub.integrationtest.api.TestAPI.*;
import static com.daimler.sechub.integrationtest.scenario3.Scenario3.*;
import static org.junit.Assert.*;

import java.util.UUID;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import com.daimler.sechub.integrationtest.api.AssertEventInspection;
import com.daimler.sechub.integrationtest.api.AssertExecutionResult;
import com.daimler.sechub.integrationtest.api.AssertJobScheduler;
import com.daimler.sechub.integrationtest.api.IntegrationTestSetup;
import com.daimler.sechub.integrationtest.api.TestAPI;
import com.daimler.sechub.sharedkernel.messaging.MessageID;
import com.daimler.sechub.sharedkernel.usecases.UseCaseIdentifier;

public class JobUsecasesEventTraceScenario3IntTest {

    @Rule
    public IntegrationTestSetup setup = IntegrationTestSetup.forScenario(Scenario3.class);

    @Rule
    public Timeout timeOut = Timeout.seconds(600);

    @Test // use scenario3 because USER_1 is already assigned to PROJECT_1
    public void UC_ADMIN_RESTARTS_JOB() {
        /* @formatter:off */
        /* check preconditions */
        assertUser(USER_1).isAssignedToProject(PROJECT_1).hasOwnerRole().hasUserRole();
        
        /* prepare */
        AssertExecutionResult result = as(USER_1).createCodeScanAndFetchScanData(PROJECT_1);
        assertNotNull(result);
        UUID sechubJobUUD = result.getResult().getSechubJobUUD();
        
        
        String status =  as(SUPER_ADMIN).getJobStatus(PROJECT_1.getProjectId(), sechubJobUUD);
        if (!status.contains("ENDED") || status.contains("STARTED") ) {
            throw new IllegalStateException("status not as expected, but:"+status);
        }
        
        TestAPI.revertJobToStillRunning(sechubJobUUD);
        status = as(SUPER_ADMIN).getJobStatus(PROJECT_1.getProjectId(), sechubJobUUD);
        if (status.contains("ENDED") || !status.contains("STARTED") ) {
            fail ("not ENDED! status="+status);
        }
        
        TestAPI.startEventInspection();

        /* execute */
        status =as(SUPER_ADMIN).restartCodeScanAndFetchJobStatus(PROJECT_1,sechubJobUUD);
        if (!status.contains("ENDED") || status.contains("STARTED") ) {
            throw new IllegalStateException("status not as expected, but:"+status);
        }
        /* test */
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
        assertAsExpectedAndCreateHistoryFile(UseCaseIdentifier.UC_ADMIN_RESTARTS_JOB.name());
        /* @formatter:on */
    }

}
