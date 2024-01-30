// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.notification.owner;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.SimpleMailMessage;

import com.mercedesbenz.sechub.domain.notification.email.EmailService;
import com.mercedesbenz.sechub.domain.notification.email.MailMessageFactory;
import com.mercedesbenz.sechub.sharedkernel.messaging.ProjectMessage;

public class InformThatProjectHasNewOwnerNotificationServiceTest {

    private InformThatProjectHasNewOwnerNotificationService serviceToTest;
    private EmailService mockedEmailService;
    private MailMessageFactory mockedMailMessageFactory;

    private ProjectMessage message;
    private SimpleMailMessage mockedMailMessage;
    private ArgumentCaptor<SimpleMailMessage> mailMessageCaptor;

    @Before
    public void before() throws Exception {
        mockedEmailService = mock(EmailService.class);
        mockedMailMessageFactory = mock(MailMessageFactory.class);

        serviceToTest = new InformThatProjectHasNewOwnerNotificationService();
        serviceToTest.emailService = mockedEmailService;
        serviceToTest.factory = mockedMailMessageFactory;

        mockedMailMessage = mock(SimpleMailMessage.class);
        mailMessageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        when(mockedMailMessageFactory.createMessage(any())).thenReturn(mockedMailMessage);

        // message to receive from event bus
        message = mock(ProjectMessage.class);
        when(message.getProjectId()).thenReturn("projectId1");
        when(mockedMailMessageFactory.createMessage(any())).thenReturn(mockedMailMessage);
    }

    @Test
    public void sends_email_to_former_project_owner_new_project_owner_and_users_containing_projectid() {

        /* prepare */

        when(message.getProjectOwnerEmailAddress()).thenReturn("owner1@example.org");
        when(message.getPreviousProjectOwnerEmailAddress()).thenReturn("prevowner@example.org");

        // only one element in the Set, because with several order is not guaranteed
        Set<String> userMails = new HashSet<>();
        userMails.add("user1@example.org");

        when(message.getUserEmailAddresses()).thenReturn(userMails);

        /* execute */
        serviceToTest.notify(message, "base1");

        /* test */
        // check mocked mail message was sent

        String[] ccAddresses = { "prevowner@example.org", "user1@example.org" };

        ArgumentCaptor<SimpleMailMessage> mailMessageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mockedEmailService).send(mailMessageCaptor.capture());

        SimpleMailMessage messageResult = mailMessageCaptor.getValue();
        assertSame(mockedMailMessage, messageResult);
        verify(messageResult).setTo("owner1@example.org");
        verify(messageResult).setCc(ccAddresses);

        // check content
        ArgumentCaptor<String> stringMessageCaptor = ArgumentCaptor.forClass(String.class);
        verify(mockedMailMessage).setText(stringMessageCaptor.capture());
        String textInMessageBody = stringMessageCaptor.getValue();
        assertTrue(textInMessageBody.contains("projectId1"));
        assertTrue(textInMessageBody.contains("Ownership of the project 'projectId1'"));
        assertTrue(textInMessageBody.contains("changed"));
        assertTrue(textInMessageBody.contains("owner1@example.org"));
        assertTrue(textInMessageBody.contains("prevowner@example.org"));
    }

    @Test
    public void send_email_omitted_because_of_empty_owner_mail_address() {

        /* prepare */
        // message to receive from event bus
        ProjectMessage message = mock(ProjectMessage.class);
        when(message.getProjectId()).thenReturn("projectId1");

        when(message.getProjectOwnerEmailAddress()).thenReturn("");
        when(message.getPreviousProjectOwnerEmailAddress()).thenReturn("prevowner@example.org");

        /* execute */
        serviceToTest.notify(message, "base1");

        /* test */
        // check mocked mail message was omitted because of missing owner mail address

        verify(mockedEmailService, never()).send(mailMessageCaptor.capture());

    }

    @Test
    public void send_email_omitted_because_of_null_owner_mail_address() {

        /* prepare */
        // message to receive from event bus
        ProjectMessage message = mock(ProjectMessage.class);
        when(message.getProjectId()).thenReturn("projectId1");

        when(message.getProjectOwnerEmailAddress()).thenReturn(null);
        when(message.getPreviousProjectOwnerEmailAddress()).thenReturn("prevowner@example.org");

        /* execute */
        serviceToTest.notify(message, "base1");

        /* test */
        // check mocked mail message was omitted because of missing owner mail address

        verify(mockedEmailService, never()).send(mailMessageCaptor.capture());

    }

    @Test
    public void send_email_omitted_because_of_empty_previous_owner_mail_address() {

        /* prepare */
        // message to receive from event bus
        ProjectMessage message = mock(ProjectMessage.class);
        when(message.getProjectId()).thenReturn("projectId1");

        when(message.getProjectOwnerEmailAddress()).thenReturn("owner1@example.org");
        when(message.getPreviousProjectOwnerEmailAddress()).thenReturn("");

        /* execute */
        serviceToTest.notify(message, "base1");

        /* test */
        // check mocked mail message was omitted because of missing previous owner mail
        // address

        verify(mockedEmailService, never()).send(mailMessageCaptor.capture());

    }

    @Test
    public void send_email_omitted_because_of_null_previous_owner_mail_address() {

        /* prepare */
        // message to receive from event bus
        ProjectMessage message = mock(ProjectMessage.class);
        when(message.getProjectId()).thenReturn("projectId1");

        when(message.getProjectOwnerEmailAddress()).thenReturn("owner1@example.org");
        when(message.getPreviousProjectOwnerEmailAddress()).thenReturn(null);

        /* execute */
        serviceToTest.notify(message, "base1");

        /* test */
        // check mocked mail message was omitted because of missing previous owner mail
        // address

        verify(mockedEmailService, never()).send(mailMessageCaptor.capture());

    }

}
