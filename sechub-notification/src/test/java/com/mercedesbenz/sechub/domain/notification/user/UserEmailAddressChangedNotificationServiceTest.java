// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.notification.user;

import static com.mercedesbenz.sechub.domain.notification.user.UserEmailAddressChangedNotificationService.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.SimpleMailMessage;

import com.mercedesbenz.sechub.domain.notification.email.EmailService;
import com.mercedesbenz.sechub.domain.notification.email.MailMessageFactory;
import com.mercedesbenz.sechub.sharedkernel.messaging.UserMessage;

class UserEmailAddressChangedNotificationServiceTest {

    private UserEmailAddressChangedNotificationService serviceToTest;
    private MailMessageFactory mailMessageFactory;
    private EmailService emailService;

    @BeforeEach
    void beforeEach() {
        serviceToTest = new UserEmailAddressChangedNotificationService();
        mailMessageFactory = mock(MailMessageFactory.class);
        emailService = mock(EmailService.class);

        serviceToTest.emailService = emailService;
        serviceToTest.factory = mailMessageFactory;
    }

    @Test
    void sends_emails_to_former_and_new_mail_address_containing_expected_content() {
        /* prepare */
        String emailAddress = "email.address@example.org";
        String formerEmailAddress = "former_email.address@example.org";

        UserMessage userMessage = new UserMessage();
        userMessage.setEmailAddress(emailAddress);
        userMessage.setFormerEmailAddress(formerEmailAddress);
        userMessage.setSubject("Your email address has been changed by a test");

        SimpleMailMessage simpleMailmessageFormer = new SimpleMailMessage();
        simpleMailmessageFormer.setSubject("<former>");

        SimpleMailMessage simpleMailmessageNew = new SimpleMailMessage();
        simpleMailmessageNew.setSubject("<new>");

        when(mailMessageFactory.createMessage(EMAIL_SUBJECT_FORMER_ADDRESS)).thenReturn(simpleMailmessageFormer);
        when(mailMessageFactory.createMessage(EMAIL_SUBJECT_NEW_ADDRESS)).thenReturn(simpleMailmessageNew);

        /* execute */
        serviceToTest.notify(userMessage);

        /* test */
        Mails mails = fetchSentMailsFromMockObjects(emailAddress, formerEmailAddress);
        assertNotNull(mails.receivedNew);
        assertNotNull(mails.receivedFormer);
        assertNotSame(mails.receivedFormer, mails.receivedNew);

        assertEquals("<former>", mails.receivedFormer.getSubject());
        assertEquals("<new>", mails.receivedNew.getSubject());

        /* @formatter:off */
        String receivedFormerText = mails.receivedFormer.getText();
        String receivedNewText = mails.receivedNew.getText();

        assertEquals("Your email address has been changed by a test and it will not be used any longer for SecHub.\n"
                + "\n"
                + "In case you do not receive a follow up notification to the new email address, please inform your SecHub administrator!",
                receivedFormerText);
        assertEquals("Your email address has been changed by a test from former_email.address@example.org to email.address@example.org. \n"
                + "Your old email address is not used in SecHub any longer.",
                receivedNewText);
        /* @formatter:on */

    }

    private Mails fetchSentMailsFromMockObjects(String emailAddress, String formerEmailAddress) {
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(emailService, times(2)).send(messageCaptor.capture());
        List<SimpleMailMessage> messagesSent = messageCaptor.getAllValues();

        assertEquals(2, messagesSent.size());
        Mails data = new Mails();
        for (SimpleMailMessage message : messagesSent) {
            if (emailAddress.equals(message.getTo()[0])) {
                data.receivedNew = message;
            }
            if (formerEmailAddress.equals(message.getTo()[0])) {
                data.receivedFormer = message;
            }
        }
        return data;
    }

    private class Mails {
        SimpleMailMessage receivedFormer;
        SimpleMailMessage receivedNew;
    }

}
