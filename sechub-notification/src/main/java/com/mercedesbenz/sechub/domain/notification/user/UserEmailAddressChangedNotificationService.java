// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.notification.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.domain.notification.email.EmailService;
import com.mercedesbenz.sechub.domain.notification.email.MailMessageFactory;
import com.mercedesbenz.sechub.sharedkernel.Step;
import com.mercedesbenz.sechub.sharedkernel.messaging.UserMessage;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.user.UseCaseAdminUpdatesUserEmailAddress;

@Service
public class UserEmailAddressChangedNotificationService {

    static final String EMAIL_SUBJECT_NEW_ADDRESS = "Your SecHub account has been changed to this address";
    static final String EMAIL_SUBJECT_FORMER_ADDRESS = "SecHub account email address changed";

    @Autowired
    MailMessageFactory factory;

    @Autowired
    EmailService emailService;

    @UseCaseAdminUpdatesUserEmailAddress(@Step(number = 3, next = { Step.NO_NEXT_STEP }, name = "Inform user that the email address has been changed"))
    public void notify(UserMessage userMessage) {
        sendEmailToFormerUserEmailAddress(userMessage);
        sendEmailToNewUserEmailAddress(userMessage);

    }

    private void sendEmailToFormerUserEmailAddress(UserMessage userMessage) {
        StringBuilder emailContent = new StringBuilder();
        emailContent.append(createEmailStart(userMessage));
        emailContent.append(" and it will not be used any longer for SecHub.\n\n"
                + "In case you do not receive a follow up notification to the new email address, please inform your SecHub administrator!");

        SimpleMailMessage message = factory.createMessage(EMAIL_SUBJECT_FORMER_ADDRESS);
        message.setTo(userMessage.getFormerEmailAddress());
        message.setText(emailContent.toString());

        emailService.send(message);
    }

    private void sendEmailToNewUserEmailAddress(UserMessage userMessage) {

        StringBuilder emailContent = new StringBuilder();
        emailContent.append(createEmailStart(userMessage));
        emailContent.append(" from ");
        emailContent.append(userMessage.getFormerEmailAddress());
        emailContent.append(" to ");
        emailContent.append(userMessage.getEmailAddress());
        emailContent.append(". \nYour old email address is not used in SecHub any longer.");

        SimpleMailMessage message = factory.createMessage(EMAIL_SUBJECT_NEW_ADDRESS);
        message.setTo(userMessage.getEmailAddress());
        message.setText(emailContent.toString());

        emailService.send(message);
    }

    private String createEmailStart(UserMessage userMessage) {
        // We userMessage.getSubject() to create the beginning of the subject.
        // So we can use this notification service by different use cases.
        return userMessage.getSubject();
    }

}
