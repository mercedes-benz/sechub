// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.notification.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

import com.daimler.sechub.domain.notification.email.EmailService;
import com.daimler.sechub.domain.notification.email.MailMessageFactory;
import com.daimler.sechub.sharedkernel.Step;
import com.daimler.sechub.sharedkernel.messaging.UserMessage;
import com.daimler.sechub.sharedkernel.usecases.admin.user.UseCaseAdministratorGrantsAdminRightsToUser;

@Service
public class InformUserThatUserBecomesAdminNotificationService {

	@Autowired
	private MailMessageFactory factory;

	@Autowired
	private EmailService emailService;

	@UseCaseAdministratorGrantsAdminRightsToUser(@Step(number = 3, next = {
			4 }, name = "Inform user that he/she becomes administrator"))
	public void notify(UserMessage userMessage, String baseUrl) {

		SimpleMailMessage message = factory.createMessage("Sechub administrator priviledges granted");

		message.setTo(userMessage.getEmailAdress());
		message.setText(createEmailContent(userMessage, baseUrl));

		emailService.send(message);

	}

	private String createEmailContent(UserMessage userMessage, String baseUrl) {
		StringBuilder emailContent = new StringBuilder();
		emailContent.append("Welcome ").append(userMessage.getUserId()).append(",\n\n");
		emailContent.append("Congratulations! You become administrator of sechub\n");
		emailContent.append("at environment:"+baseUrl);
		String text = emailContent.toString();
		return text;
	}

}
