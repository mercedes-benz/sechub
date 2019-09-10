// SPDX-License-Identifier: MIT
package com.daimler.sechub.sharedkernel.messaging;

/**
 * Just a factory for domain messages - so less boiler plate code...
 * @author Albert Tregnaghi
 *
 */
public class DomainMessageFactory {

	public static DomainMessage createRequestRoleCalculation(String userId) {
		DomainMessage roleChangeRequest = new DomainMessage(MessageID.REQUEST_USER_ROLE_RECALCULATION);
		UserMessage userMessage = new UserMessage();
		userMessage.setUserId(userId);
		roleChangeRequest.set(MessageDataKeys.USER_ID_DATA, userMessage);

		return roleChangeRequest;
	}

	public static DomainMessage createUserBecomesSuperAdmin(String userId, String email, String envbaseURL) {
		DomainMessage userBecomesSuperAdminInfo = new DomainMessage(MessageID.USER_BECOMES_SUPERADMIN);
		UserMessage userMessage = new UserMessage();
		userMessage.setUserId(userId);
		userMessage.setEmailAdress(email);
		userBecomesSuperAdminInfo.set(MessageDataKeys.USER_CONTACT_DATA, userMessage);
		userBecomesSuperAdminInfo.set(MessageDataKeys.ENVIRONMENT_BASE_URL, envbaseURL);

		return userBecomesSuperAdminInfo;
	}

	public static DomainMessage createUserNoLongerSuperAdmin(String userId, String email, String envbaseURL) {
		DomainMessage userBecomesSuperAdminInfo = new DomainMessage(MessageID.USER_NO_LONGER_SUPERADMIN);
		UserMessage userMessage = new UserMessage();
		userMessage.setUserId(userId);
		userMessage.setEmailAdress(email);
		userBecomesSuperAdminInfo.set(MessageDataKeys.USER_CONTACT_DATA, userMessage);
		userBecomesSuperAdminInfo.set(MessageDataKeys.ENVIRONMENT_BASE_URL, envbaseURL);

		return userBecomesSuperAdminInfo;
	}

	public static DomainMessage createRequestSchedulerStopMessage() {
		DomainMessage stopSchedulerMessage = new DomainMessage(MessageID.REQUEST_SCHEDULER_STOP);
		return stopSchedulerMessage;
	}

	public static DomainMessage createRequestSchedulerStartMessage() {
		DomainMessage startSchedulerMessage = new DomainMessage(MessageID.REQUEST_SCHEDULER_START);
		return startSchedulerMessage;
	}

	public static DomainMessage createRequestSchedulerStatusUpdateMessage() {
		DomainMessage startSchedulerMessage = new DomainMessage(MessageID.REQUEST_SCHEDULER_STATUS_UPDATE);
		return startSchedulerMessage;
	}


}
