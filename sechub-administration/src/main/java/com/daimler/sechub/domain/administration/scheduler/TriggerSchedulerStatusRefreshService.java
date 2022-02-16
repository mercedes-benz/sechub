// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.administration.scheduler;

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
import com.daimler.sechub.sharedkernel.usecases.admin.schedule.UseCaseAdminDisablesSchedulerJobProcessing;

@Service
@RolesAllowed(RoleConstants.ROLE_SUPERADMIN)
public class TriggerSchedulerStatusRefreshService {

    @Autowired
    DomainMessageService eventBusService;

    /* @formatter:off */
	@UseCaseAdminDisablesSchedulerJobProcessing(@Step(number=2,name="Service call",description="Sends request to scheduler to send updates about current status."))
	public void triggerSchedulerStatusRefresh() {
		/* @formatter:on */
        sendUpdateSchedulerStatusEvent();
    }

    @IsSendingAsyncMessage(MessageID.REQUEST_SCHEDULER_STATUS_UPDATE)
    private void sendUpdateSchedulerStatusEvent() {
        DomainMessage request = DomainMessageFactory.createEmptyRequest(MessageID.REQUEST_SCHEDULER_STATUS_UPDATE);
        eventBusService.sendAsynchron(request);
    }

}
