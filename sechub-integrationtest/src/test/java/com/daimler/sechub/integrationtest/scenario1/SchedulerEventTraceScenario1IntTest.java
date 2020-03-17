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

public class SchedulerEventTraceScenario1IntTest {

    @Rule
    public IntegrationTestSetup setup = IntegrationTestSetup.forScenario(Scenario1.class);

    @Rule
    public Timeout timeOut = Timeout.seconds(120);

    @After
    public void enableSchedulingAfterTest() {
        /*
         * ensure scheduler job processing is enabled again after every of these tests
         */
        as(SUPER_ADMIN).enableSchedulerJobProcessing();
    }

    @Test // use scenario1 because USER_1 not self registered at this moment
    public void UC_SIGNUP() {
        /* @formatter:off */
	    
	    /* prepare */
	    TestAPI.startEventInspection();
	    
	    /* execute */
	    as(ANONYMOUS).
	        signUpAs(Scenario1.USER_1);
	    
	    /* test */
	    AssertEventInspection.
	        assertLastInspection(0,
	            MessageID.USER_SIGNUP_REQUESTED,
	            "com.daimler.sechub.domain.notification.NotificationMessageHandler").
	    /* write */
	    writeHistoryToFile(UseCaseIdentifier.UC_SIGNUP.name());
	    /* @formatter:on */
    }
    
    @Test // use scenario1 because USER_1 not self registered at this moment
    public void UC_ADMIN_ACCEPTS_SIGNUP() {
        /* @formatter:off */
        as(ANONYMOUS).
            signUpAs(Scenario1.USER_1);
        
        assertUser(Scenario1.USER_1).
            isWaitingForSignup();
        
        /* prepare */
        TestAPI.startEventInspection();
        
        /* execute */
        as(SUPER_ADMIN).
            acceptSignup(Scenario1.USER_1);
        
        /* test */
        AssertEventInspection.
        assertLastInspection(3,
              MessageID.USER_ROLES_CHANGED,
              "com.daimler.sechub.domain.authorization.AuthMessageHandler").
        assertSender(0,
              MessageID.USER_NEW_API_TOKEN_REQUESTED,
              "com.daimler.sechub.domain.administration.user.UserCreationService").
        assertReceivers(0,
              MessageID.USER_NEW_API_TOKEN_REQUESTED,
              "com.daimler.sechub.domain.notification.NotificationMessageHandler").
        assertSender(1,
              MessageID.USER_CREATED,
              "com.daimler.sechub.domain.administration.user.UserCreationService").
        assertReceivers(1,
              MessageID.USER_CREATED,
              "com.daimler.sechub.domain.authorization.AuthMessageHandler").
        assertSender(2,
              MessageID.REQUEST_USER_ROLE_RECALCULATION,
              "com.daimler.sechub.domain.authorization.service.AuthUserCreationService").
        assertReceivers(2,
              MessageID.REQUEST_USER_ROLE_RECALCULATION,
              "com.daimler.sechub.domain.administration.user.UserRoleAdministrationMessageHandler").
        /* write */
        writeHistoryToFile(UseCaseIdentifier.UC_ADMIN_ACCEPTS_SIGNUP.name());
        /* @formatter:on */
    }

    @Test // use scenario1 because no other data necessary
    public void UC_ADMIN_DISABLES_SCHEDULER_JOB_PROCESSING() {
        /* @formatter:off */
	    /* check precondition*/
	    as(SUPER_ADMIN).
	        enableSchedulerJobProcessing();
        assertJobSchedulerEnabled();
        
	    /* prepare */
        TestAPI.startEventInspection();
	    
		/* execute */
		as(SUPER_ADMIN).
			disableSchedulerJobProcessing();
		
		/* test */
		AssertEventInspection.
		    assertLastInspection(1,
		        MessageID.SCHEDULER_JOB_PROCESSING_DISABLED,
		        "com.daimler.sechub.domain.administration.scheduler.SchedulerAdministrationMessageHandler",
		        "com.daimler.sechub.domain.notification.NotificationMessageHandler").
		    assertSender(1, 
		        MessageID.SCHEDULER_JOB_PROCESSING_DISABLED, 
		        "com.daimler.sechub.domain.schedule.config.SchedulerConfigService").
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

    @Test // use scenario1 because no other data necessary
    public void UC_ADMIN_ENABLES_SCHEDULER_JOB_PROCESSING() {
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
            assertSender(1, 
                    MessageID.SCHEDULER_JOB_PROCESSING_ENABLED, 
                    "com.daimler.sechub.domain.schedule.config.SchedulerConfigService").
            /* sanity check:*/
            assertReceivers(0, 
                    MessageID.REQUEST_SCHEDULER_ENABLE_JOB_PROCESSING, 
                    "com.daimler.sechub.domain.schedule.ScheduleMessageHandler").
            /* sanity check:*/
            assertSender(0, 
                    MessageID.REQUEST_SCHEDULER_ENABLE_JOB_PROCESSING, 
                    "com.daimler.sechub.domain.administration.scheduler.SwitchSchedulerJobProcessingService").
            /* write */
            writeHistoryToFile(UseCaseIdentifier.UC_ADMIN_ENABLES_SCHEDULER_JOB_PROCESSING.name());
        /* @formatter:on */
    }

}
