// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.scenario3;

import static com.mercedesbenz.sechub.integrationtest.api.TestAPI.*;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import com.mercedesbenz.sechub.integrationtest.IntegrationTestIsNecessaryForDocumentation;
import com.mercedesbenz.sechub.integrationtest.api.AssertEventInspection;
import com.mercedesbenz.sechub.integrationtest.api.IntegrationTestSetup;
import com.mercedesbenz.sechub.integrationtest.api.TestAPI;
import com.mercedesbenz.sechub.sharedkernel.messaging.MessageID;
import com.mercedesbenz.sechub.sharedkernel.usecases.UseCaseIdentifier;

public class UserUsecasesEventTraceScenario3IntTest implements IntegrationTestIsNecessaryForDocumentation {

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
                 from("com.mercedesbenz.sechub.domain.administration.project.ProjectUnassignUserService").
                 to("com.mercedesbenz.sechub.domain.scan.ScanMessageHandler",
                    "com.mercedesbenz.sechub.domain.schedule.ScheduleMessageHandler").
           /* 1 */
           asyncEvent(MessageID.REQUEST_USER_ROLE_RECALCULATION).
                 from("com.mercedesbenz.sechub.domain.administration.project.ProjectUnassignUserService").
                 to("com.mercedesbenz.sechub.domain.administration.user.UserRoleAdministrationMessageHandler").
           /* 2 */
           asyncEvent(MessageID.USER_ROLES_CHANGED).
                 from("com.mercedesbenz.sechub.domain.administration.user.UserRoleCalculationService").
                 to("com.mercedesbenz.sechub.domain.authorization.AuthMessageHandler").
        /* assert + write */
        assertAsExpectedAndCreateHistoryFile(UseCaseIdentifier.UC_ADMIN_OR_OWNER_UNASSIGNS_USER_FROM_PROJECT.name());

        /* @formatter:on */
    }

}
