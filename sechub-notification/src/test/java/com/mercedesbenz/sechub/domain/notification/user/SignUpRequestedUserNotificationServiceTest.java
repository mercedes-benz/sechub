// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.notification.user;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.SimpleMailMessage;

import com.mercedesbenz.sechub.domain.notification.email.EmailService;
import com.mercedesbenz.sechub.domain.notification.email.MailMessageFactory;
import com.mercedesbenz.sechub.sharedkernel.messaging.UserMessage;

public class SignUpRequestedUserNotificationServiceTest {

    private SignUpRequestedUserNotificationService serviceToTest;
    private EmailService mockedEmailService;
    private MailMessageFactory mockedMailMessageFactory;

    @Before
    public void before() throws Exception {
        mockedEmailService = mock(EmailService.class);
        mockedMailMessageFactory = mock(MailMessageFactory.class);

        serviceToTest = new SignUpRequestedUserNotificationService();
        serviceToTest.emailService = mockedEmailService;
        serviceToTest.factory = mockedMailMessageFactory;
    }

    @Test
    public void sends_email_to_user_from_event() throws Exception {

        /* prepare */
        SimpleMailMessage mockedMailMessage = mock(SimpleMailMessage.class);
        when(mockedMailMessageFactory.createMessage(any())).thenReturn(mockedMailMessage);

        // message to receive from event bus
        UserMessage message = mock(UserMessage.class);
        when(message.getEmailAddress()).thenReturn("schlau.schlumpf@schlumpfhausen.de");

        /* execute */
        serviceToTest.notify(message);

        /* test */
        // check mocked mail message was sent
        ArgumentCaptor<SimpleMailMessage> mailMessageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mockedEmailService).send(mailMessageCaptor.capture());
        assertSame(mockedMailMessage, mailMessageCaptor.getValue());
        verify(mockedMailMessage).setTo("schlau.schlumpf@schlumpfhausen.de");

        // check content
        ArgumentCaptor<String> stringMessageCaptor = ArgumentCaptor.forClass(String.class);
        verify(mockedMailMessage).setText(stringMessageCaptor.capture());
        String textInMessageBody = stringMessageCaptor.getValue();
        assertTrue(textInMessageBody.contains("Thank you for the registration in SecHub"));
        assertTrue(textInMessageBody.contains("Next steps, an administrator needs to accept your registration for you to get access to SecHub."));
        assertTrue(textInMessageBody.contains("This might take some time."));
    }

}
