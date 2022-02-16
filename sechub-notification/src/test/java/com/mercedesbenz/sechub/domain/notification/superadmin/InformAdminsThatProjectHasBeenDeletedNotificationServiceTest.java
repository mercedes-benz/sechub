// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.notification.superadmin;

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
import com.mercedesbenz.sechub.sharedkernel.messaging.ProjectMessage;

public class InformAdminsThatProjectHasBeenDeletedNotificationServiceTest {

    private InformAdminsThatProjectHasBeenDeletedNotificationService serviceToTest;
    private EmailService mockedEmailService;
    private MailMessageFactory mockedMailMessageFactory;
    private NotificationConfiguration mockedNotificationConfiguration;

    @Before
    public void before() throws Exception {
        mockedEmailService = mock(EmailService.class);
        mockedNotificationConfiguration = mock(NotificationConfiguration.class);
        mockedMailMessageFactory = mock(MailMessageFactory.class);

        serviceToTest = new InformAdminsThatProjectHasBeenDeletedNotificationService();
        serviceToTest.emailService = mockedEmailService;
        serviceToTest.factory = mockedMailMessageFactory;
        serviceToTest.notificationConfiguration = mockedNotificationConfiguration;
    }

    @Test
    public void sends_email_to_admins_containing_projectid_base_and_reason() throws Exception {
        /* prepare */
        when(mockedNotificationConfiguration.getEmailAdministrators()).thenReturn("adminMail");

        SimpleMailMessage mockedMailMessage = mock(SimpleMailMessage.class);
        when(mockedMailMessageFactory.createMessage(any())).thenReturn(mockedMailMessage);

        // message to receive from event bus
        ProjectMessage message = mock(ProjectMessage.class);
        when(message.getProjectId()).thenReturn("projectId1");

        when(message.getProjectOwnerEmailAddress()).thenReturn("owner1@example.org");
        when(message.getProjectActionTriggeredBy()).thenReturn("reason1");

        /* execute */
        serviceToTest.notify(message, "base1");

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
        assertTrue(textInMessageBody.contains("projectId1"));
        assertTrue(textInMessageBody.contains("base1"));
        assertTrue(textInMessageBody.contains("reason1"));
        assertTrue(textInMessageBody.contains("deleted"));
    }

}
