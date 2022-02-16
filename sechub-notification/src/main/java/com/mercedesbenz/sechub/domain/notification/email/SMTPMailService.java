// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.notification.email;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.sharedkernel.Profiles;

@Service
@Profile("!" + Profiles.MOCKED_NOTIFICATIONS)
public class SMTPMailService implements EmailService {

    private static final Logger LOG = LoggerFactory.getLogger(SMTPMailService.class);

    @Autowired
    public JavaMailSender mailSender;

    @Autowired
    private SimpleMailMessageSupport mailMessageSupport;

    @Override
    public void send(SimpleMailMessage message) {
        LOG.info("sending email: {}", mailMessageSupport.describeTopic(message));
        mailSender.send(message);

    }

}
