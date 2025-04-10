// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.scenario2;

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

public class UserUsecasesEventTraceScenario2IntTest implements IntegrationTestIsNecessaryForDocumentation {

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
                 from("com.mercedesbenz.sechub.domain.administration.project.ProjectAssignUserService").
                 to("com.mercedesbenz.sechub.domain.schedule.ScheduleMessageHandler",
                    "com.mercedesbenz.sechub.domain.scan.ScanMessageHandler").
           /* 1 */
           asyncEvent(MessageID.REQUEST_USER_ROLE_RECALCULATION).
                 from("com.mercedesbenz.sechub.domain.administration.project.ProjectAssignUserService").
                 to("com.mercedesbenz.sechub.domain.administration.user.UserRoleAdministrationMessageHandler").
           /* 2 */
           asyncEvent(MessageID.USER_ROLES_CHANGED).
                 from("com.mercedesbenz.sechub.domain.administration.user.UserRoleCalculationService").
                 to("com.mercedesbenz.sechub.domain.authorization.AuthMessageHandler").
        /* assert + write */
        assertAsExpectedAndCreateHistoryFile(UseCaseIdentifier.UC_ADMIN_OR_OWNER_ASSIGNS_USER_TO_PROJECT.name());
        /* @formatter:on */
    }

}
