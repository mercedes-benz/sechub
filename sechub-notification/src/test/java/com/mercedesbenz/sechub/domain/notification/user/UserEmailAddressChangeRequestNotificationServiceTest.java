// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.notification.user;

import static com.mercedesbenz.sechub.domain.notification.user.UserEmailAddressChangeRequestNotificationService.EMAIL_SUBJECT_ADDRESS;
import static org.assertj.core.api.Assertions.assertThat;
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

class UserEmailAddressChangeRequestNotificationServiceTest {

    private UserEmailAddressChangeRequestNotificationService serviceToTest;
    private MailMessageFactory mailMessageFactory;
    private EmailService emailService;

    @BeforeEach
    void beforeEach() {
        mailMessageFactory = mock(MailMessageFactory.class);
        emailService = mock(EmailService.class);
        serviceToTest = new UserEmailAddressChangeRequestNotificationService(mailMessageFactory, emailService);
    }

    @Test
    public void sends_email_to_new_mail_address_containing_expected_content() {
        /* prepare */
        String emailAddress = "email.address@example.org";
        String formerEmailAddress = "former_email.address@example.org";

        UserMessage userMessage = new UserMessage();
        userMessage.setEmailAddress(emailAddress);
        userMessage.setFormerEmailAddress(formerEmailAddress);
        userMessage.setLinkWithOneTimeToken("http://example.org/verify?token=1234");
        userMessage.setSubject("Your email address is requested to change");

        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setSubject("<new>");

        when(mailMessageFactory.createMessage(EMAIL_SUBJECT_ADDRESS)).thenReturn(simpleMailMessage);

        /* execute */
        serviceToTest.notify(userMessage);

        /* test */
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(emailService).send(messageCaptor.capture());

        List<SimpleMailMessage> messagesSent = messageCaptor.getAllValues();
        assertThat(messagesSent.size()).isEqualTo(1);
        SimpleMailMessage receivedNew = messagesSent.get(0);
        assertThat(receivedNew).isNotNull();
        assertThat(receivedNew.getSubject()).isEqualTo(simpleMailMessage.getSubject());
        assertThat(receivedNew.getTo()).containsExactly(emailAddress);
    }
}