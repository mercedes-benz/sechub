// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.notification.email;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Component;

import com.daimler.sechub.domain.notification.NotificationConfiguration;

@Component
public class MailMessageFactory {

	@Autowired
	private NotificationConfiguration configuration;

	/**
	 * Creates a simple mail message. date, from and subject are automatically set.
	 * @param subject subject of email message
	 * @return message
	 */
	public SimpleMailMessage createMessage(String subject) {
		SimpleMailMessage message = new SimpleMailMessage();
		message.setFrom(configuration.getEmailFrom());
		message.setSubject(subject);
		message.setSentDate(new Date());
		return message;
	}
}
