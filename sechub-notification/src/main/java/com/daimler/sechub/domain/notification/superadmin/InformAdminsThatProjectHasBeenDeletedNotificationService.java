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
import com.daimler.sechub.sharedkernel.messaging.ProjectMessage;
import com.daimler.sechub.sharedkernel.usecases.admin.project.UseCaseAdministratorDeleteProject;

@Service
public class InformAdminsThatProjectHasBeenDeletedNotificationService {

	@Autowired
	MailMessageFactory factory;

	@Autowired
	NotificationConfiguration notificationConfiguration;

	@Autowired
	EmailService emailService;

	@UseCaseAdministratorDeleteProject(@Step(number = 3, name = "Inform sechub admins that project has been deleted"))
	public void notify(ProjectMessage projectMessage, String baseUrl) {
		requireNonNull(projectMessage);

		SimpleMailMessage message = factory.createMessage("SecHub Project " + projectMessage.getProjectId() + " has been deleted");

		message.setTo(notificationConfiguration.getEmailAdministrators());
		message.setText(createEmailContent(projectMessage, baseUrl));

		emailService.send(message);

	}

	private String createEmailContent(ProjectMessage projectMessage, String baseUrl) {
		StringBuilder emailContent = new StringBuilder();
		emailContent.append("SecHub Project " + projectMessage.getProjectId());
		emailContent.append(" at " + baseUrl + " has been deleted.\n\n");
		emailContent.append("This was triggered by: " + projectMessage.getProjectActionTriggeredBy() + "\n");
		String text = emailContent.toString();
		return text;
	}

}
