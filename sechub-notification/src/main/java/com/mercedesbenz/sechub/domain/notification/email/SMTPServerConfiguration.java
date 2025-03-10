// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.notification.email;

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

import com.mercedesbenz.sechub.commons.core.doc.MustBeDocumented;
import com.mercedesbenz.sechub.sharedkernel.DocumentationScopeConstants;
import com.mercedesbenz.sechub.sharedkernel.Profiles;

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

    static final int DEFAULT_SMTP_SERVER_PORT = 25;

    static final String DEFAULT_SMTP_CONFIG = "mail.smtp.auth=false,mail.transport.protocol=smtp";

    private SMTPConfigStringToMapConverter configConverter = new SMTPConfigStringToMapConverter();

    @MustBeDocumented(value = "Hostname of SMPTP server", scope = DocumentationScopeConstants.SCOPE_NOTIFICATION)
    @Value("${sechub.notification.smtp.hostname}")
    String hostname;

    @MustBeDocumented(value = "Username on SMPTP server, empty value means no username", scope = DocumentationScopeConstants.SCOPE_NOTIFICATION)
    @Value("${sechub.notification.smtp.credential.username:}")
    String username;

    @MustBeDocumented(value = "Password on SMPTP server, empty value means no password", scope = DocumentationScopeConstants.SCOPE_NOTIFICATION)
    @Value("${sechub.notification.smtp.credential.password:}")
    String password;

    @MustBeDocumented(value = "Port of SMPTP server, per default:" + DEFAULT_SMTP_SERVER_PORT, scope = DocumentationScopeConstants.SCOPE_NOTIFICATION)
    @Value("${sechub.notification.smtp.port:" + DEFAULT_SMTP_SERVER_PORT + "}")
    int hostPort = DEFAULT_SMTP_SERVER_PORT;

    @MustBeDocumented(value = "SMTP configuration map. You can setup all java mail smtp settings here in comma separate form with key=value. For Example: `mail.smtp.auth=false,mail.smtp.timeout=4000`. See https://javaee.github.io/javamail/docs/api/com/sun/mail/smtp/package-summary.html for configuration mapping", scope = DocumentationScopeConstants.SCOPE_NOTIFICATION)
    @Value("${sechub.notification.smtp.config:" + DEFAULT_SMTP_CONFIG + "}")
    String smtpConfigString = DEFAULT_SMTP_CONFIG;

    @Bean
    public JavaMailSender getJavaMailSender() {
        JavaMailSenderImpl mailSender = createMailSender();
        mailSender.setHost(hostname);
        mailSender.setPort(hostPort);

        if (isNotEmpty(username)) {
            mailSender.setUsername(username);
        }
        if (isNotEmpty(password)) {
            mailSender.setPassword(password);
        }

        Properties props = mailSender.getJavaMailProperties();

        try {
            Map<String, String> map = configConverter.convertToMap(smtpConfigString);
            for (String key : map.keySet()) {
                props.put(key, map.get(key));
            }
        } catch (Exception e) {
            LOG.error("Was not able to apply given smtp configuration");
        }

        return mailSender;
    }

    protected JavaMailSenderImpl createMailSender() {
        return new JavaMailSenderImpl();
    }

    private boolean isNotEmpty(String value) {
        if (value == null || value.isEmpty()) {
            return false;
        }
        return true;
    }

}
