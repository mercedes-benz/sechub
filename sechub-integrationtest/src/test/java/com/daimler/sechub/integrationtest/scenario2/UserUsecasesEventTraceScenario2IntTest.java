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
	public Timeout timeOut = Timeout.seconds(120);

	@Test // use scenario2 because USER_1 not already assigned to PROJECT_1
    public void UC_ADMIN_ASSIGNS_USER_TO_PROJECT() {
        /* @formatter:off */
        /* prepare */
	    
        TestAPI.startEventInspection();

        /* execute */
        as(SUPER_ADMIN).
            assignUserToProject(Scenario2.USER_1,Scenario2.PROJECT_1); 
        
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
                    "com.daimler.sechub.domain.administration.project.ProjectAssignUserService").
            assertReceivers(0, 
                    MessageID.USER_ADDED_TO_PROJECT, 
                    "com.daimler.sechub.domain.schedule.ScheduleMessageHandler", 
                    "com.daimler.sechub.domain.scan.ScanMessageHandler").
            assertSender(0, 
                    MessageID.USER_ADDED_TO_PROJECT, 
                    "com.daimler.sechub.domain.administration.project.ProjectAssignUserService").
            /* write */
            writeHistoryToFile(UseCaseIdentifier.UC_ADMIN_ASSIGNS_USER_TO_PROJECT.name());
        /* @formatter:on */
    }

}
