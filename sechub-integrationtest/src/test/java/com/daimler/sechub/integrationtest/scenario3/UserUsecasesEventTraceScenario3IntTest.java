// SPDX-License-Identifier: MIT
package com.daimler.sechub.integrationtest.scenario3;

import static com.daimler.sechub.integrationtest.api.TestAPI.*;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import com.daimler.sechub.integrationtest.api.AssertEventInspection;
import com.daimler.sechub.integrationtest.api.IntegrationTestSetup;
import com.daimler.sechub.integrationtest.api.TestAPI;
import com.daimler.sechub.sharedkernel.messaging.MessageID;
import com.daimler.sechub.sharedkernel.usecases.UseCaseIdentifier;

public class UserUsecasesEventTraceScenario3IntTest {

    @Rule
    public IntegrationTestSetup setup = IntegrationTestSetup.forScenario(Scenario3.class);

    @Rule
    public Timeout timeOut = Timeout.seconds(600);

    @Test // use scenario3 because USER_1 is already assigned to PROJECT_1
    public void UC_ADMIN_UNASSIGNS_USER_FROM_PROJECT() {
        /* @formatter:off */
	    /* prepare */
        TestAPI.startEventInspection();

        /* execute */
        as(SUPER_ADMIN).
            unassignUserFromProject(Scenario3.USER_1,Scenario3.PROJECT_1);
        
        /* test */
        AssertEventInspection.assertEventInspection().
        expect().
           /* 0 */
           asyncEvent(MessageID.USER_REMOVED_FROM_PROJECT).
                 from("com.daimler.sechub.domain.administration.project.ProjectUnassignUserService").
                 to("com.daimler.sechub.domain.scan.ScanMessageHandler",
                    "com.daimler.sechub.domain.schedule.ScheduleMessageHandler").
           /* 1 */
           asyncEvent(MessageID.REQUEST_USER_ROLE_RECALCULATION).
                 from("com.daimler.sechub.domain.administration.project.ProjectUnassignUserService").
                 to("com.daimler.sechub.domain.administration.user.UserRoleAdministrationMessageHandler").
           /* 2 */
           asyncEvent(MessageID.USER_ROLES_CHANGED).
                 from("com.daimler.sechub.domain.administration.user.UserRoleCalculationService").
                 to("com.daimler.sechub.domain.authorization.AuthMessageHandler").
        /* assert + write */
        assertAsExpectedAndCreateHistoryFile(UseCaseIdentifier.UC_ADMIN_UNASSIGNS_USER_FROM_PROJECT.name());
        /* @formatter:on */
    }

}
