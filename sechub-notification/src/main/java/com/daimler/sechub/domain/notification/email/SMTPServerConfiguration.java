// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.notification.email;

import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Component;

import com.daimler.sechub.sharedkernel.MustBeDocumented;
import com.daimler.sechub.sharedkernel.Profiles;

/**
 * For examples see http://www.baeldung.com/spring-email, for setup of smtp by
 * java mail refer
 * https://javaee.github.io/javamail/docs/api/com/sun/mail/smtp/package-summary.html
 *
 * @author Albert Tregnaghi
 *
 */
@Component
@Profile("!" + Profiles.MOCKED_NOTIFICATIONS)
public class SMTPServerConfiguration {

	private static final Logger LOG = LoggerFactory.getLogger(SMTPServerConfiguration.class);

	private static final int DEFAULT_SMTP_SERVER_PORT = 25;

	private static final String DEFAULT_SMTP_CONFIG = "mail.smtp.auth=false";

	private SMTPConfigStringToMapConverter configConverter = new SMTPConfigStringToMapConverter();

	@MustBeDocumented("Hostname of SMPTP server")
	@Value("${sechub.notification.smtp.hostname}")
	private String hostname;

	@MustBeDocumented("Port of SMPTP server, per default:" + DEFAULT_SMTP_SERVER_PORT)
	@Value("${sechub.notification.smtp.port:" + DEFAULT_SMTP_SERVER_PORT + "}")
	private int hostPort = DEFAULT_SMTP_SERVER_PORT;

	@MustBeDocumented("SMTP configuration map. You can setup all java mail smtp settings here in comma separate form with key=value. For Example: `mail.smtp.auth=false,mail.smtp.timeout=4000`. See https://javaee.github.io/javamail/docs/api/com/sun/mail/smtp/package-summary.html for configuration mapping")
	@Value("${sechub.notification.smtp.config:"+ DEFAULT_SMTP_CONFIG+"}")
	private String smtpConfigString = DEFAULT_SMTP_CONFIG;

	@Bean
	public JavaMailSender getJavaMailSender() {
	    JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
	    mailSender.setHost(hostname);
	    mailSender.setPort(hostPort);

	    Properties props = mailSender.getJavaMailProperties();
	    props.put("mail.transport.protocol", "smtp");

		try {
			Map<String, String> map = configConverter.convertToMap(smtpConfigString);
			for (String key: map.keySet()) {
				props.put(key, map.get(key));
			}
		} catch (Exception e) {
			LOG.error("Was not able to apply given smtp configuration");
		}

	    return mailSender;
	}

}
