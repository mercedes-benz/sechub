// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.notification.owner;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.SimpleMailMessage;

import com.daimler.sechub.domain.notification.email.EmailService;
import com.daimler.sechub.domain.notification.email.MailMessageFactory;
import com.daimler.sechub.sharedkernel.messaging.ProjectMessage;

public class InformOwnerThatProjectHasBeenDeletedNotificationServiceTest {

    private InformOwnerThatProjectHasBeenDeletedNotificationService serviceToTest;
    private EmailService mockedEmailService;
    private MailMessageFactory mockedMailMessageFactory;

    @Before
    public void before() throws Exception {
        mockedEmailService = mock(EmailService.class);
        mockedMailMessageFactory = mock(MailMessageFactory.class);

        serviceToTest = new InformOwnerThatProjectHasBeenDeletedNotificationService();
        serviceToTest.emailService = mockedEmailService;
        serviceToTest.factory = mockedMailMessageFactory;
    }

    @Test
    public void sends_email_to_former_project_owner_containing_projectid() throws Exception {

        /* prepare */
        SimpleMailMessage mockedMailMessage = mock(SimpleMailMessage.class);
        when(mockedMailMessageFactory.createMessage(any())).thenReturn(mockedMailMessage);

        // message to receive from event bus
        ProjectMessage message = mock(ProjectMessage.class);
        when(message.getProjectId()).thenReturn("projectId1");

        when(message.getProjectOwnerEmailAddress()).thenReturn("owner1@example.org");

        /* execute */
        serviceToTest.notify(message, "base1");

        /* test */
        // check mocked mail message was sent
        ArgumentCaptor<SimpleMailMessage> mailMessageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mockedEmailService).send(mailMessageCaptor.capture());
        assertSame(mockedMailMessage, mailMessageCaptor.getValue());
        verify(mockedMailMessage).setTo("owner1@example.org");

        // check content
        ArgumentCaptor<String> stringMessageCaptor = ArgumentCaptor.forClass(String.class);
        verify(mockedMailMessage).setText(stringMessageCaptor.capture());
        String textInMessageBody = stringMessageCaptor.getValue();
        assertTrue(textInMessageBody.contains("projectId1"));
        assertTrue(textInMessageBody.contains("deleted"));
    }

}
