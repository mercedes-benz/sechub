// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.notification.user;

import java.text.MessageFormat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.domain.notification.email.EmailService;
import com.mercedesbenz.sechub.domain.notification.email.MailMessageFactory;
import com.mercedesbenz.sechub.sharedkernel.Step;
import com.mercedesbenz.sechub.sharedkernel.messaging.UserMessage;
import com.mercedesbenz.sechub.sharedkernel.usecases.user.UseCaseUserClicksLinkToGetNewAPIToken;

@Service
public class NewAPITokenAppliedUserNotificationService {

    @Autowired
    private MailMessageFactory factory;

    @Autowired
    private EmailService emailService;

    @UseCaseUserClicksLinkToGetNewAPIToken(@Step(number = 4, next = { Step.NO_NEXT_STEP }, name = "Inform user about api token change done"))
    public void notify(UserMessage userMessage) {
        StringBuilder emailContent = new StringBuilder();
        emailContent.append("You have requested a new api token.\n");
        emailContent.append(MessageFormat.format("The new api token has been applied to your user: {0}\n\n", userMessage.getUserId()));
        emailContent.append("If you have not triggered an api token change please inform administrators.\n");

        SimpleMailMessage message1 = factory.createMessage("SecHub API token changed");
        message1.setTo(userMessage.getEmailAddress());
        message1.setText(emailContent.toString());

        emailService.send(message1);

    }

}
