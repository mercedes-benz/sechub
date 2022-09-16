// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.scenario1;

import static com.mercedesbenz.sechub.integrationtest.api.AssertEventInspection.*;
import static com.mercedesbenz.sechub.integrationtest.api.TestAPI.*;

import org.junit.Rule;
import org.junit.Test;
import org.junit.jupiter.api.AfterAll;
import org.junit.rules.Timeout;

import com.mercedesbenz.sechub.integrationtest.IntegrationTestIsNecessaryForDocumentation;
import com.mercedesbenz.sechub.integrationtest.api.IntegrationTestSetup;
import com.mercedesbenz.sechub.integrationtest.internal.TestAutoCleanupData;
import com.mercedesbenz.sechub.integrationtest.internal.TestAutoCleanupData.TestCleanupTimeUnit;
import com.mercedesbenz.sechub.sharedkernel.messaging.MessageID;
import com.mercedesbenz.sechub.sharedkernel.usecases.UseCaseIdentifier;

public class AutoCleanupEventTraceScenario1IntTest implements IntegrationTestIsNecessaryForDocumentation {

    @Rule
    public IntegrationTestSetup setup = IntegrationTestSetup.forScenario(Scenario1.class);

    @Rule
    public Timeout timeOut = Timeout.seconds(600);

    @Test
    public void UC_ADMIN_UPDATES_AUTO_CLEANUP_CONFIGURATION() {
        /* @formatter:off */

	    /* prepare */
        resetAutoCleanupDays(0);
	    startEventInspection();

	    /* execute */
	    as(SUPER_ADMIN).
	        updateAutoCleanupConfiguration(new TestAutoCleanupData(4, TestCleanupTimeUnit.WEEK)); // starts cleanup

	    /* test */
	    assertEventInspection().
	    expect().
	       /* 0 */
	       asyncEvent(MessageID.AUTO_CLEANUP_CONFIGURATION_CHANGED).
	             from("com.mercedesbenz.sechub.domain.administration.config.AdministrationConfigService").
	             to("com.mercedesbenz.sechub.domain.schedule.ScheduleMessageHandler",
	                "com.mercedesbenz.sechub.domain.scan.ScanMessageHandler",
	                "com.mercedesbenz.sechub.domain.administration.job.JobAdministrationMessageHandler").
	    /* assert + write */
	    assertAsExpectedAndCreateHistoryFile(UseCaseIdentifier.UC_ADMIN_UPDATES_AUTO_CLEANUP_CONFIGURATION.name());
	    /* @formatter:on */
    }

    @AfterAll
    static void afterAll() {
        resetAutoCleanupDays(0);
    }

}
