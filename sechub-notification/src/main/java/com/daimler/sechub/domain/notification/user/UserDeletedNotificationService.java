// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.notification.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

import com.daimler.sechub.domain.notification.email.EmailService;
import com.daimler.sechub.domain.notification.email.MailMessageFactory;
import com.daimler.sechub.sharedkernel.Step;
import com.daimler.sechub.sharedkernel.messaging.UserMessage;
import com.daimler.sechub.sharedkernel.usecases.admin.user.UseCaseAdministratorDeletesUser;

@Service
public class UserDeletedNotificationService {


	@Autowired
	private MailMessageFactory factory;


	@Autowired
	private EmailService emailService;

	@UseCaseAdministratorDeletesUser(@Step(number = 5, next = {
			Step.NO_NEXT_STEP }, name = "Inform user that the account has been deleted by administrator"))
	
	public void notify(UserMessage userMessage) {
		StringBuilder emailContent = new StringBuilder();
		emailContent.append("Your account '" + userMessage.getUserId() + "'");
		//emailContent.append(" for environment " + baseUrl);	// not trivial; maybe add this later
		emailContent.append("\nhas been removed by an administrator.\n");

		SimpleMailMessage message = factory.createMessage("SecHub account removed");
		message.setTo(userMessage.getEmailAdress());
		message.setText(emailContent.toString());

		emailService.send(message);

	}

}
