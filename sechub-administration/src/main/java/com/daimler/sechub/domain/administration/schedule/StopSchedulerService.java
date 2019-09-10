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
import com.daimler.sechub.sharedkernel.usecases.admin.schedule.UseCaseAdministratorStopScheduler;

@Service
@RolesAllowed(RoleConstants.ROLE_SUPERADMIN)
public class StopSchedulerService {

	@Autowired
	DomainMessageService eventBusService;

	/* @formatter:off */
	@UseCaseAdministratorStopScheduler(@Step(number=2,name="Service call",description="Sends request to scheduler domain to stop scheduler"))
	public void stopScheduler() {
		/* @formatter:on */
		sendStopSchedulerEvent();
	}

	@IsSendingAsyncMessage(MessageID.REQUEST_SCHEDULER_STOP)
	private void sendStopSchedulerEvent() {
		DomainMessage request = DomainMessageFactory.createRequestSchedulerStopMessage();
		eventBusService.sendAsynchron(request);
	}

}
