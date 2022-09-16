// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.notification.email;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.sharedkernel.Profiles;

/**
 * Inside this unit test we explicit check the SMTP mail service and that it can
 * be injected by spring framework.
 *
 * In our "normal" unit tests we always use {@link MockEmailService}, so an
 * explicit test is necessary.
 *
 * @author Albert Tregnaghi
 *
 */
class SMTPMailServiceTest {

    private SMTPMailService serviceToTest;
    private JavaMailSender mailSender;
    private SimpleMailMessageSupport mailMessageSupport;

    @BeforeEach
    void beforeEach() {
        serviceToTest = new SMTPMailService();

        mailSender = mock(JavaMailSender.class);
        mailMessageSupport = mock(SimpleMailMessageSupport.class);

        serviceToTest.mailSender = mailSender;
        serviceToTest.mailMessageSupport = mailMessageSupport;
    }

    @Test
    void is_instance_of_email_service() {
        assertTrue(serviceToTest instanceof EmailService);
    }

    @Test
    void is_annotated_as_service() {
        assertTrue(serviceToTest.getClass().isAnnotationPresent(Service.class));
    }

    @Test
    void is_annotated_with_profile_not_mocked_notification() {

        /* execute */
        Profile profileAnnotation = serviceToTest.getClass().getAnnotation(Profile.class);

        /* test */
        assertNotNull(profileAnnotation);
        String[] values = profileAnnotation.value();
        assertEquals(1, values.length);
        assertEquals("!" + Profiles.MOCKED_NOTIFICATIONS, values[0]);
    }

    @Test
    void sendMailMessageDelegatesTo() {
        /* prepare */
        SimpleMailMessage message = new SimpleMailMessage();

        /* execute */
        serviceToTest.send(message);

        /* test */
        verify(mailSender).send(eq(message));
    }

}
