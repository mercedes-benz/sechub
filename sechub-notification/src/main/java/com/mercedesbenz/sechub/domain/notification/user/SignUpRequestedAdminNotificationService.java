// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.notification.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.domain.notification.NotificationConfiguration;
import com.mercedesbenz.sechub.domain.notification.email.EmailService;
import com.mercedesbenz.sechub.domain.notification.email.MailMessageFactory;
import com.mercedesbenz.sechub.sharedkernel.Step;
import com.mercedesbenz.sechub.sharedkernel.messaging.UserMessage;
import com.mercedesbenz.sechub.sharedkernel.usecases.user.UseCaseUserSignup;

@Service
public class SignUpRequestedAdminNotificationService {

    @Autowired
    NotificationConfiguration notificationConfiguration;

    @Autowired
    MailMessageFactory factory;

    @Autowired
    EmailService emailService;

    @UseCaseUserSignup(@Step(number = 4, next = {
            Step.NO_NEXT_STEP }, name = "Email to admin", description = "A notification is send per email to admins that a new user signup has been created and waits for acceptance."))
    public void notify(UserMessage userMessage) {
        /* build content */
        StringBuilder emailContent = new StringBuilder();

        emailContent.append("A user requested access to SecHub:\n");
        emailContent.append("- Requested user id: " + userMessage.getUserId() + "\n");
        emailContent.append("- Email address: " + userMessage.getEmailAddress() + "\n");

        /* send mail */
        SimpleMailMessage message1 = factory.createMessage("SecHub signup requested: " + userMessage.getUserId());
        message1.setTo(notificationConfiguration.getEmailAdministrators());
        message1.setText(emailContent.toString());

        emailService.send(message1);

    }

}
