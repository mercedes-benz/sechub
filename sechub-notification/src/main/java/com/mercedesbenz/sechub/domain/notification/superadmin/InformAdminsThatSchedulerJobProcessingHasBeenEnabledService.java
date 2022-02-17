// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.notification.superadmin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.domain.notification.NotificationConfiguration;
import com.mercedesbenz.sechub.domain.notification.email.EmailService;
import com.mercedesbenz.sechub.domain.notification.email.MailMessageFactory;
import com.mercedesbenz.sechub.sharedkernel.Step;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.schedule.UseCaseAdminEnablesSchedulerJobProcessing;

@Service
public class InformAdminsThatSchedulerJobProcessingHasBeenEnabledService {

    @Autowired
    private MailMessageFactory factory;

    @Autowired
    private NotificationConfiguration notificationConfiguration;

    @Autowired
    private EmailService emailService;

    @UseCaseAdminEnablesSchedulerJobProcessing(@Step(number = 4, next = {
            Step.NO_NEXT_STEP }, name = "Inform SecHub admins that scheduler job processing has been enabled"))
    public void notify(String baseUrl) {

        SimpleMailMessage message = factory.createMessage("SecHub: Scheduler job processing enabled");

        message.setTo(notificationConfiguration.getEmailAdministrators());
        message.setText(createEmailContent(baseUrl));

        emailService.send(message);

    }

    private String createEmailContent(String baseUrl) {
        StringBuilder emailContent = new StringBuilder();
        emailContent.append("Scheduler job processing has been enabled at SecHub for environment (base url): " + baseUrl + "\n");

        String text = emailContent.toString();
        return text;
    }

}
