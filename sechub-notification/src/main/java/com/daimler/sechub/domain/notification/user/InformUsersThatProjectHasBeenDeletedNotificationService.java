// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.notification.user;

import java.util.Set;

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
public class InformUsersThatProjectHasBeenDeletedNotificationService {

	private static final Logger LOG = LoggerFactory.getLogger(InformUsersThatProjectHasBeenDeletedNotificationService.class);

	@Autowired
	MailMessageFactory factory;

	@Autowired
	EmailService emailService;

	@UseCaseAdministratorDeleteProject(@Step(number = 5, name = "Inform users that the project has been deleted"))
	public void notify(ProjectMessage projectMessage) {
		Set<String> mailAdresses = projectMessage.getUserEmailAdresses();
		if (mailAdresses == null || mailAdresses.isEmpty()) {
			LOG.info("No users found for project {} so ignore sending info mail about delete",projectMessage.getProjectId());
			return;
		}
		StringBuilder emailContent = new StringBuilder();
		emailContent.append("Project ").append(projectMessage.getProjectId()).append(" has been deleted.\n");

		SimpleMailMessage message = factory.createMessage("A SecHub project where you have been a user was deleted");
		emailContent.append("This means that all report data has been deleted, and no longer access for sechub scans for this project is possible.\n\n");

		String[] userAdresses = projectMessage.getUserEmailAdresses().toArray(new String[mailAdresses.size()]);

		message.setBcc(userAdresses); // we do send per BCC so users do not get other email addresses. Maybe necessary because of data protection
		message.setText(emailContent.toString());

		emailService.send(message);

	}

}
