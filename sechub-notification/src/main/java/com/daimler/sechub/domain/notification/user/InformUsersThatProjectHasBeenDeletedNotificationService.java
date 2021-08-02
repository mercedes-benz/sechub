// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.notification.user;

import static java.util.Objects.*;

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
import com.daimler.sechub.sharedkernel.usecases.admin.project.UseCaseAdminDeleteProject;

@Service
public class InformUsersThatProjectHasBeenDeletedNotificationService {

	private static final Logger LOG = LoggerFactory.getLogger(InformUsersThatProjectHasBeenDeletedNotificationService.class);

	@Autowired
	MailMessageFactory factory;

	@Autowired
	EmailService emailService;

	@UseCaseAdminDeleteProject(@Step(number = 5, name = "Inform users that the project has been deleted"))
	public void notify(ProjectMessage projectMessage, String baseUrl) {
		requireNonNull(projectMessage);

		Set<String> mailAdresses = projectMessage.getUserEmailAdresses();
		if (mailAdresses == null || mailAdresses.isEmpty()) {
			LOG.info("No users found for project {} so ignore sending info mail about delete", projectMessage.getProjectId());
			return;
		}
		SimpleMailMessage message = factory.createMessage("A SecHub project where you have been a user was deleted: " + projectMessage.getProjectId());

		StringBuilder emailContent = new StringBuilder();
		emailContent.append("Project '" + projectMessage.getProjectId() + "' in environment " + baseUrl + "\n");
		emailContent.append("has been deleted.\n\n");
		emailContent.append("This means that all report data has been deleted, and thus sechub scans for this project are no longer accessible.\n");

		String[] userAdresses = projectMessage.getUserEmailAdresses().toArray(new String[mailAdresses.size()]);

		message.setBcc(userAdresses); // we do send per BCC so users do not get other email addresses. Maybe necessary
										// because of data protection
		message.setText(emailContent.toString());

		emailService.send(message);

	}

}
