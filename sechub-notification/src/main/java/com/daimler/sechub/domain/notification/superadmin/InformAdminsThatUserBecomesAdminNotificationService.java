// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.notification.superadmin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

import com.daimler.sechub.domain.notification.NotificationConfiguration;
import com.daimler.sechub.domain.notification.email.EmailService;
import com.daimler.sechub.domain.notification.email.MailMessageFactory;
import com.daimler.sechub.sharedkernel.Step;
import com.daimler.sechub.sharedkernel.messaging.UserMessage;
import com.daimler.sechub.sharedkernel.usecases.admin.user.UseCaseAdministratorGrantsAdminRightsToUser;

@Service
public class InformAdminsThatUserBecomesAdminNotificationService {

	@Autowired
	private MailMessageFactory factory;

	@Autowired
	private NotificationConfiguration notificationConfiguration;

	@Autowired
	private EmailService emailService;

	@UseCaseAdministratorGrantsAdminRightsToUser(@Step(number = 4, next = {
			Step.NO_NEXT_STEP }, name = "Inform sechub admins that another user became administrator"))
	public void notify(UserMessage userMessage, String baseUrl) {

		SimpleMailMessage message = factory.createMessage("A user gained sechub administrator rights");

		message.setTo(notificationConfiguration.getEmailAdministrators());
		message.setText(createEmailContent(userMessage, baseUrl));

		emailService.send(message);

	}

	private String createEmailContent(UserMessage userMessage, String baseUrl) {
		StringBuilder emailContent = new StringBuilder();
		emailContent.append("Please welcome ").append(userMessage.getUserId()).append("\n\n");
		emailContent.append("as a new administrator of sechub for environment (base url):").append(baseUrl).append("\n");
		emailContent.append("Email adress of new colleague is:"+userMessage.getEmailAdress());
		emailContent.append("Don't forget: Colleague email adress should be added to NPM (email administrators) as well.");

		String text = emailContent.toString();
		return text;
	}

}
