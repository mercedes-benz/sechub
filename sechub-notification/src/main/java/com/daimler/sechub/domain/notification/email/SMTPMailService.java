// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.notification.email;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.daimler.sechub.sharedkernel.Profiles;

@Service
@Profile("!"+Profiles.MOCKED_NOTIFICATIONS)
public class SMTPMailService implements EmailService{

	@Autowired
	public JavaMailSender mailSender;

	@Override
	public void send(SimpleMailMessage message) {
		mailSender.send(message);
	}
	
	
}
