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
	public void notify(ProjectMessage projectMessage) {
		nonNull(projectMessage);

		String projectOwnerEmailAddress = projectMessage.getProjectOwnerEmailAddress();
		if (projectOwnerEmailAddress == null) {
			LOG.warn("No project owner email message set - can not inform owner about delete of project {}",projectMessage.getProjectId());
			return;
		}
		StringBuilder emailContent = new StringBuilder();
		emailContent.append("Project ").append(projectMessage.getProjectId()).append(" has been deleted.\n");
		emailContent.append("This means that all report data has been deleted, and no longer access for sechub scans for this project is possible.\n\n");
		emailContent.append("The project owner has been already informed.\n");

		SimpleMailMessage message = factory.createMessage("A SecHub project you were the owner was deleted");

		message.setTo(projectOwnerEmailAddress);
		message.setText(emailContent.toString());

		emailService.send(message);

	}

}
