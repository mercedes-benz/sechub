// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.notification.user;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

import com.daimler.sechub.domain.notification.email.EmailService;
import com.daimler.sechub.domain.notification.email.MailMessageFactory;
import com.daimler.sechub.sharedkernel.Step;
import com.daimler.sechub.sharedkernel.messaging.JobMessage;
import com.daimler.sechub.sharedkernel.usecases.job.UseCaseAdministratorCancelsJob;

@Service
public class InformUserThatJobHasBeenCanceledService {

	private static final Logger LOG = LoggerFactory.getLogger(InformUserThatJobHasBeenCanceledService.class);

	@Autowired
	private MailMessageFactory factory;

	@Autowired
	private EmailService emailService;

	@UseCaseAdministratorCancelsJob(@Step(number = 4, name = "Inform user that her/his job has been canceled"))
	public void notify(JobMessage jobMessage) {
		String ownerEmailAddress = jobMessage.getOwnerEmailAddress();
		if (ownerEmailAddress==null || ownerEmailAddress.isEmpty()) {
			LOG.warn("Event did not contain user email address of canceled job {}, so cannot inform!", jobMessage.getJobUUID());
			return;
		}
		SimpleMailMessage message = factory.createMessage("Your SecHub Job has been canceled");

		message.setTo(ownerEmailAddress);
		message.setText(createEmailContent(jobMessage));

		emailService.send(message);

	}

	private String createEmailContent(JobMessage jobMessage) {
		StringBuilder emailContent = new StringBuilder();
		emailContent.append("Dear ").append(jobMessage.getOwner()).append(",\n\n");
		emailContent.append("Job ").append(jobMessage.getJobUUID()).append(" in project ").append(jobMessage.getProjectId());
		emailContent.append(" has been canceled.");

		String text = emailContent.toString();
		return text;
	}

}
