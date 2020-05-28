// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.notification.superadmin;

import static java.util.Objects.*;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

import com.daimler.sechub.domain.notification.NotificationConfiguration;
import com.daimler.sechub.domain.notification.email.EmailService;
import com.daimler.sechub.domain.notification.email.MailMessageFactory;
import com.daimler.sechub.sharedkernel.Step;
import com.daimler.sechub.sharedkernel.usecases.job.UseCaseAdministratorRestartsJob;
import com.daimler.sechub.sharedkernel.usecases.job.UseCaseAdministratorRestartsJobHard;

@Service
public class InformAdminsThatJobResultsHaveBeenPurgedService {

	@Autowired
	MailMessageFactory factory;

	@Autowired
	NotificationConfiguration notificationConfiguration;

	@Autowired
	EmailService emailService;
	
	@UseCaseAdministratorRestartsJobHard(@Step(number = 5, name = "Inform sechub admins when job results have been purged"))
	@UseCaseAdministratorRestartsJob(@Step(number = 5, name = "Inform sechub admins when job results have been purged"))
	public void notify(UUID sechubJobUUID, String baseUrl) {
		requireNonNull(sechubJobUUID);

		SimpleMailMessage message = factory.createMessage("Results of SecHub Job " + sechubJobUUID + " have been purged");

		message.setTo(notificationConfiguration.getEmailAdministrators());
		message.setText(createEmailContent(sechubJobUUID, baseUrl));

		emailService.send(message);

	}

	private String createEmailContent(UUID sechubJobUUID, String baseUrl) {
		StringBuilder emailContent = new StringBuilder();
		emailContent.append("Results of SecHub Job ");
		emailContent.append(sechubJobUUID);
		emailContent.append(" at ").append(baseUrl);
		emailContent.append(" have been purged.\n\n");
		
		String text = emailContent.toString();
		return text;
	}

}
