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
import com.daimler.sechub.sharedkernel.messaging.JobMessage;
import com.daimler.sechub.sharedkernel.usecases.job.UseCaseAdministratorRestartsJob;
import com.daimler.sechub.sharedkernel.usecases.job.UseCaseAdministratorRestartsJobHard;

@Service
public class InformAdminsThatJobRestartWasCanceledService {

	@Autowired
	MailMessageFactory factory;

	@Autowired
	NotificationConfiguration notificationConfiguration;

	@Autowired
	EmailService emailService;
	
	@UseCaseAdministratorRestartsJobHard(@Step(number = 3, name = "Inform sechub admins when job restart was canceled"))
	@UseCaseAdministratorRestartsJob(@Step(number = 3, name = "Inform sechub admins when job restart was canceled"))
	public void notify(JobMessage jobMessage, String baseUrl) {
		requireNonNull(jobMessage);

		SimpleMailMessage message = factory.createMessage("Restart of SecHub Job " + jobMessage.getJobUUID() + " was canceled");

		message.setTo(notificationConfiguration.getEmailAdministrators());
		message.setText(createEmailContent(jobMessage, baseUrl));

		emailService.send(message);

	}

	private String createEmailContent(JobMessage projectMessage, String baseUrl) {
		StringBuilder emailContent = new StringBuilder();
		emailContent.append("Restart of SecHub Job ");
		emailContent.append(projectMessage.getJobUUID());
		emailContent.append(" at ").append(baseUrl);
		emailContent.append(" was canceled.\n\n");

		emailContent.append(projectMessage.getInfo());
		
		String text = emailContent.toString();
		return text;
	}

}
