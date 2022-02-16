// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.notification.superadmin;

import static java.util.Objects.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

import com.daimler.sechub.domain.notification.NotificationConfiguration;
import com.daimler.sechub.domain.notification.email.EmailService;
import com.daimler.sechub.domain.notification.email.MailMessageFactory;
import com.daimler.sechub.sharedkernel.Step;
import com.daimler.sechub.sharedkernel.messaging.JobMessage;
import com.daimler.sechub.sharedkernel.usecases.job.UseCaseAdminRestartsJob;
import com.daimler.sechub.sharedkernel.usecases.job.UseCaseAdminRestartsJobHard;

@Service
public class InformAdminsThatJobRestartHasBeenTriggeredService {

    @Autowired
    MailMessageFactory factory;

    @Autowired
    NotificationConfiguration notificationConfiguration;

    @Autowired
    EmailService emailService;

    @UseCaseAdminRestartsJobHard(@Step(number = 4, name = "Inform sechub admins when job has been restarted"))
    @UseCaseAdminRestartsJob(@Step(number = 4, name = "Inform sechub admins when job has been restarted"))
    public void notify(JobMessage jobMessage, String baseUrl) {
        requireNonNull(jobMessage);

        SimpleMailMessage message = factory.createMessage("Restart of SecHub Job " + jobMessage.getJobUUID() + " triggered");

        message.setTo(notificationConfiguration.getEmailAdministrators());
        message.setText(createEmailContent(jobMessage, baseUrl));

        emailService.send(message);

    }

    private String createEmailContent(JobMessage projectMessage, String baseUrl) {
        StringBuilder emailContent = new StringBuilder();
        emailContent.append("Restart of SecHub Job ");
        emailContent.append(projectMessage.getJobUUID());
        emailContent.append(" at ").append(baseUrl);
        emailContent.append(" has been triggered.\n\n");

        emailContent.append(projectMessage.getInfo());

        String text = emailContent.toString();
        return text;
    }

}
