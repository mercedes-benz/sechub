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
	public Timeout timeOut = Timeout.seconds(120);

	@Test // use scenario3 because USER_1 is already assigned to PROJECT_1
    public void UC_ADMIN_UNASSIGNS_USER_FROM_PROJECT() {
        /* @formatter:off */
	    /* prepare */
        TestAPI.startEventInspection();

        /* execute */
        as(SUPER_ADMIN).
            unassignUserFromProject(Scenario3.USER_1,Scenario3.PROJECT_1);
        
        /* test */
        AssertEventInspection.
            assertLastInspection(2,
                MessageID.USER_ROLES_CHANGED,
                "com.daimler.sechub.domain.authorization.AuthMessageHandler"
                ).
            assertSender(2, 
                    MessageID.USER_ROLES_CHANGED, 
                    "com.daimler.sechub.domain.administration.user.UserRoleCalculationService").
            assertReceivers(1, 
                    MessageID.REQUEST_USER_ROLE_RECALCULATION, 
                    "com.daimler.sechub.domain.administration.user.UserRoleAdministrationMessageHandler").
            assertSender(1, 
                    MessageID.REQUEST_USER_ROLE_RECALCULATION, 
                    "com.daimler.sechub.domain.administration.project.ProjectUnassignUserService").
            assertReceivers(0, 
                    MessageID.USER_REMOVED_FROM_PROJECT, 
                    "com.daimler.sechub.domain.schedule.ScheduleMessageHandler", 
                    "com.daimler.sechub.domain.scan.ScanMessageHandler").
            assertSender(0, 
                    MessageID.USER_REMOVED_FROM_PROJECT, 
                    "com.daimler.sechub.domain.administration.project.ProjectUnassignUserService").
            /* write */
            writeHistoryToFile(UseCaseIdentifier.UC_ADMIN_UNASSIGNS_USER_FROM_PROJECT.name());
        /* @formatter:on */
    }

}
