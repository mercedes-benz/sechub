// SPDX-License-Identifier: MIT
package com.daimler.sechub.integrationtest.scenario2;

import static com.daimler.sechub.integrationtest.api.TestAPI.*;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import com.daimler.sechub.integrationtest.api.AssertEventInspection;
import com.daimler.sechub.integrationtest.api.IntegrationTestSetup;
import com.daimler.sechub.integrationtest.api.TestAPI;
import com.daimler.sechub.sharedkernel.messaging.MessageID;
import com.daimler.sechub.sharedkernel.usecases.UseCaseIdentifier;

public class UserUsecasesEventTraceScenario2IntTest {

    @Rule
    public IntegrationTestSetup setup = IntegrationTestSetup.forScenario(Scenario2.class);

    @Rule
    public Timeout timeOut = Timeout.seconds(600);

    @Test // use scenario2 because USER_1 not already assigned to PROJECT_1
    public void UC_ADMIN_ASSIGNS_USER_TO_PROJECT() {
        /* @formatter:off */
        /* prepare */
	    
        TestAPI.startEventInspection();

        /* execute */
        as(SUPER_ADMIN).
            assignUserToProject(Scenario2.USER_1,Scenario2.PROJECT_1); 
        
        /* test */
        AssertEventInspection.assertEventInspection().
        expect().
           /* 0 */
           asyncEvent(MessageID.USER_ADDED_TO_PROJECT).
                 from("com.daimler.sechub.domain.administration.project.ProjectAssignUserService").
                 to("com.daimler.sechub.domain.schedule.ScheduleMessageHandler",
                    "com.daimler.sechub.domain.scan.ScanMessageHandler").
           /* 1 */
           asyncEvent(MessageID.REQUEST_USER_ROLE_RECALCULATION).
                 from("com.daimler.sechub.domain.administration.project.ProjectAssignUserService").
                 to("com.daimler.sechub.domain.administration.user.UserRoleAdministrationMessageHandler").
           /* 2 */
           asyncEvent(MessageID.USER_ROLES_CHANGED).
                 from("com.daimler.sechub.domain.administration.user.UserRoleCalculationService").
                 to("com.daimler.sechub.domain.authorization.AuthMessageHandler").
        /* assert + write */
        assertAsExpectedAndCreateHistoryFile(UseCaseIdentifier.UC_ADMIN_ASSIGNS_USER_TO_PROJECT.name());
        /* @formatter:on */
    }

}
