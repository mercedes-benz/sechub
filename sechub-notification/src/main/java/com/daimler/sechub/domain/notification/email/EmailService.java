// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.notification.email;

import org.springframework.mail.SimpleMailMessage;

public interface EmailService {

	public void send(SimpleMailMessage message);
}
