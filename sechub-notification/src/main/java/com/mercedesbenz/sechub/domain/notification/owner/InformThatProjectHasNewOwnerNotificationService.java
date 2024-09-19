// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.notification.owner;

import static java.util.Objects.*;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.domain.notification.email.EmailService;
import com.mercedesbenz.sechub.domain.notification.email.MailMessageFactory;
import com.mercedesbenz.sechub.sharedkernel.Step;
import com.mercedesbenz.sechub.sharedkernel.messaging.ProjectMessage;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.user.UseCaseAdminChangesProjectOwner;

@Service
public class InformThatProjectHasNewOwnerNotificationService {

    @Autowired
    MailMessageFactory factory;

    @Autowired
    EmailService emailService;

    private static final Logger LOG = LoggerFactory.getLogger(InformThatProjectHasNewOwnerNotificationService.class);

    @UseCaseAdminChangesProjectOwner(@Step(number = 4, name = "Inform new and previous project owners that the project owner ship has changed"))
    public void notify(ProjectMessage projectMessage, String baseUrl) {
        requireNonNull(projectMessage);

        String projectOwnerEmailAddress = projectMessage.getProjectOwnerEmailAddress();
        String previousOwnerEmailAddress = projectMessage.getPreviousProjectOwnerEmailAddress();
        if (projectOwnerEmailAddress == null || projectOwnerEmailAddress.isEmpty()) {
            LOG.warn("No project owner email set - can not inform owner about owner change of project {}", projectMessage.getProjectId());
            return;
        }

        if (previousOwnerEmailAddress == null || previousOwnerEmailAddress.isEmpty()) {
            LOG.warn("No previous project owner email set - can not inform previous owner about owner change of project {}", projectMessage.getProjectId());
            return;
        }

        Set<String> ccMailsSet = projectMessage.getUserEmailAddresses();
        ccMailsSet.add(previousOwnerEmailAddress);

        String[] ccAddresses = ccMailsSet.stream().toArray(String[]::new);

        SimpleMailMessage message = factory.createMessage("Owner of project " + projectMessage.getProjectId() + " changed");

        StringBuilder emailContent = new StringBuilder();
        emailContent.append("Ownership of the project '" + projectMessage.getProjectId());
        emailContent.append("' in environment " + baseUrl + " has changed.");
        emailContent.append("\n\nPrevious owner: " + previousOwnerEmailAddress);
        emailContent.append("\nNew owner: " + projectOwnerEmailAddress);

        message.setTo(projectOwnerEmailAddress);
        message.setCc(ccAddresses);

        message.setText(emailContent.toString());

        emailService.send(message);
    }

}