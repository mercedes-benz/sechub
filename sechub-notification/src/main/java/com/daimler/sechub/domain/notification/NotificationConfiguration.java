// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.notification;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.daimler.sechub.sharedkernel.MustBeDocumented;

/**
 * For examples see http://www.baeldung.com/spring-email
 * @author Albert Tregnaghi
 *
 */
@Component
public class NotificationConfiguration {

	private static final String DEFAULT_MAIL_ADRESS_ADMINISTRATORS = "sechub@example.org";

	@Value("${sechub.notification.email.administrators:" + DEFAULT_MAIL_ADRESS_ADMINISTRATORS+ "}")
	private String emailAdministrators;

	@MustBeDocumented(value="Adress used for emails sent by sechub system")
	@Value("${sechub.notification.email.from}")
	private String emailFrom;

	@MustBeDocumented(value="Adress used for reply when email was sent by sechub system")
	@Value("${sechub.notification.email.replyto:}")
	private String emailReplyTo;

	public String getEmailAdministrators() {
		return emailAdministrators;
	}

	public String getEmailReplyTo() {
		return emailReplyTo;
	}

	public String getEmailFrom() {
		return emailFrom;
	}

}
