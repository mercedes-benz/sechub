// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.notification.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.domain.notification.email.EmailService;
import com.mercedesbenz.sechub.domain.notification.email.MailMessageFactory;
import com.mercedesbenz.sechub.sharedkernel.Step;
import com.mercedesbenz.sechub.sharedkernel.messaging.UserMessage;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.signup.UseCaseAdminAcceptsSignup;

@Service
public class NewApiTokenRequestedUserNotificationService {

    @Autowired
    private MailMessageFactory factory;

    @Autowired
    private EmailService emailService;

    @UseCaseAdminAcceptsSignup(@Step(number = 3, next = {
            Step.NO_NEXT_STEP }, name = "Email to user", description = "A notification is send per email to user that a new api token was requested. "
                    + "The mail contains a link for getting the secure API token"))
    public void notify(UserMessage userMessage) {
        String link = userMessage.getLinkWithOneTimeToken();

        StringBuilder emailContent = new StringBuilder();
        emailContent.append("You requested a new API token. Please use following link to get the token:\n");
        /*
         * important link must be at last line for integration testing. if changes here
         * are done please change the parts in `sechub-integrationtest
         * AssertUser#fetchOneApiTokenByMailOrFail` too!
         */
        emailContent.append(link);
        emailContent.append("\n");

        SimpleMailMessage message1 = factory.createMessage(userMessage.getSubject());
        message1.setTo(userMessage.getEmailAdress());
        message1.setText(emailContent.toString());

        emailService.send(message1);

    }

}
