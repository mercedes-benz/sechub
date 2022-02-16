// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.notification;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.daimler.sechub.sharedkernel.MustBeDocumented;

/**
 * For examples see http://www.baeldung.com/spring-email
 *
 * @author Albert Tregnaghi
 *
 */
@Component
public class NotificationConfiguration {

    @MustBeDocumented(value = "Single mail adress used for emails to administrators. This should an NPM (non personalized mailbox)")
    @Value("${sechub.notification.email.administrators}")
    private String emailAdministrators;

    @MustBeDocumented(value = "Adress used for emails sent by sechub system")
    @Value("${sechub.notification.email.from}")
    private String emailFrom;

    @MustBeDocumented(value = "Adress used for reply when email was sent by sechub system")
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
