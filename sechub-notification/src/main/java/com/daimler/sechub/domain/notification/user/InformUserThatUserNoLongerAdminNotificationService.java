// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.notification.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

import com.daimler.sechub.domain.notification.email.EmailService;
import com.daimler.sechub.domain.notification.email.MailMessageFactory;
import com.daimler.sechub.sharedkernel.Step;
import com.daimler.sechub.sharedkernel.messaging.UserMessage;
import com.daimler.sechub.sharedkernel.usecases.admin.user.UseCaseAdministratorRevokesAdminRightsFromAdmin;

@Service
public class InformUserThatUserNoLongerAdminNotificationService {

	@Autowired
	private MailMessageFactory factory;

	@Autowired
	private EmailService emailService;

	@UseCaseAdministratorRevokesAdminRightsFromAdmin(@Step(number = 3, next = {
			4 }, name = "Inform user about loosing administrator rights"))
	public void notify(UserMessage userMessage, String baseUrl) {

		SimpleMailMessage message = factory.createMessage("Sechub administrator privileges revoked");

		message.setTo(userMessage.getEmailAdress());
		message.setText(createEmailContent(userMessage, baseUrl));

		emailService.send(message);

	}

	private String createEmailContent(UserMessage userMessage, String baseUrl) {
		StringBuilder emailContent = new StringBuilder();
		emailContent.append("Dear " + userMessage.getUserId() + "\n\n");
		emailContent.append("Your administrator rights for SecHub where revoked\n");
		emailContent.append("for environment: " + baseUrl + "\n");
		String text = emailContent.toString();
		return text;
	}

}
