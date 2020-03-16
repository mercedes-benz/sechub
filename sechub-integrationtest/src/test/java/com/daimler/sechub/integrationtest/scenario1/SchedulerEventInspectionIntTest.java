// SPDX-License-Identifier: MIT
package com.daimler.sechub.integrationtest.scenario1;

import static com.daimler.sechub.integrationtest.api.AssertJobScheduler.*;
import static com.daimler.sechub.integrationtest.api.TestAPI.*;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import com.daimler.sechub.integrationtest.api.AssertEventInspection;
import com.daimler.sechub.integrationtest.api.IntegrationTestSetup;
import com.daimler.sechub.integrationtest.api.TestAPI;
import com.daimler.sechub.sharedkernel.messaging.MessageID;
import com.daimler.sechub.sharedkernel.usecases.UseCaseIdentifier;

public class SchedulerEventInspectionIntTest {

	@Rule
	public IntegrationTestSetup setup = IntegrationTestSetup.forScenario(Scenario1.class);

	@Rule
	public Timeout timeOut = Timeout.seconds(12066666);

	@After
	public void enableSchedulingAfterTest() {
		/* ensure scheduler job processing is enabled again after every of these tests*/
		as(SUPER_ADMIN).enableSchedulerJobProcessing();
	}

	@Test
	public void disableSchedulerJobProcessing() {
		/* @formatter:off */
	    /* check precondition*/
        assertJobSchedulerEnabled();
        
	    /* prepare */
        TestAPI.startEventInspection();
	    
		/* execute */
		as(SUPER_ADMIN).
			disableSchedulerJobProcessing();
		
		AssertEventInspection.
		    assertLastInspection(1,
		        MessageID.SCHEDULER_JOB_PROCESSING_DISABLED,
		        "com.daimler.sechub.domain.administration.scheduler.SchedulerAdministrationMessageHandler",
		        "com.daimler.sechub.domain.notification.NotificationMessageHandler").
      		/* sanity check:*/
		    assertReceivers(0, 
      		    MessageID.REQUEST_SCHEDULER_DISABLE_JOB_PROCESSING, 
      		    "com.daimler.sechub.domain.schedule.ScheduleMessageHandler").
		    /* sanity check:*/
		    assertSender(0, 
      		    MessageID.REQUEST_SCHEDULER_DISABLE_JOB_PROCESSING, 
      		    "com.daimler.sechub.domain.administration.scheduler.SwitchSchedulerJobProcessingService").
       		/* write */
       		writeHistoryToFile(UseCaseIdentifier.UC_ADMIN_DISABLES_SCHEDULER_JOB_PROCESSING.name());
		/* @formatter:on */
	}
	
	@Test
    public void enableSchedulerJobProcessing() {
        /* @formatter:off */
	    /* prepare */
	    as(SUPER_ADMIN).
            disableSchedulerJobProcessing(); // necessary to gain all requests on enabling...
	    /* check precondition*/
	    assertJobSchedulerDisabled(); 
	    
        TestAPI.startEventInspection();
        
        /* execute */
        as(SUPER_ADMIN).
            enableSchedulerJobProcessing();
        
        AssertEventInspection.
            assertLastInspection(1,
                MessageID.SCHEDULER_JOB_PROCESSING_ENABLED,
                "com.daimler.sechub.domain.administration.scheduler.SchedulerAdministrationMessageHandler",
                "com.daimler.sechub.domain.notification.NotificationMessageHandler").
            /* write */
            writeHistoryToFile(UseCaseIdentifier.UC_ADMIN_ENABLES_SCHEDULER_JOB_PROCESSING.name());
        /* @formatter:on */
    }

}
