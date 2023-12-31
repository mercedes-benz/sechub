// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.notification.user;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.SimpleMailMessage;

import com.mercedesbenz.sechub.domain.notification.email.EmailService;
import com.mercedesbenz.sechub.domain.notification.email.MailMessageFactory;
import com.mercedesbenz.sechub.sharedkernel.messaging.ProjectMessage;

public class InformUsersThatProjectHasBeenDeletedNotificationServiceTest {

    private InformUsersThatProjectHasBeenDeletedNotificationService serviceToTest;
    private EmailService mockedEmailService;
    private MailMessageFactory mockedMailMessageFactory;

    @Before
    public void before() throws Exception {
        mockedEmailService = mock(EmailService.class);
        mockedMailMessageFactory = mock(MailMessageFactory.class);

        serviceToTest = new InformUsersThatProjectHasBeenDeletedNotificationService();
        serviceToTest.emailService = mockedEmailService;
        serviceToTest.factory = mockedMailMessageFactory;
    }

    @Test
    public void sends_NO_email_when_no_users_where_defined_at_a_project() throws Exception {

        /* prepare */
        SimpleMailMessage mockedMailMessage = mock(SimpleMailMessage.class);
        when(mockedMailMessageFactory.createMessage(any())).thenReturn(mockedMailMessage);

        // message to receive from event bus
        ProjectMessage message = mock(ProjectMessage.class);
        when(message.getProjectId()).thenReturn("projectId1");
        Set<String> emptyUserList = new LinkedHashSet<>();

        when(message.getUserEmailAddresses()).thenReturn(emptyUserList);

        /* execute */
        serviceToTest.notify(message, "base1");

        /* test */
        // check mocked mail message was sent
        verify(mockedEmailService, never()).send(any());
    }

    @Test
    public void sends_email_to_all_former_project_users_as_bcc_containing_projectid() throws Exception {

        /* prepare */
        SimpleMailMessage mockedMailMessage = mock(SimpleMailMessage.class);
        when(mockedMailMessageFactory.createMessage(any())).thenReturn(mockedMailMessage);

        // message to receive from event bus
        ProjectMessage message = mock(ProjectMessage.class);
        when(message.getProjectId()).thenReturn("projectId1");
        Set<String> userList = new LinkedHashSet<>();
        userList.add("test1@example.org");
        userList.add("test2@example.org");

        when(message.getUserEmailAddresses()).thenReturn(userList);

        /* execute */
        serviceToTest.notify(message, "base1");

        /* test */
        // check mocked mail message was sent
        ArgumentCaptor<SimpleMailMessage> mailMessageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mockedEmailService).send(mailMessageCaptor.capture());
        assertSame(mockedMailMessage, mailMessageCaptor.getValue());
        verify(mockedMailMessage).setBcc("test1@example.org", "test2@example.org");

        // check content
        ArgumentCaptor<String> stringMessageCaptor = ArgumentCaptor.forClass(String.class);
        verify(mockedMailMessage).setText(stringMessageCaptor.capture());
        String textInMessageBody = stringMessageCaptor.getValue();
        assertTrue(textInMessageBody.contains("projectId1"));
        assertTrue(textInMessageBody.contains("deleted"));
    }

}
