// SPDX-License-Identifier: MIT
package com.daimler.sechub.integrationtest.scenario2;

import static com.daimler.sechub.integrationtest.api.TestAPI.*;
import static org.junit.Assert.*;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import com.daimler.sechub.integrationtest.api.IntegrationTestSetup;
import com.daimler.sechub.integrationtest.api.TestAPI;
import com.daimler.sechub.integrationtest.internal.IntegrationTestFileSupport;
import com.daimler.sechub.sharedkernel.messaging.IntegrationTestEventHistory;
import com.daimler.sechub.sharedkernel.messaging.IntegrationTestEventHistoryInspection;
import com.daimler.sechub.sharedkernel.messaging.MessageID;
import com.daimler.sechub.sharedkernel.usecases.UseCaseIdentifier;

public class DisableSchedulerEventTraceIntTest {

	@Rule
	public IntegrationTestSetup setup = IntegrationTestSetup.forScenario(Scenario2.class);

	@Rule
	public Timeout timeOut = Timeout.seconds(30);

	@After
	public void enableSchedulingAfterTest() {
		/* ensure scheduler job processing is enabled again after every of these tests*/
		as(SUPER_ADMIN).enableSchedulerJobProcessing();
	}

	@Test
	public void trace_scheduler_enabled() {
		/* @formatter:off */
	    
	    /* prepare */
	    UseCaseIdentifier usecaseIdentifier = UseCaseIdentifier.UC_ADMIN_DISABLES_SCHEDULER_JOB_PROCESSING;
        TestAPI.startUsecaseEventTracing(usecaseIdentifier);
	    
		/* execute */
		as(SUPER_ADMIN).
			disableSchedulerJobProcessing();
		
		waitMilliSeconds(1000);
		/* prepare */
		IntegrationTestEventHistory history  = null;
		int expectedHistgoryEntries = 2;
		do {
		    history = TestAPI.fetchEventHistory();
		    
		    waitMilliSeconds(300);
		}while(history.getIdToInspectionMap().size()<expectedHistgoryEntries);

		Map<Integer, IntegrationTestEventHistoryInspection> map = history.getIdToInspectionMap();
		assertEquals(usecaseIdentifier.name(), history.getUsecaseName());
		/* TODO Albert Tregnaghi, 2020-03-15: currently: not checked if usecase is the right one!! so does usecase makes sense in api at all?!*/
		IntegrationTestEventHistoryInspection inspection1 = map.get(0);
		assertNotNull(inspection1);
		assertEquals(MessageID.REQUEST_SCHEDULER_DISABLE_JOB_PROCESSING.getId(), inspection1.getEventId());
		assertEquals("com.daimler.sechub.domain.administration.scheduler.SwitchSchedulerJobProcessingService", inspection1.getSenderClassName());
		List<String> receiverClassNames = inspection1.getReceiverClassNames();
		System.out.println(receiverClassNames);
        assertTrue(receiverClassNames.contains("com.daimler.sechub.domain.schedule.ScheduleMessageHandler"));
        assertEquals(1,receiverClassNames.size());

		IntegrationTestEventHistoryInspection inspection2 = map.get(1);
		assertNotNull(inspection2);
		assertEquals(MessageID.SCHEDULER_JOB_PROCESSING_DISABLED.getId(), inspection2.getEventId());
		/* next is a little bit trcky - from stactrace we only know the proxy class name, so after $$ it is random...*/
		assertEquals("com.daimler.sechub.sharedkernel.messaging.DomainMessageService", inspection2.getSenderClassName());
		receiverClassNames = inspection2.getReceiverClassNames();
        System.out.println(receiverClassNames);
        assertTrue(receiverClassNames.contains("com.daimler.sechub.domain.administration.scheduler.SchedulerAdministrationMessageHandler"));
        assertTrue(receiverClassNames.contains("com.daimler.sechub.domain.notification.NotificationMessageHandler"));
        assertEquals(2,receiverClassNames.size());
		/* @formatter:on */
        
        /* write to build folder, so we can use it in documentation generation */
        File file = new File(IntegrationTestFileSupport.getTestfileSupport().getRootFolder(),"sechub-integrationtest/build/json/usecases/"+usecaseIdentifier.name().toLowerCase()+".json");
        IntegrationTestFileSupport.getTestfileSupport().writeTextFile(file,history.toJSON());

	}

}
