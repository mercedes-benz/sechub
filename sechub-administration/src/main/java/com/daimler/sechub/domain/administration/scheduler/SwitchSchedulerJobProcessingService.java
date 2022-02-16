// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.administration.scheduler;

import javax.annotation.security.RolesAllowed;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.daimler.sechub.sharedkernel.RoleConstants;
import com.daimler.sechub.sharedkernel.Step;
import com.daimler.sechub.sharedkernel.logging.AuditLogService;
import com.daimler.sechub.sharedkernel.messaging.DomainMessage;
import com.daimler.sechub.sharedkernel.messaging.DomainMessageFactory;
import com.daimler.sechub.sharedkernel.messaging.DomainMessageService;
import com.daimler.sechub.sharedkernel.messaging.IsSendingAsyncMessage;
import com.daimler.sechub.sharedkernel.messaging.MessageID;
import com.daimler.sechub.sharedkernel.usecases.admin.schedule.UseCaseAdminDisablesSchedulerJobProcessing;
import com.daimler.sechub.sharedkernel.usecases.admin.schedule.UseCaseAdminEnablesSchedulerJobProcessing;

@Service
@RolesAllowed(RoleConstants.ROLE_SUPERADMIN)
public class SwitchSchedulerJobProcessingService {

    @Autowired
    DomainMessageService eventBusService;

    @Autowired
    AuditLogService auditLogService;

    /* @formatter:off */
	@UseCaseAdminDisablesSchedulerJobProcessing(@Step(number=2,name="Service call",description="Sends request to scheduler domain to disable scheduler job processing"))
	public void disableJobProcessing() {
		/* @formatter:on */
        auditLogService.log("Disabled job processing");
        sendDisableSchedulerJobProcessingMessage();
    }

    /* @formatter:off */
	@UseCaseAdminEnablesSchedulerJobProcessing(@Step(number=2,name="Service call",description="Sends request to scheduler domain to enable scheduler job processing"))
	public void enableJobProcessing() {
		/* @formatter:on */
        auditLogService.log("Enabled job processing");
        sendEnableSchedulerJobProcessingMessage();
    }

    @IsSendingAsyncMessage(MessageID.REQUEST_SCHEDULER_ENABLE_JOB_PROCESSING)
    private void sendEnableSchedulerJobProcessingMessage() {
        DomainMessage request = DomainMessageFactory.createEmptyRequest(MessageID.REQUEST_SCHEDULER_ENABLE_JOB_PROCESSING);
        eventBusService.sendAsynchron(request);
    }

    @IsSendingAsyncMessage(MessageID.REQUEST_SCHEDULER_DISABLE_JOB_PROCESSING)
    private void sendDisableSchedulerJobProcessingMessage() {
        DomainMessage request = DomainMessageFactory.createEmptyRequest(MessageID.REQUEST_SCHEDULER_DISABLE_JOB_PROCESSING);
        eventBusService.sendAsynchron(request);
    }

}
