// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.administration.schedule;

import javax.annotation.security.RolesAllowed;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.daimler.sechub.sharedkernel.RoleConstants;
import com.daimler.sechub.sharedkernel.Step;
import com.daimler.sechub.sharedkernel.messaging.DomainMessage;
import com.daimler.sechub.sharedkernel.messaging.DomainMessageFactory;
import com.daimler.sechub.sharedkernel.messaging.DomainMessageService;
import com.daimler.sechub.sharedkernel.messaging.IsSendingAsyncMessage;
import com.daimler.sechub.sharedkernel.messaging.MessageID;
import com.daimler.sechub.sharedkernel.usecases.admin.schedule.UseCaseAdministratorStartScheduler;

@Service
@RolesAllowed(RoleConstants.ROLE_SUPERADMIN)
public class StartSchedulerService {

	@Autowired
	DomainMessageService eventBusService;

	/* @formatter:off */
	@UseCaseAdministratorStartScheduler(@Step(number=2,name="Service call",description="Sends request to scheduler domain to start scheduler"))
	public void startScheduler() {
		/* @formatter:on */
		sendStartSchedulerEvent();
	}

	@IsSendingAsyncMessage(MessageID.REQUEST_SCHEDULER_START)
	private void sendStartSchedulerEvent() {
		DomainMessage request = DomainMessageFactory.createRequestSchedulerStartMessage();
		eventBusService.sendAsynchron(request);
	}


}
