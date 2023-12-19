// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.notification.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.domain.notification.email.EmailService;
import com.mercedesbenz.sechub.domain.notification.email.MailMessageFactory;
import com.mercedesbenz.sechub.sharedkernel.Step;
import com.mercedesbenz.sechub.sharedkernel.messaging.UserMessage;
import com.mercedesbenz.sechub.sharedkernel.usecases.user.UseCaseUserSignup;

@Service
public class SignUpRequestedUserNotificationService {

    @Autowired
    MailMessageFactory factory;

    @Autowired
    EmailService emailService;

    @UseCaseUserSignup(@Step(number = 3, name = "Email to user", description = "A notification is send per email to user that a new user signup has been created and waits for acceptance."))
    public void notify(UserMessage userMessage) {
        /* build content */
        StringBuilder emailContent = new StringBuilder();

        emailContent.append("Thank you for the registration in SecHub.\n");
        emailContent.append("Next steps, an administrator needs to accept your registration for you to get access to SecHub.\n");
        emailContent.append("This might take some time.\n");

        /* send mail */
        SimpleMailMessage message1 = factory.createMessage("Successful registration in SecHub");
        message1.setTo(userMessage.getEmailAddress());
        message1.setText(emailContent.toString());

        emailService.send(message1);
    }

}
