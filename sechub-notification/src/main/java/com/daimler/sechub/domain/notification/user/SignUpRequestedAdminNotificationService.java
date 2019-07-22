// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.notification.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

import com.daimler.sechub.domain.notification.NotificationConfiguration;
import com.daimler.sechub.domain.notification.email.EmailService;
import com.daimler.sechub.domain.notification.email.MailMessageFactory;
import com.daimler.sechub.sharedkernel.Step;
import com.daimler.sechub.sharedkernel.messaging.UserMessage;
import com.daimler.sechub.sharedkernel.usecases.user.UseCaseUserSignup;

@Service
public class SignUpRequestedAdminNotificationService {

	@Autowired
	NotificationConfiguration notificationConfiguration;

	@Autowired
	MailMessageFactory factory;

	@Autowired
	EmailService emailService;

	@UseCaseUserSignup(@Step(number = 3, next = {
			Step.NO_NEXT_STEP }, name = "Email to admin", description = "A notification is send per email to admins that a new user signup has been created and waits for acceptance."))
	public void notify(UserMessage userMessage) {
		/* build content */
		StringBuilder emailContent = new StringBuilder();

		emailContent.append("A user requested access to SecHub:\n");
		emailContent.append("- Requested user id:'");
		emailContent.append(userMessage.getUserId());
		emailContent.append("''\n- Mail adress:'");
		emailContent.append(userMessage.getEmailAdress());
		emailContent.append("'\n");

		/* send mail */
		SimpleMailMessage message1 =factory.createMessage("SecHub signup requested");
		message1.setTo(notificationConfiguration.getEmailAdministrators());
		message1.setText(emailContent.toString());

		emailService.send(message1);

	}

}
