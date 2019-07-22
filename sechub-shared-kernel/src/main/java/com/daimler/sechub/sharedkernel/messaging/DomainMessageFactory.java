// SPDX-License-Identifier: MIT
package com.daimler.sechub.sharedkernel.messaging;

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

}
