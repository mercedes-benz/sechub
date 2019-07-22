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
import com.daimler.sechub.sharedkernel.usecases.admin.user.UseCaseAdministratorRevokesAdminRightsFromAdmin;

@Service
public class InformAdminsThatUserNoLongerAdminNotificationService {

	@Autowired
	private MailMessageFactory factory;

	@Autowired
	private NotificationConfiguration notificationConfiguration;

	@Autowired
	private EmailService emailService;

	@UseCaseAdministratorRevokesAdminRightsFromAdmin(@Step(number = 4, next = {
			Step.NO_NEXT_STEP }, name = "Inform sechub admins that another admin is no longer admin"))
	public void notify(UserMessage userMessage, String baseUrl) {

		SimpleMailMessage message = factory.createMessage("An admin lost sechub administrator rights");

		message.setTo(notificationConfiguration.getEmailAdministrators());
		message.setText(createEmailContent(userMessage, baseUrl));

		emailService.send(message);

	}

	private String createEmailContent(UserMessage userMessage, String baseUrl) {
		StringBuilder emailContent = new StringBuilder();
		emailContent.append("User ").append(userMessage.getUserId()).append("left hte group of sechub administrators.\n\n");
		emailContent.append("She/He will be no longer admin for environment (base url):").append(baseUrl).append("\n");
		emailContent.append("Email adress of old colleague was:"+userMessage.getEmailAdress());
		emailContent.append("Don't forget: Colleague email adress should be removed from NPM (email administrators) as well.");
		String text = emailContent.toString();
		return text;
	}

}
