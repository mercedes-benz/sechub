// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.notification.email;

import org.springframework.mail.SimpleMailMessage;

public interface EmailService {

    public void send(SimpleMailMessage message);
}
