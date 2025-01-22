package com.mercedesbenz.sechub.domain.notification.user;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.domain.notification.email.EmailService;
import com.mercedesbenz.sechub.domain.notification.email.MailMessageFactory;
import com.mercedesbenz.sechub.sharedkernel.Step;
import com.mercedesbenz.sechub.sharedkernel.messaging.UserMessage;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.user.UseCaseUserUpdatesEmailAddress;

@Service
public class UserEmailAddressChangeRequestNotificationService {
    private static final String EMAIL_SUBJECT_ADDRESS = "Verify new SecHub account email address";
    private final MailMessageFactory mailMessageFactory;
    private final EmailService emailService;

    public UserEmailAddressChangeRequestNotificationService(MailMessageFactory mailMessageFactory, EmailService emailService) {
        this.mailMessageFactory = mailMessageFactory;
        this.emailService = emailService;
    }

    @UseCaseUserUpdatesEmailAddress(@Step(number = 3, name = "Send mail to verify", next = {
            Step.NO_NEXT_STEP }, description = "The service will send a mail to the new email address to verify the change request."))
    public void notify(UserMessage userMessage) {
        sendEmailToNewUserEmailAddress(userMessage);
    }

    private void sendEmailToNewUserEmailAddress(UserMessage userMessage) {
        /* @formatter:off */
        String emailContent = userMessage.getSubject() +
                " to this email address. \n\n"
                + "Please verify the change by clicking on the link below.\n\n" +
                userMessage.getLinkWithOneTimeToken();
        /* @formatter:on */
        SimpleMailMessage message = mailMessageFactory.createMessage(EMAIL_SUBJECT_ADDRESS);
        message.setTo(userMessage.getFormerEmailAddress());
        message.setText(emailContent);

        emailService.send(message);
    }
}
