// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.notification.email;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.domain.notification.NotificationConfiguration;

@Component
public class MailMessageFactory {

    @Autowired
    NotificationConfiguration configuration;

    /**
     * Creates a simple mail message. date, from and subject are automatically set.
     *
     * @param subject subject of email message
     * @return message
     */
    public SimpleMailMessage createMessage(String subject) {
        String emailReplyTo = configuration.getEmailReplyTo();

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(configuration.getEmailFrom());
        if (isEmpty(emailReplyTo)) {
            message.setReplyTo(emailReplyTo);
        }
        message.setSubject(subject);
        message.setSentDate(new Date());
        return message;
    }

    private boolean isEmpty(String value) {
        if (value == null || value.isEmpty()) {
            return false;
        }
        return true;
    }
}
