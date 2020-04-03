// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.notification.superadmin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

import com.daimler.sechub.domain.notification.NotificationConfiguration;
import com.daimler.sechub.domain.notification.email.EmailService;
import com.daimler.sechub.domain.notification.email.MailMessageFactory;
import com.daimler.sechub.sharedkernel.Step;
import com.daimler.sechub.sharedkernel.usecases.admin.schedule.UseCaseAdministratorDisablesSchedulerJobProcessing;

@Service
public class InformAdminsThatSchedulerJobProcessingHasBeenDisabledService {

	@Autowired
	private MailMessageFactory factory;

	@Autowired
	private NotificationConfiguration notificationConfiguration;

	@Autowired
	private EmailService emailService;

	@UseCaseAdministratorDisablesSchedulerJobProcessing(@Step(number = 4, next = {
			Step.NO_NEXT_STEP }, name = "Inform SecHub admins that scheduler job processing has been disabled"))
	public void notify(String baseUrl) {

		SimpleMailMessage message = factory.createMessage("SecHub: Scheduler job processing disabled");

		message.setTo(notificationConfiguration.getEmailAdministrators());
		message.setText(createEmailContent(baseUrl));

		emailService.send(message);

	}

	private String createEmailContent(String baseUrl) {
		StringBuilder emailContent = new StringBuilder();
		emailContent.append("Scheduler job processing has been disabled at SecHub for environment (base url): " + baseUrl + "\n\n");
		emailContent.append("WARNING: Users can still add new Jobs, but will wait for execution until scheduler job processing will be enabled again!\n");

		String text = emailContent.toString();
		return text;
	}

}
