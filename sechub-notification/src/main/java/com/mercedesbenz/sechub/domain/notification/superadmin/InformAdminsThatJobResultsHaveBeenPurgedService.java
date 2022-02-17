// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.notification.superadmin;

import static java.util.Objects.*;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.domain.notification.NotificationConfiguration;
import com.mercedesbenz.sechub.domain.notification.email.EmailService;
import com.mercedesbenz.sechub.domain.notification.email.MailMessageFactory;
import com.mercedesbenz.sechub.sharedkernel.Step;
import com.mercedesbenz.sechub.sharedkernel.usecases.job.UseCaseAdminRestartsJob;
import com.mercedesbenz.sechub.sharedkernel.usecases.job.UseCaseAdminRestartsJobHard;

@Service
public class InformAdminsThatJobResultsHaveBeenPurgedService {

    @Autowired
    MailMessageFactory factory;

    @Autowired
    NotificationConfiguration notificationConfiguration;

    @Autowired
    EmailService emailService;

    @UseCaseAdminRestartsJobHard(@Step(number = 5, name = "Inform sechub admins when job results have been purged"))
    @UseCaseAdminRestartsJob(@Step(number = 5, name = "Inform sechub admins when job results have been purged"))
    public void notify(UUID sechubJobUUID, String baseUrl) {
        requireNonNull(sechubJobUUID);

        SimpleMailMessage message = factory.createMessage("Results of SecHub Job " + sechubJobUUID + " have been purged");

        message.setTo(notificationConfiguration.getEmailAdministrators());
        message.setText(createEmailContent(sechubJobUUID, baseUrl));

        emailService.send(message);

    }

    private String createEmailContent(UUID sechubJobUUID, String baseUrl) {
        StringBuilder emailContent = new StringBuilder();
        emailContent.append("Results of SecHub Job ");
        emailContent.append(sechubJobUUID);
        emailContent.append(" at ").append(baseUrl);
        emailContent.append(" have been purged.\n\n");

        String text = emailContent.toString();
        return text;
    }

}
