// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.schedule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.daimler.sechub.domain.schedule.access.ScheduleGrantUserAccessToProjectService;
import com.daimler.sechub.domain.schedule.access.ScheduleRevokeUserAccessAtAllService;
import com.daimler.sechub.domain.schedule.access.ScheduleRevokeUserAccessFromProjectService;
import com.daimler.sechub.domain.schedule.config.ScheduleConfigService;
import com.daimler.sechub.domain.schedule.whitelist.ProjectWhiteListUpdateService;
import com.daimler.sechub.sharedkernel.messaging.AsynchronMessageHandler;
import com.daimler.sechub.sharedkernel.messaging.DomainMessage;
import com.daimler.sechub.sharedkernel.messaging.IsReceivingAsyncMessage;
import com.daimler.sechub.sharedkernel.messaging.MessageDataKeys;
import com.daimler.sechub.sharedkernel.messaging.MessageID;
import com.daimler.sechub.sharedkernel.messaging.ProjectMessage;
import com.daimler.sechub.sharedkernel.messaging.UserMessage;

@Component
public class ScheduleMessageHandler implements AsynchronMessageHandler{


	private static final Logger LOG = LoggerFactory.getLogger(ScheduleMessageHandler.class);


	@Autowired
	ScheduleGrantUserAccessToProjectService grantService;

	@Autowired
	ScheduleRevokeUserAccessFromProjectService revokeUserFromProjectService;

	@Autowired
	ScheduleRevokeUserAccessAtAllService revokeUserService;

	@Autowired
	ProjectWhiteListUpdateService projectWhiteListUpdateService;

	@Autowired
	ScheduleConfigService configService;

	@Override
	public void receiveAsyncMessage(DomainMessage request) {
		MessageID messageId = request.getMessageId();
		LOG.debug("received domain request: {}", request);

		switch (messageId) {
		case USER_ADDED_TO_PROJECT:
			handleUserAddedToProject(request);
			break;
		case USER_REMOVED_FROM_PROJECT:
			handleUserRemovedFromProject(request);
			break;
		case USER_DELETED:
			handleUserDeleted(request);
			break;
		case PROJECT_CREATED:
			handleProjectCreated(request);
			break;
		case PROJECT_WHITELIST_UPDATED:
			handleProjectWhiteListUpdated(request);
			break;
		case REQUEST_SCHEDULER_STOP:
			handleSchedulerStopRequest(request);
			break;
		case REQUEST_SCHEDULER_START:
			handleSchedulerStartRequest(request);
			break;
		default:
			throw new IllegalStateException("unhandled message id:"+messageId);
		}
	}

	@IsReceivingAsyncMessage(MessageID.REQUEST_SCHEDULER_START)
	private void handleSchedulerStartRequest(DomainMessage request) {
		startOrStopSchedulerJobProcessing(request,true);

	}

	@IsReceivingAsyncMessage(MessageID.REQUEST_SCHEDULER_STOP)
	private void handleSchedulerStopRequest(DomainMessage request) {
		startOrStopSchedulerJobProcessing(request,false);
	}

	private void startOrStopSchedulerJobProcessing(DomainMessage request, boolean enabled) {
		configService.setJobProcessingEnabled(enabled);
		/* FIXME ATRIGNA, 2019-09-10: rename the other parts (also in domain administration) so its clear this enabling/disabling of job processing */
	}

	@IsReceivingAsyncMessage(MessageID.PROJECT_CREATED)
	private void handleProjectCreated(DomainMessage request) {
		ProjectMessage data = request.get(MessageDataKeys.PROJECT_CREATION_DATA);
		updateWhiteList(data);
	}

	@IsReceivingAsyncMessage(MessageID.PROJECT_WHITELIST_UPDATED)
	private void handleProjectWhiteListUpdated(DomainMessage request) {
		ProjectMessage data = request.get(MessageDataKeys.PROJECT_WHITELIST_UPDATE_DATA);
		updateWhiteList(data);
	}

	@IsReceivingAsyncMessage(MessageID.USER_ADDED_TO_PROJECT)
	private void handleUserAddedToProject(DomainMessage request) {
		UserMessage data = request.get(MessageDataKeys.PROJECT_TO_USER_DATA);
		grantService.grantUserAccessToProject(data.getUserId(),data.getProjectId());
	}

	@IsReceivingAsyncMessage(MessageID.USER_REMOVED_FROM_PROJECT)
	private void handleUserRemovedFromProject(DomainMessage request) {
		UserMessage data = request.get(MessageDataKeys.PROJECT_TO_USER_DATA);
		revokeUserFromProjectService.revokeUserAccessFromProject(data.getUserId(), data.getProjectId());
	}

	@IsReceivingAsyncMessage(MessageID.USER_DELETED)
	private void handleUserDeleted(DomainMessage request) {
		UserMessage data = request.get(MessageDataKeys.USER_DELETE_DATA);
		revokeUserService.revokeUserAccess(data.getUserId());
	}

	private void updateWhiteList(ProjectMessage data) {
		projectWhiteListUpdateService.update(data.getProjectId(),data.getWhitelist());
	}


}
