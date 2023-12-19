// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.notification.superadmin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.domain.notification.NotificationConfiguration;
import com.mercedesbenz.sechub.domain.notification.email.EmailService;
import com.mercedesbenz.sechub.domain.notification.email.MailMessageFactory;
import com.mercedesbenz.sechub.sharedkernel.Step;
import com.mercedesbenz.sechub.sharedkernel.messaging.UserMessage;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.user.UseCaseAdminRevokesAdminRightsFromAdmin;

@Service
public class InformAdminsThatUserNoLongerAdminNotificationService {

    @Autowired
    private MailMessageFactory factory;

    @Autowired
    private NotificationConfiguration notificationConfiguration;

    @Autowired
    private EmailService emailService;

    @UseCaseAdminRevokesAdminRightsFromAdmin(@Step(number = 4, next = {
            Step.NO_NEXT_STEP }, name = "Inform SecHub admins that another admin is no longer admin"))
    public void notify(UserMessage userMessage, String baseUrl) {

        SimpleMailMessage message = factory.createMessage("SecHub: Revoked administrator rights from " + userMessage.getUserId());

        message.setTo(notificationConfiguration.getEmailAdministrators());
        message.setText(createEmailContent(userMessage, baseUrl));

        emailService.send(message);

    }

    private String createEmailContent(UserMessage userMessage, String baseUrl) {
        StringBuilder emailContent = new StringBuilder();
        emailContent.append("User " + userMessage.getUserId() + " left the group of SecHub administrators.\n");
        emailContent.append("She/He will be no longer admin for environment (base url): " + baseUrl + "\n\n");
        emailContent.append("Email address of colleague was: " + userMessage.getEmailAddress() + "\n");
        emailContent.append("Don't forget to remove that email address from NPM (SecHub administrators) as well.\n");
        String text = emailContent.toString();
        return text;
    }

}
