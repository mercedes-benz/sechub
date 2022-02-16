// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.notification;

import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;

import com.daimler.sechub.domain.notification.owner.InformOwnerThatProjectHasBeenDeletedNotificationService;
import com.daimler.sechub.domain.notification.superadmin.InformAdminsThatProjectHasBeenDeletedNotificationService;
import com.daimler.sechub.domain.notification.user.InformUsersThatProjectHasBeenDeletedNotificationService;
import com.daimler.sechub.domain.notification.user.NewAPITokenAppliedUserNotificationService;
import com.daimler.sechub.domain.notification.user.NewApiTokenRequestedUserNotificationService;
import com.daimler.sechub.domain.notification.user.SignUpRequestedAdminNotificationService;
import com.daimler.sechub.domain.notification.user.UserDeletedNotificationService;
import com.daimler.sechub.sharedkernel.messaging.DomainMessage;
import com.daimler.sechub.sharedkernel.messaging.MessageDataKeys;
import com.daimler.sechub.sharedkernel.messaging.MessageID;
import com.daimler.sechub.sharedkernel.messaging.ProjectMessage;
import com.daimler.sechub.sharedkernel.messaging.UserMessage;

public class NotificationMessageHandlerTest {

    private SignUpRequestedAdminNotificationService mockedSignUpRequestedAdminNotificationService;
    private NotificationMessageHandler handlerToTest;
    private NewAPITokenAppliedUserNotificationService mockedNewAPITokenAppliedUserNotificationService;
    private NewApiTokenRequestedUserNotificationService mockedNewApiTokenRequestedUserNotificationService;
    private UserDeletedNotificationService mockedUserDeletedNotificationService;
    private InformAdminsThatProjectHasBeenDeletedNotificationService mockedInformAdminsThatProjectHasBeenDeletedNotificationService;
    private InformOwnerThatProjectHasBeenDeletedNotificationService mockedInformOwnerThatProjectHasBeenDeletedNotificationService;
    private InformUsersThatProjectHasBeenDeletedNotificationService mockedInformUsersThatProjectHasBeenDeletedNotificationService;

    @Before
    public void before() throws Exception {
        mockedSignUpRequestedAdminNotificationService = mock(SignUpRequestedAdminNotificationService.class);
        mockedNewAPITokenAppliedUserNotificationService = mock(NewAPITokenAppliedUserNotificationService.class);
        mockedNewApiTokenRequestedUserNotificationService = mock(NewApiTokenRequestedUserNotificationService.class);
        mockedUserDeletedNotificationService = mock(UserDeletedNotificationService.class);
        mockedInformAdminsThatProjectHasBeenDeletedNotificationService = mock(InformAdminsThatProjectHasBeenDeletedNotificationService.class);
        mockedInformOwnerThatProjectHasBeenDeletedNotificationService = mock(InformOwnerThatProjectHasBeenDeletedNotificationService.class);
        mockedInformUsersThatProjectHasBeenDeletedNotificationService = mock(InformUsersThatProjectHasBeenDeletedNotificationService.class);

        handlerToTest = new NotificationMessageHandler();
        handlerToTest.signupRequestedAdminNotificationService = mockedSignUpRequestedAdminNotificationService;
        handlerToTest.newAPITokenAppliedUserNotificationService = mockedNewAPITokenAppliedUserNotificationService;
        handlerToTest.newApiTokenRequestedUserNotificationService = mockedNewApiTokenRequestedUserNotificationService;
        handlerToTest.userDeletedNotificationService = mockedUserDeletedNotificationService;

        /* project deleted */
        handlerToTest.informAdminsThatProjectHasBeenDeletedService = mockedInformAdminsThatProjectHasBeenDeletedNotificationService;
        handlerToTest.informOwnerThatProjectHasBeenDeletedService = mockedInformOwnerThatProjectHasBeenDeletedNotificationService;
        handlerToTest.informUsersThatProjectHasBeenDeletedService = mockedInformUsersThatProjectHasBeenDeletedNotificationService;
    }

    @Test
    public void an_event_about_created_signup_triggers_signUpCreatedAdminNotificationService_with_included_signup_data() {
        /* prepare */
        UserMessage userMessage = mock(UserMessage.class);
        DomainMessage request = mock(DomainMessage.class);
        when(request.getMessageId()).thenReturn(MessageID.USER_SIGNUP_REQUESTED);
        when(request.get(MessageDataKeys.USER_SIGNUP_DATA)).thenReturn(userMessage);

        /* execute */
        handlerToTest.receiveAsyncMessage(request);

        /* test */
        verify(mockedSignUpRequestedAdminNotificationService).notify(userMessage);
    }

    @Test
    public void an_event_about_deleted_user_triggers_UserDeletedNotificationService() {
        /* prepare */
        UserMessage userMessage = mock(UserMessage.class);
        DomainMessage request = mock(DomainMessage.class);
        when(request.getMessageId()).thenReturn(MessageID.USER_DELETED);
        when(request.get(MessageDataKeys.USER_DELETE_DATA)).thenReturn(userMessage);

        /* execute */
        handlerToTest.receiveAsyncMessage(request);

        /* test */
        verify(mockedUserDeletedNotificationService).notify(userMessage);
    }

    @Test
    public void an_event_about_deleted_project_triggers_3_snotification_ervices() {
        /* prepare */
        ProjectMessage projectMessage = mock(ProjectMessage.class);
        DomainMessage request = mock(DomainMessage.class);
        when(request.getMessageId()).thenReturn(MessageID.PROJECT_DELETED);
        when(request.get(MessageDataKeys.PROJECT_DELETE_DATA)).thenReturn(projectMessage);
        when(request.get(MessageDataKeys.ENVIRONMENT_BASE_URL)).thenReturn("base1");

        /* execute */
        handlerToTest.receiveAsyncMessage(request);

        /* test */
        verify(mockedInformAdminsThatProjectHasBeenDeletedNotificationService).notify(projectMessage, "base1");
        verify(mockedInformOwnerThatProjectHasBeenDeletedNotificationService).notify(projectMessage, "base1");
        verify(mockedInformUsersThatProjectHasBeenDeletedNotificationService).notify(projectMessage, "base1");

    }

    @Test
    public void an_event_about_changed_api_token_triggers_newApiTokenUserNotificationService() {
        /* prepare */
        UserMessage userMessage = mock(UserMessage.class);
        DomainMessage request = mock(DomainMessage.class);
        when(request.getMessageId()).thenReturn(MessageID.USER_API_TOKEN_CHANGED);
        when(request.get(MessageDataKeys.USER_API_TOKEN_DATA)).thenReturn(userMessage);

        /* execute */
        handlerToTest.receiveAsyncMessage(request);

        /* test */
        verify(mockedNewAPITokenAppliedUserNotificationService).notify(userMessage);
    }

    @Test
    public void an_event_about_requested_new_api_token_triggers_newApiTokenRequestedUserNotificationService() {
        /* prepare */
        UserMessage userMessage = mock(UserMessage.class);
        DomainMessage request = mock(DomainMessage.class);
        when(request.getMessageId()).thenReturn(MessageID.USER_NEW_API_TOKEN_REQUESTED);
        when(request.get(MessageDataKeys.USER_ONE_TIME_TOKEN_INFO)).thenReturn(userMessage);

        /* execute */
        handlerToTest.receiveAsyncMessage(request);

        /* test */
        verify(mockedNewApiTokenRequestedUserNotificationService).notify(userMessage);
    }

}
