// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.notification.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.domain.notification.email.EmailService;
import com.mercedesbenz.sechub.domain.notification.email.MailMessageFactory;
import com.mercedesbenz.sechub.sharedkernel.Step;
import com.mercedesbenz.sechub.sharedkernel.messaging.UserMessage;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.user.UseCaseAdminRevokesAdminRightsFromAdmin;

@Service
public class InformUserThatUserNoLongerAdminNotificationService {

    @Autowired
    private MailMessageFactory factory;

    @Autowired
    private EmailService emailService;

    @UseCaseAdminRevokesAdminRightsFromAdmin(@Step(number = 3, next = { 4 }, name = "Inform user about loosing administrator rights"))
    public void notify(UserMessage userMessage, String baseUrl) {

        SimpleMailMessage message = factory.createMessage("SecHub administrator privileges revoked");

        message.setTo(userMessage.getEmailAddress());
        message.setText(createEmailContent(userMessage, baseUrl));

        emailService.send(message);

    }

    private String createEmailContent(UserMessage userMessage, String baseUrl) {
        StringBuilder emailContent = new StringBuilder();
        emailContent.append("Dear " + userMessage.getUserId() + "\n\n");
        emailContent.append("Your administrator rights for SecHub where revoked\n");
        emailContent.append("for environment: " + baseUrl + "\n");
        String text = emailContent.toString();
        return text;
    }

}
