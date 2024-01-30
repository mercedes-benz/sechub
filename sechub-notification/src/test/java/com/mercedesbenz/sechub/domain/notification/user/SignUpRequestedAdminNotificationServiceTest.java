// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.notification.user;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.SimpleMailMessage;

import com.mercedesbenz.sechub.domain.notification.NotificationConfiguration;
import com.mercedesbenz.sechub.domain.notification.email.EmailService;
import com.mercedesbenz.sechub.domain.notification.email.MailMessageFactory;
import com.mercedesbenz.sechub.sharedkernel.messaging.UserMessage;

public class SignUpRequestedAdminNotificationServiceTest {

    private SignUpRequestedAdminNotificationService serviceToTest;
    private NotificationConfiguration mockedNotificationConfiguration;
    private EmailService mockedEmailService;
    private MailMessageFactory mockedMailMessageFactory;

    @Before
    public void before() throws Exception {
        mockedNotificationConfiguration = mock(NotificationConfiguration.class);
        mockedEmailService = mock(EmailService.class);
        mockedMailMessageFactory = mock(MailMessageFactory.class);

        serviceToTest = new SignUpRequestedAdminNotificationService();
        serviceToTest.emailService = mockedEmailService;
        serviceToTest.factory = mockedMailMessageFactory;
        serviceToTest.notificationConfiguration = mockedNotificationConfiguration;
    }

    @Test
    public void sends_email_to_admins_containing_userid_and_email_from_event() throws Exception {

        /* prepare */
        when(mockedNotificationConfiguration.getEmailAdministrators()).thenReturn("adminMail");

        SimpleMailMessage mockedMailMessage = mock(SimpleMailMessage.class);
        when(mockedMailMessageFactory.createMessage(any())).thenReturn(mockedMailMessage);

        // message to receive from event bus
        UserMessage message = mock(UserMessage.class);
        when(message.getUserId()).thenReturn("adam42");
        when(message.getEmailAddress()).thenReturn("new.user@example.org");

        /* execute */
        serviceToTest.notify(message);

        /* test */
        // check mocked mail message was sent
        ArgumentCaptor<SimpleMailMessage> mailMessageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mockedEmailService).send(mailMessageCaptor.capture());
        assertSame(mockedMailMessage, mailMessageCaptor.getValue());
        verify(mockedMailMessage).setTo("adminMail");

        // check content
        ArgumentCaptor<String> stringMessageCaptor = ArgumentCaptor.forClass(String.class);
        verify(mockedMailMessage).setText(stringMessageCaptor.capture());
        String textInMessageBody = stringMessageCaptor.getValue();
        assertTrue(textInMessageBody.contains("adam42"));
        assertTrue(textInMessageBody.contains("new.user@example.org"));
    }

}
