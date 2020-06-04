// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.notification.owner;

import static java.util.Objects.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

import com.daimler.sechub.domain.notification.email.EmailService;
import com.daimler.sechub.domain.notification.email.MailMessageFactory;
import com.daimler.sechub.sharedkernel.Step;
import com.daimler.sechub.sharedkernel.messaging.ProjectMessage;
import com.daimler.sechub.sharedkernel.usecases.admin.project.UseCaseAdministratorDeleteProject;

@Service
public class InformOwnerThatProjectHasBeenDeletedNotificationService {

	@Autowired
	MailMessageFactory factory;

	@Autowired
	EmailService emailService;

	private static final Logger LOG = LoggerFactory.getLogger(InformOwnerThatProjectHasBeenDeletedNotificationService.class);

	@UseCaseAdministratorDeleteProject(@Step(number = 4, name = "Inform project owner that the project has been deleted"))
	public void notify(ProjectMessage projectMessage, String baseUrl) {
		requireNonNull(projectMessage);

		String projectOwnerEmailAddress = projectMessage.getProjectOwnerEmailAddress();
		if (projectOwnerEmailAddress == null) {
			LOG.warn("No project owner email message set - can not inform owner about delete of project {}",projectMessage.getProjectId());
			return;
		}
		SimpleMailMessage message = factory.createMessage("A SecHub project you were the owner was deleted: " + projectMessage.getProjectId());

		StringBuilder emailContent = new StringBuilder();
		emailContent.append("Your project '");
		emailContent.append(projectMessage.getProjectId());
		emailContent.append("' in environment ");
		emailContent.append(baseUrl);
		emailContent.append("\n");
		emailContent.append("has been deleted.\n\n");
		emailContent.append("This means that all report data has been deleted, and thus sechub scans for this project are no longer accessible.\n");

		message.setTo(projectOwnerEmailAddress);
		message.setText(emailContent.toString());

		emailService.send(message);

	}

}
