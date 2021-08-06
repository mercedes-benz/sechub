// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.notification.superadmin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

import com.daimler.sechub.domain.notification.NotificationConfiguration;
import com.daimler.sechub.domain.notification.email.EmailService;
import com.daimler.sechub.domain.notification.email.MailMessageFactory;
import com.daimler.sechub.sharedkernel.MustBeDocumented;
import com.daimler.sechub.sharedkernel.Step;
import com.daimler.sechub.sharedkernel.messaging.ClusterMemberMessage;
import com.daimler.sechub.sharedkernel.usecases.admin.status.UseCaseAdminReceivesNotificationAboutNewchedulerInstanceStart;

@Service
public class InformAdminsThatNewSchedulerInstanceHasBeenStarted {

    private static final Logger LOG = LoggerFactory.getLogger(InformAdminsThatNewSchedulerInstanceHasBeenStarted.class);

    @Value("${sechub.notification.scheduler.startup.enabled:true}")
    @MustBeDocumented(scope = "administration", value = "When enabled, administrators will be informed by notification "
            + "when new scheduler instances are started. " + "Those notifications will also contain information about potential zombie jobs.\n\n"
            + " When disabled, incoming events wille be ignored and no notification sent.")
    boolean notificationEnabled;

    @Autowired
    private MailMessageFactory factory;

    @Autowired
    private NotificationConfiguration notificationConfiguration;

    @Autowired
    private EmailService emailService;

    @UseCaseAdminReceivesNotificationAboutNewchedulerInstanceStart(@Step(number = 2, next = {
            Step.NO_NEXT_STEP }, name = "Inform sechub admins that new scheduler job has been started"))
    public void notify(String baseUrl, ClusterMemberMessage memberMessage) {
        if (!notificationEnabled) {
            LOG.debug("Notification ignored");
            return;
        }
        SimpleMailMessage message = factory.createMessage("New SecHub scheduler instance has been started");

        message.setTo(notificationConfiguration.getEmailAdministrators());
        message.setText(createEmailContent(baseUrl, memberMessage));

        emailService.send(message);

    }

    private String createEmailContent(String baseUrl, ClusterMemberMessage memberMessage) {
        StringBuilder emailContent = new StringBuilder();
        emailContent.append("A new scheduler job instance has been started.\n\n");
        emailContent.append("Environment: base url:").append(baseUrl).append(", ");
        emailContent.append("Hostname:").append(memberMessage.getHostName()).append("\n\n");
        emailContent.append(memberMessage.getInformation());

        String text = emailContent.toString();
        return text;
    }

}
