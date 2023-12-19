// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.messaging;

/**
 * Just a factory for domain messages - so less boiler plate code...
 *
 * @author Albert Tregnaghi
 *
 */
public class DomainMessageFactory {

    /**
     * Creates an empty request containing no data but only message id
     *
     * @param messageId
     * @return message
     */
    public static DomainMessage createEmptyRequest(MessageID messageId) {
        DomainMessage emptyMessage = new DomainMessage(messageId);
        return emptyMessage;

    }

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
        userMessage.setEmailAddress(email);
        userBecomesSuperAdminInfo.set(MessageDataKeys.USER_CONTACT_DATA, userMessage);
        userBecomesSuperAdminInfo.set(MessageDataKeys.ENVIRONMENT_BASE_URL, envbaseURL);

        return userBecomesSuperAdminInfo;
    }

    public static DomainMessage createUserNoLongerSuperAdmin(String userId, String email, String envbaseURL) {
        DomainMessage userBecomesSuperAdminInfo = new DomainMessage(MessageID.USER_NO_LONGER_SUPERADMIN);
        UserMessage userMessage = new UserMessage();
        userMessage.setUserId(userId);
        userMessage.setEmailAddress(email);
        userBecomesSuperAdminInfo.set(MessageDataKeys.USER_CONTACT_DATA, userMessage);
        userBecomesSuperAdminInfo.set(MessageDataKeys.ENVIRONMENT_BASE_URL, envbaseURL);

        return userBecomesSuperAdminInfo;
    }

}
